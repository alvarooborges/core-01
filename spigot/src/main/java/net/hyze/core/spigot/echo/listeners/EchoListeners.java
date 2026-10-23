package net.hyze.core.spigot.echo.listeners;

import com.google.common.collect.ImmutableSet;
import dev.utils.echo.IEchoListener;
import dev.utils.shared.Printer;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.echo.packets.BroadcastMessagePacket;
import net.hyze.core.shared.echo.packets.SendMessagePacket;
import net.hyze.core.shared.echo.packets.config.ConfigReloadPacket;
import net.hyze.core.shared.echo.packets.config.ServersReloadPacket;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.echo.packets.RestartPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;
import java.util.Set;

public class EchoListeners implements IEchoListener {

    @Subscribe
    public void on(RestartPacket packet) {

        if (!packet.getApps().contains(CoreProvider.getApp().getId())) {
            return;
        }

        CoreSpigotPlugin.STOP_COUNTDOWN = packet.getSeconds();
        CoreSpigotPlugin.getInstance().getStopManager().runStop();
    }

    @Subscribe
    public void on(ConfigReloadPacket packet) {
        Printer.INFO.coloredPrint("&e[Config] Reloading...");
        CoreProvider.Cache.Local.CONFIG.provide().update(packet.getConfig());
        Printer.INFO.coloredPrint("&e[Config] Reloaded.");
    }

    @Subscribe
    public void on(ServersReloadPacket packet) {
        Printer.INFO.coloredPrint("&e[Servers] Reloading...");
        CoreProvider.Repositories.SERVERS.provide().fetchAll();
        Printer.INFO.coloredPrint("&e[Servers] Reloaded.");
    }

    @Subscribe
    public void on(SendMessagePacket packet) {
        packet.getUsersIds().stream()
                .map(userId -> CoreProvider.Cache.Local.USERS.provide().getIfPresent(userId))
                .filter(Objects::nonNull)
                .map(user -> Bukkit.getPlayerExact(user.getNick()))
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .forEach(player -> player.sendMessage(packet.getComponents()));
    }

    @Subscribe
    public void on(BroadcastMessagePacket packet) {

        if (packet.getServer() != null) {
            if (!Objects.equals(packet.getServer(), CoreProvider.getApp().getServer())) {
                return;
            }
        }

        boolean empty = packet.getGroups().isEmpty();

        Set<Player> players = ImmutableSet.copyOf(Bukkit.getOnlinePlayers());

        Bukkit.getScheduler().runTaskAsynchronously(CoreSpigotPlugin.getInstance(), () -> {
            players.stream()
                    .filter(target -> {
                        if (empty) {
                            return true;
                        }

                        User user = CoreProvider.Cache.Local.USERS.provide().get(target.getName());

                        return user != null && packet.getGroups().stream()
                                .anyMatch(group -> {
                                    if (packet.isGroupStrict()) {
                                        return user.hasStrictGroup(group);
                                    }

                                    return user.hasGroup(group);
                                });
                    })
                    .forEach(target -> target.sendMessage(packet.getComponents()));
        });
    }
}
