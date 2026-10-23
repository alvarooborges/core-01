package net.hyze.core.spigot.commands.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.commands.CommandRestriction;
import net.hyze.core.shared.commands.GroupCommandRestrictable;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.misc.cooldowns.Cooldowns;
import net.hyze.core.shared.misc.cooldowns.redis.UserRedisCooldowns;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.commands.CustomCommand;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public class MeltCommand extends CustomCommand implements GroupCommandRestrictable {

    private static final String COOLDOWN_KEY = "MELT_COOLDOWN";

    /**
     * Tempo que o jogador tem que esperar para executar o comando novamente.
     */
    private static final Map<Group, Integer> COOLDOWNS = Maps.newHashMap();

    static {
        COOLDOWNS.put(Group.DIVINE, 60 * 5); // 5 minutos.
        COOLDOWNS.put(Group.ARCANE, 60 * 10); // 10 minutos.
        COOLDOWNS.put(Group.DEFAULT, 60 * 20); // 20 minutos.
    }

    @Getter
    private final Group group;

    private final HashMap<MaterialData, Result> map = Maps.newHashMap();

    private final BiFunction<User, List<Result>, Boolean> onPreMelt;

    public MeltCommand(Group group, BiFunction<User, List<Result>, Boolean> preMelt) {
        super("derreter", CommandRestriction.IN_GAME);
        this.group = group;
        this.onPreMelt = preMelt;

        map.put(new MaterialData(Material.IRON_ORE, (byte) 0), new Result(new MaterialData(Material.IRON_INGOT, (byte) 0), 1));
        map.put(new MaterialData(Material.GOLD_ORE, (byte) 0), new Result(new MaterialData(Material.GOLD_INGOT, (byte) 0), 1));
        map.put(new MaterialData(Material.REDSTONE_ORE, (byte) 0), new Result(new MaterialData(Material.REDSTONE, (byte) 0), 1));
        map.put(new MaterialData(Material.COAL_ORE, (byte) 0), new Result(new MaterialData(Material.COAL, (byte) 0), 1));
        map.put(new MaterialData(Material.DIAMOND_ORE, (byte) 0), new Result(new MaterialData(Material.DIAMOND, (byte) 0), 1));
        map.put(new MaterialData(Material.EMERALD_ORE, (byte) 0), new Result(new MaterialData(Material.EMERALD, (byte) 0), 1));
        map.put(new MaterialData(Material.LAPIS_ORE, (byte) 0), new Result(new MaterialData(Material.INK_SACK, (byte) 4), 1));
    }

    @Override
    public void onCommand(CommandSender sender, User user, String[] args) {

        if (!UserRedisCooldowns.hasEnded(user, COOLDOWN_KEY)) {
            String cooldown = Cooldowns.getFormattedTimeLeft(UserRedisCooldowns.getSecondsLeft(user, COOLDOWN_KEY) * 1000L);

            Message.ERROR.send(sender, String.format("Aguarde %s para derreter novamente.", cooldown));
            return;
        }

        Player player = (Player) sender;

        Inventory inventory = InventoryUtils.copy(player.getInventory());

        Set<MaterialData> checked = Sets.newHashSet();

        List<Result> results = Lists.newArrayList();

        for (ItemStack target : inventory.getContents()) {
            if (target == null || checked.contains(target.getData())) {
                continue;
            }

            checked.add(target.getData());

            Result targetResult = map.get(target.getData());
            if (targetResult == null) {
                continue;
            }

            int amountOfType = Arrays.stream(inventory.getContents())
                    .filter(Objects::nonNull)
                    .filter(item -> item.getData().equals(target.getData()))
                    .mapToInt(ItemStack::getAmount)
                    .sum();

            if (amountOfType < targetResult.getAmountNeeded()) {
                continue;
            }

            int leftOvers = Math.floorMod(amountOfType, targetResult.getAmountNeeded());
            int used = amountOfType - leftOvers;
            int resultProductAmount = used / targetResult.getAmountNeeded();

            int removed = 0;

            Inventory checkerInventory = InventoryUtils.copy(inventory);
            ItemStack[] checkerContents = checkerInventory.getContents();

            for (int i = 0; i < checkerContents.length; i++) {

                if (checkerContents[i] == null || !checkerContents[i].getData().equals(target.getData())) {
                    continue;
                }

                ItemStack content = checkerContents[i];

                if (content.getAmount() <= (used - removed)) {
                    checkerContents[i] = null;

                    removed += content.getAmount();

                    if (removed == used) {
                        break;
                    }

                    continue;
                }

                checkerContents[i].setAmount(content.getAmount() - (used - removed));
                break;
            }

            checkerInventory.setContents(checkerContents);

            ItemStack result = new ItemStack(
                    targetResult.getMaterialData().getItemType(),
                    resultProductAmount,
                    targetResult.getMaterialData().getData()
            );

            if (!InventoryUtils.fits(checkerInventory, result)) {
                continue;
            }

            checkerInventory.addItem(result);

            results.add(new Result(new MaterialData(result.getType(), (byte) result.getDurability()), result.getAmount()));

            inventory.setContents(checkerInventory.getContents());
        }

        if (results.isEmpty()) {
            Message.ERROR.send(sender, "Nenhum item para derreter.");
            return;
        }

        Integer cooldown = COOLDOWNS.get(user.getHighestGroup());

        if (cooldown != null) {
            UserRedisCooldowns.start(user, COOLDOWN_KEY, cooldown, TimeUnit.SECONDS);
        }

        if (this.onPreMelt != null) {
            boolean result = this.onPreMelt.apply(user, results);

            if (!result) {
                return;
            }
        }

        player.getInventory().setContents(inventory.getContents());
        Message.SUCCESS.send(sender, "Itens derretidos.");
    }

    @Getter
    @RequiredArgsConstructor
    public class Result {

        private final MaterialData materialData;
        private final int amountNeeded;
    }
}
