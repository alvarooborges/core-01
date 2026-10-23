package net.hyze.core.spigot.echo.listeners;

import dev.utils.echo.IEchoListener;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.echo.packets.user.UserLoggedPacket;
import net.hyze.core.shared.echo.packets.user.UserOnlineVerifyPacket;
import net.hyze.core.shared.echo.packets.user.UserTellPacket;
import net.hyze.core.shared.messages.MessageUtils;
import net.hyze.core.shared.user.User;
import net.hyze.core.shared.world.location.SerializedLocation;
import net.hyze.core.spigot.echo.packets.UserLocationRequest;
import net.hyze.core.spigot.echo.packets.UserPreferenceUpdatePacket;
import net.hyze.core.spigot.misc.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.greenrobot.eventbus.Subscribe;

import java.util.Optional;

public class UserEchoListener implements IEchoListener {

    @Subscribe
    public void on(UserOnlineVerifyPacket packet) {
        User user = CoreProvider.Cache.Local.USERS.provide().get(packet.getUserId());

        UserOnlineVerifyPacket.VerifyResponse response = new UserOnlineVerifyPacket.VerifyResponse(
                Optional.ofNullable(Bukkit.getPlayerExact(user.getNick()))
                        .map(Player::isOnline)
                        .orElse(false)
        );

        packet.setResponse(response);
    }

    @Subscribe
    public void on(UserTellPacket packet) {
        User sender = CoreProvider.Cache.Local.USERS.provide().getIfPresent(packet.getSenderId());
        User target = CoreProvider.Cache.Local.USERS.provide().getIfPresent(packet.getTargetId());

        // Se nenhum dos 2 estiver online
        if (sender == null && target == null) {
            return;
        }

        if (sender == null) {
            sender = CoreProvider.Cache.Local.USERS.provide().get(packet.getSenderId());
        }

        if (target == null) {
            target = CoreProvider.Cache.Local.USERS.provide().get(packet.getTargetId());
        }

        Player senderPlayer = Bukkit.getPlayerExact(sender.getNick());

        String message = MessageUtils.stripColor(MessageUtils.translateColorCodes(packet.getMessage()));

        if (senderPlayer != null) {
            Message.EMPTY.send(senderPlayer, String.format(
                    "&8Mensagem para %s%s&8:&6 %s",
                    target.getHighestGroup().getColor(),
                    target.getNick(),
                    message
            ));
        }

        Player targetPlayer = Bukkit.getPlayerExact(target.getNick());

        if (targetPlayer != null) {
            Message.EMPTY.send(targetPlayer, String.format(
                    "&8Mensagem de %s%s&8:&6 %s",
                    sender.getHighestGroup().getColor(),
                    sender.getNick(),
                    message
            ));
        }
    }

    @Subscribe
    public void on(UserPreferenceUpdatePacket packet) {
        CoreProvider.Cache.Local.USERS_PREFERENCES.provide().refresh(packet.getUserId());
    }

    @Subscribe
    public void on(UserLocationRequest packet) {
        User user = CoreProvider.Cache.Local.USERS.provide().getIfPresent(packet.getUserId());

        if (user != null) {
            Player player = Bukkit.getPlayerExact(user.getNick());

            if (player != null && player.isOnline()) {
                Location location = player.getLocation();

                packet.setResponse(new UserLocationRequest.UserLocationResponse(new SerializedLocation(
                        CoreProvider.getApp().getId(),
                        location.getWorld().getName(),
                        location.getX(),
                        location.getY(),
                        location.getZ(),
                        location.getYaw(),
                        location.getPitch()
                )));
            }
        }
    }

    @Subscribe
    public void on(UserLoggedPacket packet) {
        User user = CoreProvider.Cache.Local.USERS.provide().getIfPresent(packet.getUserId());

        if (user != null) {
            user.setLogged(true);
        }
    }
}
