package net.hyze.core.spigot.listeners;

import com.google.common.collect.ImmutableSet;
import dev.utils.bukkit.stop.event.PreStopEvent;
import dev.utils.shared.Printer;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.apps.ServerStatus;
import net.hyze.core.shared.group.Group;
import net.hyze.core.spigot.CoreSpigotConstants;
import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.events.HyzePreStopCountdownEvent;
import net.hyze.core.spigot.events.HyzePreStopEvent;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.Title;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreStopEventLowest(HyzePreStopEvent event) {
        Printer.INFO.coloredPrint("&e[Core] PreStopEvent handler");

        ((ServerStatus) CoreProvider.getApp().getStatus()).setRestarting(true);

        PreStopEvent.PreStopHandler preStopHandler = event.registerPreStopHandler();

        final int seconds = event.getCountdownSeconds();

        AtomicInteger countdown = new AtomicInteger(seconds);
        String subTitle = "&eEm %s segundos";

        new BukkitRunnable() {
            @Override
            public void run() {

                if (Bukkit.getOnlinePlayers().size() == 0) {
                    preStopHandler.done();
                    cancel();
                    return;
                }


                Bukkit.getPluginManager().callEvent(new HyzePreStopCountdownEvent(seconds, countdown.get()));

                Set<? extends Player> players = ImmutableSet.copyOf(Bukkit.getOnlinePlayers());

                boolean allIsStaff = players.stream()
                        .map(p -> CoreProvider.Cache.Local.USERS.provide().get(p.getName()))
                        .allMatch(u -> u.hasGroup(Group.ADMINISTRATOR));

                if (allIsStaff) {
                    for (Player player : players) {
                        player.kickPlayer("[Staff] Reiniciando");
                    }
                }

                if (countdown.get() == seconds || (countdown.get() > 0 && countdown.get() <= 10)) {
                    for (Player player : players) {
                        Message.EMPTY.send(player, String.format("\n&eEste servidor será reiniciado em %s segundos!\n ", countdown.get()));
                    }
                }

                int count_ = countdown.getAndDecrement();

                if (count_ == -1) {

                    for (Player player : players) {
                        player.closeInventory();

                        player.kickPlayer(ChatColor.RED + "Reiniciando...");
                    }

                    return;
                }

                if (count_ <= -3) {
                    preStopHandler.done();
                    this.cancel();
                }

                if (count_ < 0) {
                    CoreSpigotConstants.STOPPING = true;
                    return;
                }

                Title title = Title.builder()
                        .stay(40)
                        .title("&eReiniciando")
                        .subTitle(String.format(subTitle, count_))
                        .build();

                for (Player player : players) {
                    title.send(player);
                    player.playSound(player.getLocation(), Sound.FIRE_IGNITE, 1, 1);
                }
            }
        }.runTaskTimer(CoreSpigotPlugin.getInstance(), 20L, 20L);
    }
}
