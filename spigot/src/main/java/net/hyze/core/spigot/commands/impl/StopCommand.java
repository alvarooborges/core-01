package net.hyze.core.spigot.commands.impl;

import com.google.common.primitives.Ints;
import lombok.Getter;
import net.hyze.core.shared.commands.CommandRestriction;
import net.hyze.core.shared.commands.GroupCommandRestrictable;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.commands.CustomCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class StopCommand extends CustomCommand implements GroupCommandRestrictable {

    @Getter
    private final Group group = Group.GAME_MASTER;

    public StopCommand() {
        super("stop", CommandRestriction.CONSOLE_AND_IN_GAME);
    }

    @Override
    public void onCommand(CommandSender sender, User user, String[] args) {
        int countdown = 30;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("now")) {
                if (CoreSpigotPlugin.getInstance().getStopManager().getTask() != null) {
                    CoreSpigotPlugin.getInstance().getStopManager().getTask().cancel();
                }

                Bukkit.shutdown();
                return;
            }

            if (Ints.tryParse(args[0]) != null) {
                countdown = Math.min(Math.max(10, Ints.tryParse(args[0])), 60);
            }
        }

        CoreSpigotPlugin.STOP_COUNTDOWN = countdown;
        CoreSpigotPlugin.getInstance().getStopManager().runStop(sender);
    }
}
