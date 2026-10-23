package net.hyze.core.spigot.misc.modreq.echo.listeners;

import dev.utils.echo.IEchoListener;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.group.Group;
import net.hyze.core.spigot.misc.modreq.ModreqManager;
import net.hyze.core.spigot.misc.modreq.echo.packets.ModreqRequestEchoPacket;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

public class ModreqEchoListeners implements IEchoListener {

    @Subscribe
    public void on(ModreqRequestEchoPacket packet) {

        /**
         * pra tocar o som.
         */
        CoreProvider.Cache.Local.USERS.provide().getOnlineUsersByServer(CoreProvider.getApp().getServer())
                .stream()
                .filter(user -> user.hasGroup(Group.MODERATOR))
                .map(user -> Bukkit.getPlayerExact(user.getNick()))
                .filter(Objects::nonNull)
                .forEach(player -> player.playSound(player.getLocation(), Sound.FIZZ, 1F, 1F));

        ModreqManager.REQUESTS.put(packet.getUserId(), System.currentTimeMillis());
    }
}
