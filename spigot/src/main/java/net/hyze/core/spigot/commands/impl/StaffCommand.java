package net.hyze.core.spigot.commands.impl;

import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.commands.CommandRestriction;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.messages.MessageUtils;
import net.hyze.core.shared.servers.Server;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.commands.CustomCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffCommand extends CustomCommand {

    public StaffCommand() {
        super("staff", CommandRestriction.IN_GAME);
    }

    @Override
    public void onCommand(CommandSender sender, User user, String[] args) {

        Player player = (Player) sender;

        StringBuilder string = new StringBuilder();

        string.append("\n&fMembros da equipe online:\n ");

        CoreProvider.Cache.Local.USERS.provide()
                .getOnlineUsersByServer(Server.FACTIONS_ALPHA)
                .stream()
                .filter(target -> target.hasGroup(Group.HELPER))
                .forEach(
                        target -> {
                            string.append("&f- " + target.getHighestGroup().getDisplayTag(target.getNick()))
                                    .append("\n ");
                        }
                );

        player.sendMessage(MessageUtils.translateColorCodes(string.toString()));

    }

}
