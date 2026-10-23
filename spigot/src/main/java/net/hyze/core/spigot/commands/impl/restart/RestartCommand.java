package net.hyze.core.spigot.commands.impl.restart;

import net.hyze.core.shared.commands.CommandRestriction;
import net.hyze.core.shared.commands.GroupCommandRestrictable;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.commands.CustomCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RestartCommand extends CustomCommand implements GroupCommandRestrictable {

    private RestartInventory inventory;

    public RestartCommand() {
        super("reiniciar", CommandRestriction.CONSOLE_AND_IN_GAME, "restart");

//        registerArgument(new Argument("sendServer", "Servidor para onde os jogadores serão enviados.", false));
//        registerArgument(new Argument("seconds", "Tempo em segundos para reiniciar...", false));
    }

    @Override
    public void onCommand(CommandSender sender, User user, String[] args) {


        if (this.inventory == null) {
            this.inventory = new RestartInventory();
        }

        ((Player) sender).openInventory(this.inventory);

//        if (args.length == 1 && args[0].equalsIgnoreCase("menu")) {
//
//
//
//            return;
//        }
//
//        App appRaw = null;
//
//        if (args.length >= 1) {
//
//            String appId = args[0];
//
//            appRaw = CoreProvider.Cache.Local.APPS.provide().get(appId);
//
//            if (appRaw == null) {
//                AppStatus status = CoreProvider.Cache.Redis.APPS_STATUS.provide().fetch(appId, AppStatus.class);
//
//                if (status == null) {
//                    Message.ERROR.send(sender, String.format("O servidor \"%s\" não existe.", appId));
//                    return;
//                }
//
//                appRaw = new App(status.getAppId(), "", status.getType(), status.getAddress(), status.getServer());
//            }
//
//            if (appRaw.getType().equals(AppType.PROXY)) {
//                Message.ERROR.send(sender, "Ops, a aplicação informada é do tipo Proxy.");
//                return;
//            }
//
//        }
//
//        int countdownValueRaw = 60;
//
//        if (args.length >= 2) {
//            if (Ints.tryParse(args[1]) == null) {
//                Message.ERROR.send(sender, "Ops, o valor informado não é um número!");
//                return;
//            }
//            countdownValueRaw = Ints.tryParse(args[1]);
//        }
//
//        int countdownValue = countdownValueRaw;
//        App app = appRaw;
//        String subTitle = "&e&lEM %s SEGUNDOS";
//        AtomicInteger countdown = new AtomicInteger(countdownValue);
//
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//
//                if (countdown.get() == countdownValue || (countdown.get() > 0 && countdown.get() <= 10)) {
//                    Bukkit.broadcastMessage(
//                            MessageUtils.translateColorCodes(String.format("\n&eEste servidor será reiniciado em %s segundos!\n ", countdown.get()))
//                    );
//                }
//
//                int count_ = countdown.getAndDecrement();
//
//                if (count_ == 0) {
//
//                    Bukkit.getOnlinePlayers().forEach(player -> {
//
//                        if (app != null) {
//                            User targetUser = CoreProvider.Cache.Local.USERS.provide().get(player.getName());
//                            Message.SUCCESS.send(player, "Conectando-se...");
//                            CoreProvider.Redis.ECHO.provide().publish(new UserConnectHandShakePacket(targetUser, app, ConnectReason.PLUGIN));
//                        } else {
//                            player.kickPlayer(ChatColor.RED + "Reiniciando servidor...");
//                        }
//
//                    });
//
//                    return;
//                }
//
//                if (count_ <= -5) {
//                    Bukkit.shutdown();
//                    this.cancel();
//                }
//
//                Title title = new Title()
//                        .stay(40)
//                        .title("&e&lREINICIANDO")
//                        .subTitle(String.format(subTitle, count_));
//
//                Bukkit.getOnlinePlayers().forEach(player -> {
//                    title.send(player);
//                    player.playSound(player.getLocation(), Sound.FIRE_IGNITE, 1, 1);
//                });
//
//            }
//        }.runTaskTimer(CoreSpigotPlugin.getInstance(), 20L, 20L);
    }

    @Override
    public Group getGroup() {
        return Group.GAME_MASTER;
    }

}
