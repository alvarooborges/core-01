package net.hyze.core.spigot.commands;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.commands.CommandRestriction;
import net.hyze.core.shared.commands.Commandable;
import net.hyze.core.shared.commands.argument.Argument;
import net.hyze.core.shared.misc.utils.DefaultMessage;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.commands.events.ExecuteCustomCommandEvent;
import net.hyze.core.spigot.misc.combat.CombatManager;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.BlockedWhenRestarting;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class CustomCommand extends Command implements Commandable<CommandSender> {

    private final Map<String, Timing> timings = Maps.newConcurrentMap();

    @Getter
    protected final String name0;

    @Getter
    protected final Set<String> aliases0;

    @Getter
    @Setter
    protected String description0;

    @Getter
    protected final CommandRestriction commandRestriction;

    @Getter
    protected final Map<CustomCommand, String> subCommands = Maps.newConcurrentMap();

    @Getter
    protected final LinkedList<Argument> arguments = Lists.newLinkedList();

    public CustomCommand(String name, CommandRestriction commandRestriction, String... aliases) {
        super(name);

        this.name0 = name;
        this.aliases0 = Sets.newHashSet(aliases);
        this.commandRestriction = commandRestriction;

        if (aliases.length > 0) {
            setAliases(Lists.newArrayList(aliases));
        }
    }

    public CustomCommand(String name, String... aliases) {
        this(name, CommandRestriction.CONSOLE_AND_IN_GAME, aliases);
    }

    @Override
    public boolean isConsole(CommandSender sender) {
        return sender instanceof ConsoleCommandSender;
    }

    @Override
    public boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    @Override
    public String getSenderNick(CommandSender sender) {
        return sender.getName();
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (this instanceof BlockedWhenRestarting) {
            if (CoreSpigotPlugin.getInstance().getStopManager().isStopping()) {
                Message.ERROR.send(sender, "O servidor está reiniciando.");
                return true;
            }
        }

        if (sender instanceof Player && !CombatManager.ALLOWED_COMMANDS.contains(label)) {
            User user = CoreProvider.Cache.Local.USERS.provide().get(sender.getName());

            if (CombatManager.isTagged(user)) {
                Message.ERROR.sendDefault(sender, DefaultMessage.COMBAT_COMMAND_ERROR);
                return true;
            }
        }

        ExecuteCustomCommandEvent event = new ExecuteCustomCommandEvent(sender, this, label, args);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return true;
        }


        this.executeRaw(sender, label, args);


        return true;
    }

    @Override
    public void beforeCommand(String label) {
        Timing timing = timings.getOrDefault(
                CustomCommand.this.getClass().getName() + "_" + label,
                Timings.of(
                        CoreSpigotPlugin.getInstance(),
                        "CustomCommand: " + CustomCommand.this.getClass().getName() + " (" + label + ")"
                )
        );

        timing.startTimingIfSync();
    }

    @Override
    public void afterCommand(String label) {
        Timing timing = timings.getOrDefault(
                CustomCommand.this.getClass().getName() + "_" + label,
                Timings.of(
                        CoreSpigotPlugin.getInstance(),
                        "CustomCommand: " + CustomCommand.this.getClass().getName() + " (" + label + ")"
                )
        );

        timing.stopTimingIfSync();
    }

    public final void registerSubCommand(CustomCommand subCommand) {
        this.registerSubCommand(subCommand, new String());
    }

    public void registerSubCommand(CustomCommand subCommand, String description) {
        subCommand.setDescription0(description);

        this.subCommands.put(subCommand, description);
    }

    public final void unregisterSubCommand(String label) {
        this.subCommands.keySet().removeIf(sub -> sub.name0.equals(label) || sub.aliases0.contains(label));
    }

    public final void registerArgument(Argument argument) {
        this.arguments.add(argument);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        CustomCommand subCommand = getSubCommands().keySet()
                .stream()
                .filter(sub -> sub.getName0().equalsIgnoreCase(args[0]) || sub.getAliases0().stream().anyMatch(a -> a.equalsIgnoreCase(args[0])))
                .findFirst()
                .orElse(null);

        if (subCommand != null) {
            return subCommand.tabComplete(sender, alias, Arrays.copyOfRange(args, 1, args.length));
        }

        return this.tabComplete0(sender, alias, args);
    }

    protected int getInteger(CommandSender sender, String value, int min, int max) {
        return getInteger(sender, value, min, max, false);
    }

    protected int getInteger(CommandSender sender, String value, int min, int max, boolean Throws) {
        int i = min;

        try {
            i = Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            if (Throws) {
                throw new NumberFormatException(String.format("%s is not a valid number", value));
            }
        }

        if (i < min) {
            i = min;
        } else if (i > max) {
            i = max;
        }

        return i;
    }
}
