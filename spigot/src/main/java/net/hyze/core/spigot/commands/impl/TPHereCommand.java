package net.hyze.core.spigot.commands.impl;

import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.commands.CommandRestriction;
import net.hyze.core.shared.commands.GroupCommandRestrictable;
import net.hyze.core.shared.commands.argument.impl.NickArgument;
import net.hyze.core.shared.echo.packets.user.connect.ConnectReason;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.commands.CustomCommand;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.TeleportManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

public class TPHereCommand extends CustomCommand implements GroupCommandRestrictable {

    public TPHereCommand() {
        super("tphere", CommandRestriction.IN_GAME);

        registerArgument(new NickArgument("nick", "Nick do jogador", true));
    }

    @Override
    public void onCommand(CommandSender sender, User user, String[] args) {

        User targetUser = CoreProvider.Cache.Local.USERS.provide().get(args[0]);

        if (targetUser == null) {
            Message.ERROR.send(sender, String.format("O jogador %s não foi encontrado.", args[0]));
            return;
        }

        if (!targetUser.isLogged()) {
            Message.ERROR.send(sender, String.format("O jogador %s não está online.", targetUser.getNick()));
            return;
        }

//        Teleporter.builder()
//                .toUser(user)
//                .welcomeMessage(new ComponentBuilder(String.format(
//                        "Você foi teleportado até %s.",
//                        user.getNick()
//                )).color(ChatColor.GREEN).create())
//                .reason(ConnectReason.PLUGIN)
//                .build()
//                .teleport(targetUser);

        TeleportManager.teleport(
                targetUser,
                user,
                ConnectReason.PLUGIN,
                TextComponent.fromLegacyText(ChatColor.GREEN + String.format("Você foi teleportado até %s.", targetUser.getNick()))
        );

    }

    @Override
    public Group getGroup() {
        return Group.MANAGER;
    }
}
