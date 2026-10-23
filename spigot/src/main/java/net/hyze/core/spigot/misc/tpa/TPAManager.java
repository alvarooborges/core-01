package net.hyze.core.spigot.misc.tpa;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import dev.utils.echo.IEchoListener;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.echo.packets.tpa.TPAPacket;
import net.hyze.core.shared.echo.packets.tpa.TPAcceptPacket;
import net.hyze.core.shared.echo.packets.tpa.TPCancelPacket;
import net.hyze.core.shared.echo.packets.tpa.TPDenyPacket;
import net.hyze.core.shared.echo.packets.user.connect.ConnectReason;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.messages.MessageUtils;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.TeleportManager;
import net.hyze.core.spigot.world.location.unserializer.BukkitLocationParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.greenrobot.eventbus.Subscribe;

import java.util.Set;

public class TPAManager implements IEchoListener {

    private static final Multimap<Integer, Integer> REQUESTS = ArrayListMultimap.create();

    @Subscribe
    public void on(TPAPacket packet) {
        REQUESTS.put(packet.getTargetId(), packet.getRequesterId());

        Bukkit.getScheduler().runTaskLater(CoreSpigotPlugin.getInstance(), () -> {
            REQUESTS.remove(packet.getTargetId(), packet.getRequesterId());
        }, 60 * 20);

        User targetUser = CoreProvider.Cache.Local.USERS.provide().getIfPresent(packet.getTargetId());

        if (targetUser == null) {
            return;
        }

        Player targetPlayer = Bukkit.getPlayerExact(targetUser.getNick());

        if (targetPlayer != null && targetPlayer.isOnline()) {

            User requesterUser = CoreProvider.Cache.Local.USERS.provide().getIfPresent(packet.getRequesterId());

            Group group = requesterUser.getHighestGroup();

            ComponentBuilder builder = new ComponentBuilder("\n");

            if (group.isHigher(Group.DEFAULT)) {
                builder.append("[")
                        .color(group.getColor())
                        .append(group.getTagStriped())
                        .append("] ");
            }

            builder.color(group.getColor())
                    .append(requesterUser.getNick())
                    .append(" lhe enviou um pedido de teletransporte!").color(ChatColor.YELLOW)
                    .append("\n")
                    .append("Clique").color(ChatColor.YELLOW)
                    .append(" AQUI").color(ChatColor.GREEN).bold(true)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + requesterUser.getNick()))
                    .event(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new BaseComponent[]{
                                    new TextComponent(MessageUtils.translateColorCodes(String.format(
                                            "&aClique e aceite o pedido \n&ade teletransporte de %s.", requesterUser.getNick()
                                    )))
                            }
                    ))
                    .append(" para aceitar ou").bold(false).color(ChatColor.YELLOW)
                    .append(" AQUI").color(ChatColor.RED).bold(true)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + requesterUser.getNick()))
                    .append(" para negar.").bold(false).color(ChatColor.YELLOW)
                    .append("\n");

            targetPlayer.spigot().sendMessage(builder.create());
        }
    }

    @Subscribe
    public void on(TPDenyPacket packet) {
        REQUESTS.remove(packet.getTargetId(), packet.getRequesterId());

        User requesterUser = CoreProvider.Cache.Local.USERS.provide().getIfPresent(packet.getRequesterId());

        if (requesterUser == null) {
            return;
        }

        Player requesterPlayer = Bukkit.getPlayerExact(requesterUser.getNick());

        if (requesterPlayer != null && requesterPlayer.isOnline()) {
            User targetUser = CoreProvider.Cache.Local.USERS.provide().getIfPresent(packet.getTargetId());

            Message.INFO.send(requesterPlayer, "Pedido de teleporte para " + targetUser.getNick() + " negado.");
        }
    }

    @Subscribe
    public void on(TPCancelPacket packet) {
        REQUESTS.remove(packet.getTargetId(), packet.getRequesterId());

        User targetUser = CoreProvider.Cache.Local.USERS.provide().getIfPresent(packet.getTargetId());

        if (targetUser == null) {
            return;
        }

        Player targetPlayer = Bukkit.getPlayerExact(targetUser.getNick());

        if (targetPlayer != null && targetPlayer.isOnline()) {
            User requesterUser = CoreProvider.Cache.Local.USERS.provide().get(packet.getRequesterId());
            Message.INFO.send(targetPlayer, "Pedido de teleporte cancelado por " + requesterUser.getNick() + ".");
        }
    }

    @Subscribe
    public void on(TPAcceptPacket packet) {
        REQUESTS.remove(packet.getTargetId(), packet.getRequesterId());

        User targetUser = CoreProvider.Cache.Local.USERS.provide().getIfPresent(packet.getTargetId());

        if (targetUser == null) {
            return;
        }

        Player target = Bukkit.getPlayerExact(targetUser.getNick());

        if (target != null && target.isOnline()) {
            User requesterUser = CoreProvider.Cache.Local.USERS.provide().get(packet.getRequesterId());

            TeleportManager.teleport(
                    requesterUser,
                    BukkitLocationParser.serialize(target.getLocation()),
                    ConnectReason.TPA,
                    TextComponent.fromLegacyText(ChatColor.GREEN + String.format("Pedido de teleporte aceito por %s.", targetUser.getNick()))
            );
        }
    }

    public static Set<Integer> getRequests(Integer targetId) {
        return ImmutableSet.copyOf(REQUESTS.get(targetId));
    }

    public static boolean hasRequest(User requester, User target) {
        return hasRequest(requester.getId(), target.getId());
    }

    public static boolean hasRequest(Integer requesterId, Integer targetId) {
        return REQUESTS.containsEntry(targetId, requesterId);
    }
}
