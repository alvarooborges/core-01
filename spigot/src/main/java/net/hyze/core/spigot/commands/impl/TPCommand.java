package net.hyze.core.spigot.commands.impl;

import lombok.Getter;
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
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPCommand extends CustomCommand implements GroupCommandRestrictable {

    @Getter
    private Group group = Group.MANAGER;

    public TPCommand() {
        super("tp", CommandRestriction.IN_GAME);

        registerArgument(new NickArgument("nick", "Nick do jogador que será procurado", true));
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

        if (args.length == 2 && args[1].equalsIgnoreCase("alert") && user.hasGroup(Group.MANAGER)) {
            ((Player) sender).setGameMode(GameMode.SPECTATOR);
        }

//        Teleporter.builder()
//                .toUser(targetUser)
//                .welcomeMessage(new ComponentBuilder(String.format(
//                        "Você foi teleportado até %s.",
//                        targetUser.getNick()
//                )).color(ChatColor.GREEN).create())
//                .reason(ConnectReason.PLUGIN)
//                .build()
//                .teleport(user);

        TeleportManager.teleport(
                user,
                targetUser,
                ConnectReason.TPA,
                TextComponent.fromLegacyText(ChatColor.GREEN + String.format("Teleportado até %s.", targetUser.getNick()))
        );
    }
}
