package net.hyze.core.spigot.misc.tpa.commands;

import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.apps.App;
import net.hyze.core.shared.commands.CommandRestriction;
import net.hyze.core.shared.commands.argument.impl.NickArgument;
import net.hyze.core.shared.echo.packets.tpa.TPAPacket;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.user.User;
import net.hyze.core.shared.user.preferences.PreferenceStatus;
import net.hyze.core.shared.user.preferences.UserPreference;
import net.hyze.core.spigot.commands.CustomCommand;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.preference.CorePreference;
import net.hyze.core.spigot.misc.tpa.TPAManager;
import net.hyze.core.spigot.misc.tpa.events.TPAEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPACommand extends CustomCommand {

    public TPACommand() {
        super("tpa", CommandRestriction.IN_GAME);

        registerArgument(new NickArgument("nick", "jogador que você deseja enviar o pedido de teleporte"));
    }

    @Override
    public void onCommand(CommandSender sender, User requester, String[] args) {
        User target = CoreProvider.Cache.Local.USERS.provide().get(args[0]);

        if (target == null) {
            Message.ERROR.send(sender, String.format("O jogador %s não foi encontrado.", args[0]));
            return;
        }

        UserPreference preferences = CoreProvider.Cache.Local.USERS_PREFERENCES.provide().get(target);

        if (preferences.getPreference(CorePreference.TPA.name()).is(PreferenceStatus.OFF)) {
            Message.ERROR.send(sender, "O jogador desabilitou pedidos de tpa!");
            return;
        }

        if (!target.isLogged()) {
            Message.ERROR.send(sender, String.format("O jogador %s não está online.", target.getNick()));
            return;
        }

        App requesterApp = CoreProvider.Cache.Redis.USERS_STATUS.provide().getBukkitApp(requester.getNick());
        App targetApp = CoreProvider.Cache.Redis.USERS_STATUS.provide().getBukkitApp(target.getNick());

        if (requesterApp == null) {
            Message.ERROR.send(sender, "Tente novamente mais tarde.");
            return;
        }

        if (!requesterApp.isSameServer(targetApp)) {
            Message.ERROR.send(sender, String.format("O jogador %s não está online.", target.getNick()));
            return;
        }

        if (TPAManager.hasRequest(requester, target)) {
            Message.ERROR.send(sender, String.format("Você já possui um pedido de teleporte pendente para o jogador %s.", target.getNick()));
            return;
        }

        if (target.equals(requester)) {
            Message.ERROR.send(sender, "Você já está onde você está.");
            return;
        }

        TPAEvent event = new TPAEvent(requester, target);

        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        CoreProvider.Redis.ECHO.provide().publish(new TPAPacket(requester.getId(), target.getId()));

        ComponentBuilder builder = new ComponentBuilder("\nVocê enviou um pedido de teleporte para ").color(ChatColor.YELLOW);

        Group group = target.getHighestGroup();

        if (group.isHigher(Group.DEFAULT)) {
            builder.append(group.getDisplayTag())
                    .append(" ");
        }

        builder.append(target.getNick())
                .color(group.getColor())
                .append("\n")
                .append("Clique").color(ChatColor.YELLOW)
                .append(" AQUI").color(ChatColor.RED)
                .bold(true)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpcancel " + target.getNick()))
                .append(" para cancelar.").color(ChatColor.YELLOW)
                .bold(false)
                .append("\n");

        ((Player) sender).sendMessage(builder.create());
    }
}
