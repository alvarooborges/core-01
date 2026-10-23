package net.hyze.core.spigot.misc.customitem.data.enchanted_books;

import lombok.Getter;
import net.hyze.core.shared.misc.utils.NumberUtils;
import net.hyze.core.shared.misc.utils.Pair;
import net.hyze.core.shared.misc.utils.WeightedCollection;
import net.hyze.core.spigot.CoreSpigotConstants;
import net.hyze.core.spigot.misc.customitem.CustomItem;
import net.hyze.core.spigot.misc.enchantments.CustomEnchantment;
import net.hyze.core.spigot.misc.enchantments.CustomEnchantmentRegistry;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public abstract class AbstractRandomEnchantmentsBook extends CustomItem {

    @Getter
    protected final ItemBuilder itemBuilder;

    @Getter
    private final Map<Pair<Enchantment, Integer>, Double> enchantments;

    @Getter
    private final Map<Pair<CustomEnchantment, Integer>, Double> customEnchantments;


    public AbstractRandomEnchantmentsBook(String key,
                                          Map<Pair<Enchantment, Integer>, Double> enchantments,
                                          Map<Pair<CustomEnchantment, Integer>, Double> customEnchantments) {
        super(key);

        this.enchantments = enchantments;
        this.customEnchantments = customEnchantments;

        this.itemBuilder = ItemBuilder.of(Material.BOOK)
                .name("&7" + getDisplayName())
                .glowing(true)
                .lore(
                        "&7Ao abrir este livro, você receberá um",
                        "&7dos Livros de Encantamento que",
                        "&7estão listados abaixo!",
                        "",
                        "&8\u25AA &fLista de Possíveis Encantamentos:"
                );

        WeightedCollection<Pair<?, Integer>> randomList = new WeightedCollection<>();

        enchantments.forEach(randomList::add);
        customEnchantments.forEach(randomList::add);

        NBTTagList nbtEnchantments = new NBTTagList();
        NBTTagList nbtCustomEnchantments = new NBTTagList();

        randomList.stream()
                .sorted((r1, r2) -> Double.compare(r2.getChance(), r1.getChance()))
                .forEach(random -> {
                    Pair<?, Integer> pair = random.getObject();

                    int level = pair.getRight();

                    if (pair.getLeft() instanceof Enchantment) {
                        Enchantment enchantment = (Enchantment) pair.getLeft();

                        this.itemBuilder.lore(String.format(
                                " &8\u25AA &7%s %s",
                                CoreSpigotConstants.TRANSLATE_ITEM.get(enchantment),
                                NumberUtils.toRoman(level)
                        ));

                        NBTTagCompound compound = new NBTTagCompound();

                        compound.setString("enchantment", enchantment.getName());
                        compound.setInt("level", level);
                        compound.setDouble("weight", random.getWeight());

                        nbtEnchantments.add(compound);

                    } else if (pair.getLeft() instanceof CustomEnchantment) {
                        CustomEnchantment enchantment = (CustomEnchantment) pair.getLeft();

                        String color = "&b";

                        if (random.getChance() < 5) {
                            color = "&b";
                        } else if (random.getChance() < 10) {
                            color = "&6";
                        }

                        this.itemBuilder.lore(String.format(
                                " &8\u25AA %s%s",
                                color,
                                enchantment.getDisplayName(level)
                        ));

                        NBTTagCompound compound = new NBTTagCompound();

                        compound.setString("enchantment", enchantment.getKey());
                        compound.setInt("level", level);
                        compound.setDouble("weight", random.getWeight());

                        nbtCustomEnchantments.add(compound);
                    }
                });

        this.itemBuilder.nbt("divine_enchantments", nbtEnchantments);
        this.itemBuilder.nbt("divine_custom_enchantments", nbtCustomEnchantments);
    }

    public boolean contains(Enchantment enchantment, int lvl) {
        return enchantments.containsKey(new Pair<>(enchantment, lvl));
    }

    protected String getRewardName(Pair<?, Integer> pair) {
        if (pair.getLeft() instanceof Enchantment) {

            Enchantment enchantment = (Enchantment) pair.getLeft();
            return CoreSpigotConstants.TRANSLATE_ITEM.get(enchantment) + " " + NumberUtils.toRoman(pair.getRight());

        } else if (pair.getLeft() instanceof CustomEnchantment) {

            CustomEnchantment enchantment = (CustomEnchantment) pair.getLeft();
            return enchantment.getDisplayName(pair.getRight());
        }

        return null;
    }

    protected WeightedCollection<Pair<?, Integer>> getRandomList(ItemStack item) {
        WeightedCollection<Pair<?, Integer>> randomList = new WeightedCollection<>();

        ItemBuilder builder = ItemBuilder.of(item);

        NBTTagList nbtEnchantments = builder.nbtList("divine_enchantments");
        for (int i = 0; i < nbtEnchantments.size(); i++) {
            NBTTagCompound compound = nbtEnchantments.get(i);

            Enchantment enchantment = Enchantment.getByName(compound.getString("enchantment"));

            if (enchantment != null) {
                randomList.add(new Pair<>(enchantment, compound.getInt("level")), compound.getDouble("weight"));
            }
        }

        NBTTagList nbtCustomEnchantments = builder.nbtList("divine_custom_enchantments");
        for (int i = 0; i < nbtCustomEnchantments.size(); i++) {
            NBTTagCompound compound = nbtCustomEnchantments.get(i);

            CustomEnchantment customEnchantment = CustomEnchantmentRegistry.get(compound.getString("enchantment"));

            if (customEnchantment != null) {
                randomList.add(new Pair<>(customEnchantment, compound.getInt("level")), compound.getDouble("weight"));
            }
        }

        return randomList;
    }
}
