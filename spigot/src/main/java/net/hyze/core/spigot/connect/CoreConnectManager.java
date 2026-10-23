package net.hyze.core.spigot.connect;

import dev.utils.echo.EchoOptions;
import dev.utils.echo.IEchoListener;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.echo.packets.user.connect.ConnectConsent;
import net.hyze.core.shared.echo.packets.user.connect.UserConnectHandShakePacket;
import net.hyze.core.shared.echo.packets.user.connect.UserConnectHandShakeSuccessPacket;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.connect.events.AsyncUserConnectHandShakeEvent;
import net.hyze.core.spigot.connect.events.ConnectSide;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class CoreConnectManager implements IEchoListener {

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

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void on(UserConnectHandShakePacket packet) {

        if (packet.getProxyConsent() != ConnectConsent.ALLOWED) {
            return;
        }

        boolean changed = false;

        TARGET:
        {
            if (CoreProvider.getApp().isSame(packet.getAppId()) && packet.getTargetAppConsent() == ConnectConsent.PENDING) {
                changed = true;

                User handle = CoreProvider.Cache.Local.USERS.provide().get(packet.getUserId());

                AsyncUserConnectHandShakeEvent event = new AsyncUserConnectHandShakeEvent(handle, ConnectSide.TARGET, packet);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    packet.setTargetAppConsent(ConnectConsent.DENIED);
                    break TARGET;
                }

                packet.setTargetAppConsent(ConnectConsent.ALLOWED);
            }
        }

        CURRENT:
        {
            if (packet.getCurrentAppConsent() == ConnectConsent.PENDING && packet.getTargetAppConsent() == ConnectConsent.ALLOWED) {
                User handle = CoreProvider.Cache.Local.USERS.provide().get(packet.getUserId());

                Player player = Bukkit.getPlayerExact(handle.getNick());

                if (player != null && player.isOnline()) {
                    changed = true;

                    AsyncUserConnectHandShakeEvent event = new AsyncUserConnectHandShakeEvent(handle, ConnectSide.CURRENT, packet);
                    Bukkit.getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        packet.setTargetAppConsent(ConnectConsent.DENIED);
                        break CURRENT;
                    }

                    packet.setCurrentAppConsent(ConnectConsent.ALLOWED);
                }
            }
        }

        if (changed) {
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
