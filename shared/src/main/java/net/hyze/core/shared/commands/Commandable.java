package net.hyze.core.shared.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import io.sentry.Sentry;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.CoreWrapper;
import net.hyze.core.shared.commands.argument.Argument;
import net.hyze.core.shared.commands.argument.ArgumentValidationResponse;
import net.hyze.core.shared.commands.argument.impl.NickArgument;
import net.hyze.core.shared.misc.utils.TextUtil;
import net.hyze.core.shared.user.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bson.Document;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface Commandable<T> {

    String getName0();

    Set<String> getAliases0();

    CommandRestriction getCommandRestriction();

    <S extends Commandable<T>> Map<S, String> getSubCommands();

    LinkedList<Argument> getArguments();

    boolean isConsole(T sender);

    boolean isPlayer(T sender);

    String getSenderNick(T sender);

    default void beforeCommand(String label) {
    }

    default void afterCommand(String label) {
    }

    default void onCommand(T sender, User user, String label, String[] args) {
        onCommand(sender, user, args);
    }

    default void onCommand(T sender, User user, String[] args) {
        onCommand(sender, args);
    }

    default void onCommand(T sender, String[] args) {
        sendSubCommandUsage(sender);
    }

    default void sendSubCommandUsage(T sender) {

        User user = CoreProvider.Cache.Local.USERS.provide().getIfPresent(getSenderNick(sender));

        if (!getSubCommands().isEmpty()) {

            ComponentBuilder builder = new ComponentBuilder("\nComandos disponíveis:")
                    .color(net.md_5.bungee.api.ChatColor.GREEN);

            for (Map.Entry<Commandable<T>, String> entry : getSubCommands().entrySet()) {

                Commandable<T> subCommand = entry.getKey();
                String description = entry.getValue();

                if (subCommand instanceof CommandRestrictable) {
                    CommandRestrictable restrictable = (CommandRestrictable) subCommand;

                    if (user == null || !restrictable.canExecute(user)) {
                        continue;
                    }
                }

                builder.append("\n/", ComponentBuilder.FormatRetention.NONE)
                        .color(ChatColor.YELLOW)
                        .append(getName0())
                        .append(" ")
                        .append(subCommand.getName0());

                if (!subCommand.getArguments().isEmpty()) {

                    for (Argument argument : subCommand.getArguments()) {

                        builder.append(" ", ComponentBuilder.FormatRetention.NONE)
                                .append(argument.getName())
                                .color(ChatColor.GRAY);

                        if (argument.getDescription() != null) {
                            builder.event(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    new ComponentBuilder(argument.getDescription()).color(ChatColor.YELLOW).create()
                            ));
                        }
                    }
                }

                if (description != null && !description.isEmpty()) {
                    builder.append(" - ", ComponentBuilder.FormatRetention.NONE)
                            .append(description)
                            .color(ChatColor.WHITE);

                }
            }

            builder.append("\n ");

            CoreWrapper.getWrapper().sendMessage(getSenderNick(sender), builder.create());
        }
    }

    default ComponentBuilder getUsage(T sender, String label) {
        ComponentBuilder cb = new ComponentBuilder("Utilize: /")
                .color(net.md_5.bungee.api.ChatColor.RED)
                .append(label);

        getArguments().forEach(argument -> {
            cb.append(" ")
                    .reset()
                    .append(argument.getName())
                    .color(net.md_5.bungee.api.ChatColor.RED)
                    .event(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder(argument.getDescription()).color(net.md_5.bungee.api.ChatColor.YELLOW).create()
                    ));
        });

        return cb;
    }

    default String getUsageWithoutSlash(String label, String commandDescription) {

        StringBuilder sb = new StringBuilder(" ")
                .append(ChatColor.YELLOW)
                .append(label)
                .append(" ");

        for (Argument argument : getArguments()) {
            sb.append(ChatColor.GRAY)
                    .append(argument.getName());
        }

        sb.append(ChatColor.DARK_GRAY)
                .append(" - ")
                .append(ChatColor.GRAY)
                .append(commandDescription);

        return sb.toString();
    }

    default void executeRaw(T sender, String label, String[] args) {

        if (getCommandRestriction() != null) {
            if (getCommandRestriction().equals(CommandRestriction.CONSOLE) && !isConsole(sender)) {
                CoreWrapper.getWrapper().sendMessage(getSenderNick(sender), ChatColor.RED + "Este comando só pode ser usado pelo console.");
                return;
            }

            if (getCommandRestriction().equals(CommandRestriction.IN_GAME) && !isPlayer(sender)) {
                CoreWrapper.getWrapper().sendMessage(getSenderNick(sender), ChatColor.RED + "Este comando só pode ser usado por jogadores.");
                return;
            }
        }

        User user = null;

        if (isPlayer(sender)) {

            user = CoreProvider.Cache.Local.USERS.provide().get(getSenderNick(sender));

            if (user == null) {
                CoreWrapper.getWrapper().sendMessage(getSenderNick(sender), ChatColor.RED + "Você não está registrado.");
                return;
            }

            if (!"login".equalsIgnoreCase(getName0())) {
                if (!user.isLogged()) {
                    CoreWrapper.getWrapper().sendMessage(getSenderNick(sender), ChatColor.RED + "Você não está logado.");
                    return;
                }
            }

            if (this instanceof CommandRestrictable) {
                CommandRestrictable restrictable = (CommandRestrictable) this;

                /*
                 * Caso o jogador não tenha permissão, negar aqui.
                 */
                if (!restrictable.canExecute(user)) {
                    CoreWrapper.getWrapper().sendMessage(getSenderNick(sender), ChatColor.RED + restrictable.getErrorMessage());
                    return;
                }
            }
        }

        // Documento para log
        Document document = new Document();

        if (user != null) {
            document.put("user_id", user.getId());
        }

        String rawLabel = label.trim().replaceAll(" +", " ");
        String rawArgs = String.join(" ", args).trim().replaceAll(" +", " ");
        String rawCmd = rawLabel + (!rawArgs.isEmpty() ? (" " + rawArgs) : "");

        document.put("label", label);
        document.put("base_label", label.split(" ")[0]);
        document.put("executed_at", new Date());
        document.put("app_id", CoreProvider.getApp().getId());

        if (CoreProvider.getApp().getServer() != null) {
            document.put("server_id", CoreProvider.getApp().getServer().getId());
        }

        {
            Set<String> blackList = Sets.newHashSet();
            blackList.add("login");
            blackList.add("trocarsenha");
            blackList.add("mudarsenha");
            blackList.add("register");
            blackList.add("registrar");

            if (!blackList.contains(label)) {
                document.put("args", Arrays.asList(args));
                document.put("raw", rawCmd);
            }
        }

        try {
            if (args.length > 0) {
                Commandable<T> subCommand = getSubCommands().keySet()
                        .stream()
                        .filter(sub -> sub.getName0().equalsIgnoreCase(args[0]) || sub.getAliases0().stream().anyMatch(alias -> alias.equalsIgnoreCase(args[0])))
                        .findFirst()
                        .orElse(null);

                if (subCommand != null) {
                    subCommand.executeRaw(sender, String.format("%s %s", label, subCommand.getName0()), Arrays.copyOfRange(args, 1, args.length));
                    return;
                }
            }

            Map<Argument, String> mapOfArgs = IntStream
                    .range(0, getArguments().size())
                    .boxed()
                    .collect(Collectors.toMap(
                            i -> getArguments().get(i),
                            i -> {
                                if (i < args.length) {
                                    return args[i];
                                }

                                return "";
                            }
                    ));

            for (Map.Entry<Argument, String> entry : mapOfArgs.entrySet()) {
                Argument arg = entry.getKey();
                String argValue = entry.getValue();

                if (argValue.isEmpty() && arg.isRequired()) {
                    CoreWrapper.getWrapper().sendMessage(getSenderNick(sender), getUsage(sender, label).create());
                    return;
                }

                if (!argValue.isEmpty()) {
                    ArgumentValidationResponse validationResponse = arg.validate(entry.getValue());

                    if (!validationResponse.isSuccess()) {

                        BaseComponent[] errorMessage = Optional
                                .ofNullable(validationResponse.getErrorMessage())
                                .orElse(arg.getErrorMessage(argValue));

                        CoreWrapper.getWrapper().sendMessage(getSenderNick(sender),
                                new ComponentBuilder("")
                                        .color(ChatColor.RED)
                                        .append(errorMessage)
                                        .create()
                        );

                        return;
                    }
                }
            }

            this.beforeCommand(label);

            this.onCommand(sender, user, label, args);

            this.afterCommand(label);

            /*
             * Adicionando log de comando
             */
//            try {
//                MongoCollection<Document> collection = CoreProvider.Database.Mongo.MAIN.provide()
//                        .getConnection()
//                        .getCollection("chat_log");
//
//                collection.insertOne(document);
//            } catch (Exception exception) {
//                exception.printStackTrace();
//            }

        } catch (Exception exception) {
            exception.printStackTrace();

            CoreWrapper.getWrapper().sendMessage(getSenderNick(sender), ChatColor.RED + "Ocorreu um erro interno. Tente novamente mais tarde.");

            Sentry.captureException(exception);

            /*
             * Adicionando log de erro
             */
//            try {
//                MongoCollection<Document> collection = CoreProvider.Database.Mongo.MAIN.provide()
//                        .getConnection()
//                        .getCollection("chat_log_exceptions");
//
//                document.put("exception", asString);
//
//                collection.insertOne(document);
//            } catch (Exception e) {
//                exception.printStackTrace();
//            }
        }
    }

    default List<String> tabComplete0(T sender, String alias, String[] args) {
        if (CoreProvider.getApp().getServer() != null && args.length > 0 && this.getArguments().size() >= args.length) {
            int index = args.length - 1;
            String token = args[index];

            if (!token.isEmpty()) {
                Argument argument = this.getArguments().get(index);

                if (argument instanceof NickArgument) {

                    Set<User> users = CoreProvider.Cache.Local.USERS.provide().getOnlineUsersByServer(CoreProvider.getApp().getServer());

                    Set<String> nicks = new HashSet<>();

                    for (User user : users) {
                        String nick = user.getNick();
                        if (TextUtil.startsWithIgnoreCase(nick, args[index])) {
                            nicks.add(nick);
                        }
                    }

                    return TextUtil.copyPartialMatches(args[index], nicks, new ArrayList(nicks.size()));
                }
            }
        }

        return ImmutableList.of();
    }
}
