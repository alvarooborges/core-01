package net.hyze.core.spigot.commands.impl;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import net.hyze.core.shared.commands.CommandRestriction;
import net.hyze.core.shared.commands.GroupCommandRestrictable;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.commands.CustomCommand;
import net.hyze.core.spigot.misc.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;
import java.util.UUID;

public class AdminCommand extends CustomCommand implements GroupCommandRestrictable {

    public static final Map<UUID, Player> SCREENSHOT_CACHE= Maps.newHashMap();

    @Getter
    private final Group group = Group.GAME_MASTER;

    public AdminCommand() {
        super("admin", CommandRestriction.CONSOLE_AND_IN_GAME);

        registerSubCommand(new CustomCommand("cuboid", CommandRestriction.IN_GAME) {
            @Override
            public void onCommand(CommandSender sender, User user, String[] args) {
                Player player = (Player) sender;
                player.setMetadata("cuboid", new FixedMetadataValue(CoreSpigotPlugin.getInstance(), new CuboidSetup()));
                Message.SUCCESS.send(player, "Clique no primeiro canto seguido do segundo canto!");
            }
        });

        registerSubCommand(new CustomCommand("worlds") {
            @Override
            public void onCommand(CommandSender sender, User user, String[] args) {
                for(World world : Bukkit.getWorlds()) {
                    sender.sendMessage(world.getName());
                }
            }
        });
    }

    @Setter
    @Getter
    public static class CuboidSetup {

        private Location first, second;
    }
}
