package net.hyze.core.spigot.commands.impl;

import lombok.Getter;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.commands.CommandRestriction;
import net.hyze.core.shared.commands.GroupCommandRestrictable;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.commands.CustomCommand;
import net.hyze.core.spigot.misc.message.Message;
import org.bukkit.command.CommandSender;

public class MaintenanceCommand extends CustomCommand implements GroupCommandRestrictable {

    @Getter
    private Group group = Group.GAME_MASTER;

    public MaintenanceCommand() {
        super("manutencao", CommandRestriction.CONSOLE_AND_IN_GAME);
    }

    @Override
    public void onCommand(CommandSender sender, User user, String[] args) {

        if (args.length > 0) {
            boolean value = Boolean.parseBoolean(args[0]);
            CoreProvider.getApp().getStatus().setMaintenance(value);
        } else {
            CoreProvider.getApp().getStatus().setMaintenance(!CoreProvider.getApp().getStatus().isMaintenance());
        }

        Message.INFO.send(sender, "Manutenção definida para: " + CoreProvider.getApp().getStatus().isMaintenance());

    }
}
