package net.hyze.core.spigot.misc.modreq.commands;

import lombok.Getter;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.commands.CommandRestriction;
import net.hyze.core.shared.commands.GroupCommandRestrictable;
import net.hyze.core.shared.commands.argument.impl.NickArgument;
import net.hyze.core.shared.echo.packets.user.connect.ConnectReason;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.misc.utils.DefaultMessage;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.commands.CustomCommand;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.modreq.ModreqManager;
import net.hyze.core.spigot.misc.utils.TeleportManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

public class ModreqTpCommand extends CustomCommand implements GroupCommandRestrictable {

    @Getter
    private Group group = ModreqManager.STAFF_GROUP;

    public ModreqTpCommand() {
        super("modreqtp", CommandRestriction.IN_GAME);

        registerArgument(new NickArgument("jogador", "jogador alvo", true));
    }

    @Override
    public void onCommand(CommandSender sender, User user, String[] args) {
        User target = CoreProvider.Cache.Local.USERS.provide().get(args[0]);

        if (target == null) {
            Message.ERROR.send(sender, DefaultMessage.PLAYER_NOT_FOUND.format(args[0]));
            return;
        }

        Long at = ModreqManager.REQUESTS.get(target.getId());

        if (at == null || at < System.currentTimeMillis() - ModreqManager.DELAY) {
            Message.ERROR.send(sender, "Solicitação não encontrada ou expirada.");
            return;
        }

        TeleportManager.teleport(
                user,
                target,
                ConnectReason.PLUGIN,
                TextComponent.fromLegacyText(ChatColor.GREEN + "Você foi teleportado até o jogador " + args[0])
        );
    }
}
