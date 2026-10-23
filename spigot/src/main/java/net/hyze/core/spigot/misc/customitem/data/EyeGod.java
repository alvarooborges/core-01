package net.hyze.core.spigot.misc.customitem.data;

import lombok.Getter;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.misc.cooldowns.UserCooldowns;
import net.hyze.core.shared.misc.utils.TimeCode;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.commands.events.ExecuteCustomCommandEvent;
import net.hyze.core.spigot.events.chat.PlayerLocalChatEvent;
import net.hyze.core.spigot.misc.combat.CombatManager;
import net.hyze.core.spigot.misc.customitem.CustomItem;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.InventoryUtils;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import net.hyze.core.spigot.misc.utils.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.TimeUnit;

public class EyeGod extends CustomItem implements Listener {

    public static String KEY = "custom_item_eye_god";
    private static boolean registeredEvents = false;

    @Getter
    private final ItemBuilder itemBuilder;

    public EyeGod() {
        super(KEY);


        this.itemBuilder = ItemBuilder.of(Material.EYE_OF_ENDER)
                .glowing(true)
                .name("&6" + this.getDisplayName())
                .lore(
                        "&7Entre em modo espectador por",
                        "&f20 segundos &7e explore o mundo",
                        "&7ao seu redor sem ser visto.",
                        "&7",
                        "&7Não será possível interagir",
                        "&7com o mundo, falar no chat",
                        "&7ou usar comandos enquanto",
                        "&7estiver sob efeito deste item."
                );

        if (!registeredEvents) {
            Bukkit.getPluginManager().registerEvents(this, CoreSpigotPlugin.getInstance());
            registeredEvents = true;
        }
    }

    @Override
    public String getDisplayName() {
        return "Sentinela";
    }

    @Subscribe
    public void on(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);


        Player player = event.getPlayer();


        User user = CoreProvider.Cache.Local.USERS.provide().get(player.getName());

        if (!UserCooldowns.hasEnded(user, KEY)) {
            Message.ERROR.send(player, String.format(
                    "Aguarde %s para usar o %s novamente.",
                    TimeCode.getFormattedTimeLeft(UserCooldowns.getMillisLeft(user, KEY)),
                    getDisplayName()
            ));
            return;
        }

        UserCooldowns.start(user, KEY, 20, TimeUnit.SECONDS);

        if (CombatManager.isTagged(user)) {
            Message.ERROR.send(player, "Você não pode usar o Sentinela em combate.");
            return;
        }

        InventoryUtils.subtractOneOnHand(event);

        player.setGameMode(GameMode.SPECTATOR);
        player.setMetadata(KEY, new FixedMetadataValue(CoreSpigotPlugin.getInstance(), true));

        Message.SUCCESS.send(player, "Você usou o Sentinela.");

        Location fallback = player.getLocation().clone();

        new BukkitRunnable() {
            int seconds = 20;

            @Override
            public void run() {

                if (seconds < 0) {
                    cancel();
                    return;
                }

                if (player.isOnline() && seconds >= 0) {
                    Title.builder()
                            .subTitle("&cFim do Sentinela em " + seconds + "s.")
                            .stay(40)
                            .build()
                            .send(player);

                    if (seconds == 0) {
                        player.removeMetadata(KEY, CoreSpigotPlugin.getInstance());
                        player.setGameMode(GameMode.SURVIVAL);

                        player.eject();
                        player.leaveVehicle();

                        Title.clear(player);

                        if (!player.teleport(fallback)) {
                            CombatManager.untag(user);
                            player.kickPlayer("Você usou o Sentinela.");
                        }
                    }
                }

                seconds--;
            }
        }.runTaskTimer(CoreSpigotPlugin.getInstance(), 0, 20);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on0(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata(KEY)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on0(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (player.hasMetadata(KEY)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on0(PlayerSpectatorTargetEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata(KEY)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on0(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata(KEY)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on0(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata(KEY)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on0(PlayerJoinEvent event) {
        event.getPlayer().removeMetadata(KEY, CoreSpigotPlugin.getInstance());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on0(PlayerTeleportEvent event) {
        if (event.getPlayer().hasMetadata(KEY) && event.getCause().equals(PlayerTeleportEvent.TeleportCause.SPECTATE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on0(ExecuteCustomCommandEvent event) {
        if (!(event.getSender() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getSender();

        if (player.hasMetadata(KEY)) {
            Message.ERROR.send(player, "Você não pode executar comandos enquanto usa o Sentinela.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void on0(PlayerLocalChatEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata(KEY)) {
            Message.ERROR.send(player, "Você não pode usar o chat enquanto usa o Sentinela.");
            event.setCancelled(true);
        }
    }
}
