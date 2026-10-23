package net.hyze.core.spigot.commands.impl;

import com.google.common.collect.Lists;
import java.io.File;

import net.hyze.core.shared.commands.CommandRestriction;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.messages.MessageUtils;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.commands.CustomCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PluginsCommand extends CustomCommand {

    public PluginsCommand() {
        super("plugins", CommandRestriction.CONSOLE_AND_IN_GAME, "pl", "version", "ver", "about");
    }

    @Override
    public void onCommand(CommandSender sender, User user, String[] args) {

        if (user == null || !user.hasGroup(Group.GAME_MASTER)) {
            sender.sendMessage(ChatColor.WHITE + "Plugins (1): " + ChatColor.GREEN + "Factions");
            return;
        }

        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();

        ComponentBuilder componentBuilder = new ComponentBuilder("\nPlugins (" + plugins.length + "): ")
                .color(ChatColor.WHITE);

        for (Plugin plugin : plugins) {

            ChatColor color;

            if (!plugin.isEnabled()) {
                color = ChatColor.RED;
            } else {

                color = ChatColor.GREEN;
            }

            componentBuilder
                    .append(plugin.getName())
                    .color(color)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(MessageUtils.translateColorCodes(
                            String.join("\n", Lists.newArrayList(
                                    color + plugin.getName(),
                                    "&fVersão: &7" + plugin.getDescription().getVersion(),
                                    "",
                                    color + "Clique para copiar!"
                            ))
                    ))))
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, plugin.getDescription().getVersion()));

            if (!plugin.equals(plugins[plugins.length - 1])) {
                componentBuilder
                        .append(", ")
                        .color(ChatColor.WHITE);
            }

        }

        componentBuilder
                .append(".\n")
                .color(ChatColor.WHITE);

        ((Player) sender).spigot().sendMessage(componentBuilder.create());
    }

}
