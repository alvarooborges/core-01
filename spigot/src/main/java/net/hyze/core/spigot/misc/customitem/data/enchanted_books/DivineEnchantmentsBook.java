package net.hyze.core.spigot.misc.customitem.data.enchanted_books;

import com.google.common.collect.Lists;
import net.hyze.core.shared.misc.utils.Pair;
import net.hyze.core.shared.misc.utils.WeightedCollection;
import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.misc.customitem.INonStackable;
import net.hyze.core.spigot.misc.enchantments.CustomEnchantment;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.InventoryUtils;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import net.hyze.core.spigot.misc.utils.ItemStackUtils;
import net.hyze.core.spigot.misc.utils.Title;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DivineEnchantmentsBook extends AbstractRandomEnchantmentsBook implements INonStackable {

    public static String KEY = "custom_item_divine_enchantments_book";

    private final List<Player> ACTIVES = Lists.newArrayList();

    public DivineEnchantmentsBook(Map<Pair<Enchantment, Integer>, Double> enchantments,
                                  Map<Pair<CustomEnchantment, Integer>, Double> customEnchantments) {

        super(KEY, enchantments, customEnchantments);
    }

    @Override
    public String getDisplayName() {
        return "&bLivro de Encantamentos Divinos";
    }

    @Subscribe
    public void on(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        WeightedCollection<Pair<?, Integer>> randomList = getRandomList(event.getItem());

        Player player = event.getPlayer();

        if (randomList.size() < 5) {
            Message.ERROR.send(player, " OPS! Não achamos os encantamentos :(");
            return;
        }

        if (ACTIVES.contains(player)) {
            Message.ERROR.send(player, "Você só pode abrir um livro por vez.");
            return;
        }

        ItemStack itemChecker = ItemBuilder.of(Material.BARRIER).name(UUID.randomUUID().toString()).make();

        if (!InventoryUtils.fits(player.getInventory(), itemChecker)) {
            Message.ERROR.send(player, "Seu inventário está cheio.");
            return;
        }

        Pair<?, Integer> pair = randomList.next().getObject();

        ItemStack reward;

        if (pair.getLeft() instanceof Enchantment) {
            Enchantment enchantment = (Enchantment) pair.getLeft();

            reward = new ItemStack(Material.ENCHANTED_BOOK);

            ItemStackUtils.addBookEnchantment(reward, enchantment, pair.getRight());

        } else if (pair.getLeft() instanceof CustomEnchantment) {
            CustomEnchantment enchantment = (CustomEnchantment) pair.getLeft();

            reward = enchantment.getBook(pair.getRight(), 1);
        } else {
            Message.ERROR.send(player, "Algo de errado aconteceu! Entre em contato com algum staff.");
            return;
        }

        String rewardName = getRewardName(pair);

        ItemStack inHand = player.getItemInHand();

        int delay = 1;

        List<String> names = randomList.stream().map(random -> getRewardName(random.getObject())).collect(Collectors.toList());

        Collections.shuffle(names);

        ACTIVES.add(player);

        new BukkitRunnable() {
            float run = 0;
            int speed = 2;
            int nameIndex = 0;

            @Override
            public void cancel() {
                super.cancel();
                ACTIVES.remove(player);
            }

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (run / 20 == 5) {
                    boolean success = false;

                    if (InventoryUtils.fits(player.getInventory(), itemChecker)) {
                        boolean subtract = InventoryUtils.subtractOne(player, inHand);

                        if (subtract) {
                            player.getInventory().addItem(reward);

                            try {
                                Title.builder()
                                        .fadeIn(0)
                                        .subTitle("&b" + rewardName)
                                        .stay(20 * 3)
                                        .fadeOut(20 * 3)
                                        .build()
                                        .send(player);
                            } catch (Exception ignored) {

                            }

                            Message.SUCCESS.send(player, "Você ganhou 1x Livro " + rewardName + ".");
                            success = true;

                            player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_BLAST, 5, 0);

                            Bukkit.getScheduler().runTaskAsynchronously(CoreSpigotPlugin.getInstance(), () -> {
                                for (int i = 0; i < 50; i++) {
                                    player.getWorld().playEffect(player.getLocation().clone().add(0, 1, 0), Effect.FIREWORKS_SPARK, 1, 5);
                                }
                            });
                        }
                    }

                    if (!success) {
                        Message.ERROR.send(player, "Algo de errado aconteceu!");
                    }

                    cancel();
                    return;
                }

                if (run % speed == 0) {
                    if (nameIndex >= names.size()) {
                        nameIndex = 0;
                    }

                    try {
                        Title.builder()
                                .fadeIn(0)
                                .stay(20)
                                .fadeOut(0)
                                .subTitle("&b" + names.get(nameIndex))
                                .build()
                                .send(player);
                    } catch (Exception ignored) {
                    }

                    nameIndex++;

                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
                    speed += 2;
                }

                run += delay;
            }
        }.runTaskTimer(CoreSpigotPlugin.getInstance(), 0, delay);
    }
}
