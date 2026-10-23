package net.hyze.core.spigot.misc.utils;

import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.apps.App;
import net.hyze.core.shared.apps.AppType;
import net.hyze.core.shared.echo.packets.SendMessagePacket;
import net.hyze.core.shared.echo.packets.user.connect.ConnectReason;
import net.hyze.core.shared.echo.packets.user.connect.SendUserToAppTypePacket;
import net.hyze.core.shared.echo.packets.user.connect.UserConnectHandShakePacket;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.messages.MessageUtils;
import net.hyze.core.shared.misc.utils.Position;
import net.hyze.core.shared.user.User;
import net.hyze.core.shared.world.location.SerializedLocation;
import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.events.PlayerTeleportManagerEvent;
import net.hyze.core.spigot.misc.combat.CombatManager;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.world.location.unserializer.BukkitLocationParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Deprecated
public class TeleportManager {

    public static Group AWAIT_BYPASS_GROUP = Group.ARCANE;

    public static Set<ConnectReason> REASONS_BYPASS = Sets.newHashSet(
            ConnectReason.PLUGIN,
            ConnectReason.JOIN,
            ConnectReason.RESPAWN
    );

    public static void teleport(User user, AppType type, ConnectReason reason) {
        teleport(user, type, reason, (String) null);
    }

    public static void teleport(User user, AppType type, ConnectReason reason, String message) {
        teleport(user, type, reason, (Position) null);
    }

    public static void teleport(User user, AppType type, ConnectReason reason, Position position) {
        TeleportManager.teleport(user, () -> {
            if (!type.isCurrent()) {

                CoreProvider.Redis.ECHO.provide().publish(new SendUserToAppTypePacket(
                        user.getId(),
                        CoreProvider.getApp().getServer(),
                        type,
                        reason,
                        position,
                        null,
                        reason == ConnectReason.RESPAWN // kick on error
                ));

            } else if (position != null) {

                Player player = Bukkit.getPlayerExact(user.getNick());

                if (player != null && player.isOnline()) {
                    SerializedLocation location = SerializedLocation.of(CoreProvider.getApp().getId(), "world", position);

                    player.teleport(location.parser(new BukkitLocationParser()));
                    player.teleport(location.parser(new BukkitLocationParser()));
                }

            }
        }, reason);
    }

    public static void teleport(User user, App app, ConnectReason reason, String message) {
        Runnable runnable;

        Optional<String> messageOptional = Optional.ofNullable(message)
                .map(MessageUtils::translateColorCodes);

        runnable = () -> {
            CoreProvider.Redis.ECHO.provide().publish(new UserConnectHandShakePacket(
                    user,
                    app.getId(),
                    reason
                    //                    messageOptional.orElse(null)
            ));
        };

        TeleportManager.teleport(user, runnable, reason);
    }

    public static void teleport(User user, User target, ConnectReason reason, BaseComponent[] welcomeMessage) {
        Player player = Bukkit.getPlayerExact(user.getNick());
        Player targetPlayer = Bukkit.getPlayerExact(target.getNick());

        Runnable runnable;

        if (player != null && player.isOnline() && targetPlayer != null && targetPlayer.isOnline()) {
            runnable = () -> {
                if (player.isOnline()) {
                    player.teleport(targetPlayer.getLocation());
                    player.teleport(targetPlayer.getLocation());

                    if (welcomeMessage != null) {
                        player.sendMessage(welcomeMessage);
                    }
                }
            };
        } else {
            runnable = () -> {
                App targetApp = CoreProvider.Cache.Redis.USERS_STATUS.provide().getBukkitApp(target.getNick());

                if (targetApp != null) {
                    UserConnectHandShakePacket packet = new UserConnectHandShakePacket(
                            user,
                            target,
                            targetApp.getId(),
                            reason
                    );

                    packet.setWelcomeMessage(welcomeMessage);

                    CoreProvider.Redis.ECHO.provide().publish(packet);
                }
            };
        }

        TeleportManager.teleport(user, runnable, reason);
    }

    public static void teleport(User user, SerializedLocation location, ConnectReason reason, String welcomeMessage) {
        teleport(user, location, reason, TextComponent.fromLegacyText(MessageUtils.translateFormat(welcomeMessage)));
    }

    public static void teleport(User user, SerializedLocation location, ConnectReason reason) {
        teleport(user, location, reason, (BaseComponent[]) null);
    }

    public static void teleport(User user, SerializedLocation location, ConnectReason reason, BaseComponent[] welcomeMessage) {

        Player player = Bukkit.getPlayerExact(user.getNick());

        Runnable runnable;

        if (player != null && player.isOnline() && CoreProvider.getApp().getId().equals(location.getAppId())) {
            runnable = () -> {
                if (player.isOnline()) {
                    player.teleport(location.parser(new BukkitLocationParser()));
                    player.teleport(location.parser(new BukkitLocationParser()));

                    if (welcomeMessage != null) {
                        player.sendMessage(welcomeMessage);
                    }
                }
            };
        } else {
            runnable = () -> {
                UserConnectHandShakePacket packet = new UserConnectHandShakePacket(
                        user,
                        location.getAppId(),
                        location,
                        reason
                );

                packet.setWelcomeMessage(welcomeMessage);

                CoreProvider.Redis.ECHO.provide().publish(packet);
            };
        }

        TeleportManager.teleport(user, runnable, reason);
    }

    private static void teleport(User user, Runnable runnable, ConnectReason reason) {

        Player player = Bukkit.getPlayerExact(user.getNick());

        if (player != null && player.isOnline()) {

            player.leaveVehicle();

            if (CombatManager.isTagged(user)) {
                Message.ERROR.sendDefault(player, net.hyze.core.shared.misc.utils.DefaultMessage.COMBAT_TELEPORT_ERROR);
                return;
            }
        }

        if (REASONS_BYPASS.contains(reason) || user.hasGroup(AWAIT_BYPASS_GROUP)) {

            if (player != null && player.isOnline()) {
                Bukkit.getPluginManager().callEvent(new PlayerTeleportManagerEvent(player));
            }

            runnable.run();
        } else {
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
}
