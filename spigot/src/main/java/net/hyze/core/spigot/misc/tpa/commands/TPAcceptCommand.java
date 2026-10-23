package net.hyze.core.spigot.misc.tpa.commands;

import com.google.common.collect.Maps;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.apps.App;
import net.hyze.core.shared.commands.CommandRestriction;
import net.hyze.core.shared.commands.argument.impl.NickArgument;
import net.hyze.core.shared.echo.packets.tpa.TPAcceptPacket;
import net.hyze.core.shared.misc.utils.Patterns;
import net.hyze.core.shared.misc.utils.Plural;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.commands.CustomCommand;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.tpa.TPAManager;
import net.hyze.core.spigot.misc.tpa.events.TPAcceptEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class TPAcceptCommand extends CustomCommand {

    private static final Map<String, BiPredicate<User, Integer>> FILTER_MAP = Maps.newHashMap();
    private static final Map<String, String> DESC_MAP = Maps.newHashMap();

    public TPAcceptCommand() {
        super("tpaccept", CommandRestriction.IN_GAME, "bring");

        registerArgument(new NickArgument("nick", "jogador que você deseja aceitar o pedido de teleporte"));

        for (Map.Entry<String, BiPredicate<User, Integer>> entry : FILTER_MAP.entrySet()) {
            registerSubCommand(new CustomCommand(entry.getKey().toLowerCase(), CommandRestriction.IN_GAME) {
                @Override
                public void onCommand(CommandSender sender, User target, String[] args) {
                    TPAcceptCommand.this.execute(sender, target, args, entry.getValue());
                }
            });
        }
    }

    @Override
    public ComponentBuilder getUsage(CommandSender sender, String label) {
        ComponentBuilder cb = super.getUsage(sender, label);

        for (Map.Entry<String, String> entry : DESC_MAP.entrySet()) {
            cb.append("\n")
                    .append("- /tpaccept " + entry.getKey().toLowerCase() + ": ")
                    .color(ChatColor.RED)
                    .append(entry.getValue())
                    .color(ChatColor.GRAY);
        }

        return cb;
    }

    @Override
    public void onCommand(CommandSender sender, User target, String[] args) {
        execute(sender, target, args, null);
    }

    private void execute(CommandSender sender, User target, String[] args, BiPredicate<User, Integer> filter) {
        if (filter != null) {
            Set<Integer> requests = TPAManager.getRequests(target.getId())
                    .stream()
                    .filter(requesterId -> filter.test(target, requesterId))
                    .limit(20)
                    .collect(Collectors.toSet());

            if (requests.isEmpty()) {
                Message.ERROR.send(sender, "Nenhum pedido de teleporte encontrado.");
                return;
            }

            long count = requests.stream()
                    .map(CoreProvider.Cache.Local.USERS.provide()::get)
                    .filter(Objects::nonNull)
                    .filter(requester -> {
                        TPAcceptEvent event = new TPAcceptEvent((Player) sender, requester, target);

                        Bukkit.getServer().getPluginManager().callEvent(event);

                        if (event.isCancelled()) {
                            return false;
                        }

                        CoreProvider.Redis.ECHO.provide().publish(new TPAcceptPacket(requester.getId(), target.getId()));

                        return true;
                    })
                    .count();

            Message.SUCCESS.send(sender, String.format(
                    "&aVocê aceitou %s %s de teleporte.",
                    count,
                    Plural.of(count, "pedido", "pedidos")
            ));

            return;
        }

        User requester;

        if (!Patterns.NICK.matches(args[0]) || (requester = CoreProvider.Cache.Local.USERS.provide().get(args[0])) == null) {
            Message.ERROR.send(sender, String.format("O jogador %s não foi encontrado.", args[0]));
            return;
        }

        if (!requester.isLogged()) {
            Message.ERROR.send(sender, String.format("O jogador %s não está online.", requester.getNick()));
            return;
        }

        if (!TPAManager.hasRequest(requester, target)) {
            Message.ERROR.send(sender, "Você não possui um pedido de teleporte deste jogador.");
            return;
        }

        App requesterApp = CoreProvider.Cache.Redis.USERS_STATUS.provide().getBukkitApp(requester.getNick());
        App targetApp = CoreProvider.Cache.Redis.USERS_STATUS.provide().getBukkitApp(target.getNick());

        if (!targetApp.isSameServer(requesterApp)) {
            Message.ERROR.send(sender, String.format("O jogador %s não está online.", requester.getNick()));
            return;
        }

        TPAcceptEvent event = new TPAcceptEvent((Player) sender, requester, target);

        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        Message.SUCCESS.send(sender, String.format("&aVocê aceitou o pedido de teleporte de %s.", requester.getNick()));
        CoreProvider.Redis.ECHO.provide().publish(new TPAcceptPacket(requester.getId(), target.getId()));
    }

    /**
     * Precisa ser chamado antes do registro/instancia do comando
     *
     * @param key - Sub comando
     * @param desc - Descrição do filtro
     * @param filter - Filtro que será aplicado
     */
    public static void registerFilter(String key, String desc, BiPredicate<User, Integer> filter) {
        FILTER_MAP.put(key, filter);

        if (desc != null) {
            DESC_MAP.put(key, desc);
        }
    }
}
