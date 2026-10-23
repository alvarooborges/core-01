package net.hyze.core.spigot.listeners;

import dev.utils.echo.EchoOptions;
import dev.utils.echo.IEchoListener;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.echo.packets.user.connect.ConnectConsent;
import net.hyze.core.shared.echo.packets.user.connect.UserConnectHandShakePacket;
import net.hyze.core.shared.echo.packets.user.connect.UserConnectHandShakeSuccessPacket;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.CoreSpigotConstants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

public abstract class AbstractDefaultUserHandShakeEchoListener implements IEchoListener {

    @Subscribe
    public void on(UserConnectHandShakeSuccessPacket packet) {
        User handle = CoreProvider.Cache.Local.USERS.provide().getIfPresent(packet.getUserId());

        if (handle == null) {
            return;
        }

        Player player = Bukkit.getPlayerExact(handle.getNick());

        if (player == null || !player.isOnline()) {
            return;
        }

        player.kickPlayer("connect_to:" + packet.getAppId());
    }

    @Subscribe
    public void on(UserConnectHandShakePacket packet) {
        if (packet.getProxyConsent() != ConnectConsent.ALLOWED) {
            return;
        }

        boolean changed = false;

        // Se a aplicação for a target
        TARGET:
        {
            if (packet.getTargetAppConsent() == ConnectConsent.PENDING) {
                if (Objects.equals(CoreProvider.getApp().getId(), packet.getAppId())) {
                    changed = true;
                    packet.setTargetAppConsent(ConnectConsent.ALLOWED);
                }
            }
        }

        // Se a aplicação for a current
        CURRENT:
        {
            User user = CoreProvider.Cache.Local.USERS.provide().getIfPresent(packet.getUserId());

            if (user != null) {
                Player player = Bukkit.getPlayerExact(user.getNick());

                if (player != null && player.isOnline()) {
                    if (packet.getTargetAppConsent() == ConnectConsent.ALLOWED
                            && packet.getCurrentAppConsent() == ConnectConsent.PENDING) {
                        changed = true;
                        packet.setCurrentAppConsent(ConnectConsent.ALLOWED);
                    }
                }
            }
        }

        if (changed) {
            /*
             * Reemitando pacote com a confirmação de conexão
             */
            CoreProvider.Redis.ECHO.provide().publish(
                    packet,
                    EchoOptions.builder()
                            .id(packet.getHandleHeader().getId())
                            .target(packet.getHandleHeader().getSender())
                            .build()
            );
        }
    }
}
