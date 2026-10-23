package net.hyze.core.spigot.misc.utils.teleporter;

import com.google.common.collect.Sets;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.apps.AppType;
import net.hyze.core.shared.echo.packets.SendMessagePacket;
import net.hyze.core.shared.echo.packets.user.connect.ConnectReason;
import net.hyze.core.shared.echo.packets.user.connect.SendUserToAppTypePacket;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.misc.utils.Position;
import net.hyze.core.shared.user.User;
import net.hyze.core.shared.world.location.SerializedLocation;
import net.hyze.core.spigot.CoreSpigotConstants;
import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.CoreSpigotSettings;
import net.hyze.core.spigot.events.PlayerTeleportManagerEvent;
import net.hyze.core.spigot.misc.utils.Title;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Builder
@Getter
public class Teleporter {

    public static Group GROUP_IGNORE_WAIT = Group.ARCANE;

    public static Set<ConnectReason> REASONS_IGNORE_WAIT = Sets.newHashSet(
            ConnectReason.PLUGIN,
            ConnectReason.JOIN,
            ConnectReason.RESPAWN
    );

    private final User toUser;
    private final AppType toAppType;
    private final Position toPosition;
    private final boolean kickOnError;
    private final BaseComponent[] welcomeMessage;

    @NonNull
    private final ConnectReason reason;
    public void teleport(User user) {

        Player player = Bukkit.getPlayerExact(user.getNick());

        Runnable runnable = () -> {

            boolean playerIsOnline = player != null && player.isOnline();

            if (toAppType != null) {

                if (toAppType.isCurrent() && playerIsOnline) {
                    SerializedLocation location;

                    if (toPosition != null) {
                        location = SerializedLocation.of(
                                CoreProvider.getApp().getId(),
                                CoreSpigotSettings.getInstance().getWorldName(),
                                toPosition
                        );
                    } else {
                        location = SerializedLocation.of(
                                CoreProvider.getApp().getId(),
                                CoreSpigotSettings.getInstance().getWorldName(),
                                CoreSpigotSettings.getInstance().getSpawnPosition()
                        );
                    }

                    player.teleport(location.parser(CoreSpigotConstants.LOCATION_PARSER));
                    player.teleport(location.parser(CoreSpigotConstants.LOCATION_PARSER));
                    return;
                }

                CoreProvider.Redis.ECHO.provide().publish(new SendUserToAppTypePacket(
                        user.getId(),
                        CoreProvider.getApp().getServer(),
                        toAppType,
                        reason,
                        toPosition,
                        welcomeMessage,
                        kickOnError
                ));
            }

        };

        // Sem delay
        if (REASONS_IGNORE_WAIT.contains(reason) || user.hasGroup(GROUP_IGNORE_WAIT)) {
            if (player != null && player.isOnline()) {
                Bukkit.getPluginManager().callEvent(new PlayerTeleportManagerEvent(player));
            }

            runnable.run();
        } else { // com delay
            ComponentBuilder builder = new ComponentBuilder("\nVocê será teleportado em 3 segundos.\n")
                    .color(ChatColor.YELLOW)
                    .append(" * VIPs não precisam aguardar para serem teleportado.\n");

            SendMessagePacket sendMessagePacket = new SendMessagePacket(Collections.singleton(user.getId()), builder.create());

            CoreProvider.Redis.ECHO.provide().publish(sendMessagePacket);

            AtomicInteger times = new AtomicInteger();

            new BukkitRunnable() {
                @Override
                public void run() {

                    if (times.get() == 3) {
                        if (player != null && player.isOnline()) {
                            player.leaveVehicle();

                            Title.clear(player);
                            Bukkit.getPluginManager().callEvent(new PlayerTeleportManagerEvent(player));
                        }

                        runnable.run();
                        cancel();
                        return;
                    } else if (player != null && player.isOnline()) {
                        try {
                            player.leaveVehicle();

                            Title title = Title.builder()
                                    .title(ChatColor.YELLOW + "" + (3 - times.get()))
                                    .subTitle(ChatColor.YELLOW + " VIPs não precisam esperar." + StringUtils.repeat(".", times.get()))
                                    .stay(30 * 20)
                                    .build();

                            title.send(player);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    times.incrementAndGet();
                }
            }.runTaskTimer(CoreSpigotPlugin.getInstance(), 0, 20);
        }
    }

//    public static void execute(Player player, Teleporter teleporter) {
//
//        User user = CoreProvider.Cache.Local.USERS.provide().get(player.getName());
//
//        Runnable runnable = () -> {
//
//            if (teleporter.getToUser() != null) {
//
//                Player target = Bukkit.getPlayerExact(teleporter.getToUser().getNick());
//
//                if (target != null && target.isOnline()) {
//                    player.teleport(target.getLocation());
//                    player.teleport(target.getLocation());
//                    return;
//                }
//
//
//
//                return;
//            }
//
//            if (teleporter.getToAppType() != null) {
//
//                if (teleporter.getToAppType().isCurrent()) {
//                    SerializedLocation location;
//
//                    if (teleporter.getToPosition() != null) {
//                        location = SerializedLocation.of(
//                                CoreProvider.getApp().getId(),
//                                CoreSpigotSettings.getInstance().getWorldName(),
//                                teleporter.getToPosition()
//                        );
//                    } else {
//                        location = SerializedLocation.of(
//                                CoreProvider.getApp().getId(),
//                                CoreSpigotSettings.getInstance().getWorldName(),
//                                CoreSpigotSettings.getInstance().getSpawnPosition()
//                        );
//                    }
//
//                    player.teleport(location.parser(CoreSpigotConstants.LOCATION_PARSER));
//                    player.teleport(location.parser(CoreSpigotConstants.LOCATION_PARSER));
//                    return;
//                }
//
//                CoreProvider.Redis.ECHO.provide().publish(new SendUserToAppTypePacket(
//                        user.getId(),
//                        CoreProvider.getApp().getServer(),
//                        teleporter.getToAppType(),
//                        teleporter.getReason(),
//                        teleporter.getToPosition(),
//                        teleporter.getWelcomeMessage(),
//                        teleporter.isKickOnError()
//                ));
//
//                return;
//            }
//        };
//
//        // Sem delay
//        if (REASONS_IGNORE_WAIT.contains(teleporter.getReason()) || user.hasGroup(GROUP_IGNORE_WAIT)) {
//            if (player.isOnline()) {
//                Bukkit.getPluginManager().callEvent(new PlayerTeleportManagerEvent(player));
//                runnable.run();
//            }
//        } else { // com delay
//            ComponentBuilder builder = new ComponentBuilder("\nVocê será teleportado em 3 segundos.\n")
//                    .color(ChatColor.YELLOW)
//                    .append(" * VIPs não precisam aguardar para serem teleportado.\n");
//
//            SendMessagePacket sendMessagePacket = new SendMessagePacket(Collections.singleton(user.getId()), builder.create());
//
//            CoreProvider.Redis.ECHO.provide().publish(sendMessagePacket);
//
//            AtomicInteger times = new AtomicInteger();
//
//            new BukkitRunnable() {
//                @Override
//                public void run() {
//
//                    if (times.get() == 3) {
//                        if (player.isOnline()) {
//                            player.leaveVehicle();
//
//                            Title.clear(player);
//                            Bukkit.getPluginManager().callEvent(new PlayerTeleportManagerEvent(player));
//                        }
//
//                        runnable.run();
//                        cancel();
//                        return;
//
//                    } else {
//
//                        if (!player.isOnline()) {
//                            cancel();
//                            return;
//                        }
//
//                        try {
//
//                            player.leaveVehicle();
//
//                            Title title = new Title()
//                                    .title(ChatColor.YELLOW + "" + (3 - times.get()))
//                                    .subTitle(ChatColor.YELLOW + " VIPs não precisam esperar." + StringUtils.repeat(".", times.get()))
//                                    .stay(30 * 20);
//
//                            title.send(player);
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    times.incrementAndGet();
//                }
//            }.runTaskTimer(CoreSpigotPlugin.getInstance(), 0, 20);
//        }
//    }
//
//    public void teleport(User user) {
//
//        Player player = Bukkit.getPlayerExact(user.getNick());
//
//        if (!(player != null && player.isOnline())) {
//            CoreProvider.Redis.ECHO.provide().publish(new TeleporterPacket(user, this));
//            return;
//        }
//
//        execute(player, this);
//    }
}
