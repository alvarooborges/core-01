package net.hyze.core.spigot.commands.impl;

import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.commands.CommandRestriction;
import net.hyze.core.shared.misc.utils.NumberUtils;
import net.hyze.core.shared.servers.Server;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.commands.CustomCommand;
import net.hyze.core.spigot.misc.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class OnlineCommand extends CustomCommand {

    public OnlineCommand() {
        super("online", CommandRestriction.IN_GAME, "list");
    }

    @Override
    public void onCommand(CommandSender sender, User user, String[] args) {

        Server server = CoreProvider.getApp().getServer();

        if (server == null) {
            Message.SUCCESS.send(sender, String.format("\n&eJogadores online: &a%s.\n ", NumberUtils.format(Bukkit.getOnlinePlayers().size())));
            return;
        }

        long online = CoreProvider.Cache.Local.SERVERS.provide().getOnlineCount(server);

        Message.SUCCESS.send(sender, String.format("\n&e%s: &a%s jogadores online. \n ", server.getDisplayName(), NumberUtils.format(online)));

    }

}
