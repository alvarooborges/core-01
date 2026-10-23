package net.hyze.core.bungee.commands.impl.reload.subcommands;

import net.hyze.core.bungee.commands.CustomCommand;
import net.hyze.core.bungee.messages.Message;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.echo.packets.config.ConfigReloadPacket;
import net.hyze.core.shared.echo.packets.config.ServersReloadPacket;
import net.md_5.bungee.api.CommandSender;

public class ReloadServersSubCommand extends CustomCommand {

    public ReloadServersSubCommand() {
        super("servers", "s");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        
        CoreProvider.Redis.ECHO.provide().publish(new ServersReloadPacket());
        Message.INFO.send(sender, "Servers regarregados em todas as aplicações.");
        
    }

}
