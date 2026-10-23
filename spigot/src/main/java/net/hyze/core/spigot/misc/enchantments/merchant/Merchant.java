package net.hyze.core.spigot.misc.enchantments.merchant;

import io.netty.buffer.Unpooled;
import net.hyze.core.spigot.misc.enchantments.CustomEnchantmentUtil;
import net.hyze.core.spigot.misc.enchantments.merchant.events.MerchantTradeEvent;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import net.hyze.core.spigot.misc.utils.PlayerUtils;
import net.minecraft.server.v1_8_R3.ContainerMerchant;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public class Merchant implements IMerchant, Listener {

    private final String name;
    private EntityHuman human;
    private ItemStack originalResult;
    private int expCost;

    private static final ItemStack DEFAULT_RESULT_ITEM = new ItemBuilder(Material.BARRIER)
            .clearFlags()
            .name("&cNenhum resultado")
            .make();

    private static final ItemStack DEFAULT_SECOND_ITEM = new ItemBuilder(Material.MAP)
            .clearFlags()
            .name("&aMaterial")
            .lore("&7Coloque um item no segundo slot", "&7para que ele seja fundido ao", "&7primeiro item!")
            .make();

    public Merchant(String name, EntityHuman human, JavaPlugin plugin) {
        this.name = name;
        this.human = human;

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void a_(EntityHuman human) {
        this.human = human;
        if (this.human == null) {
            HandlerList.unregisterAll(this);
        }
    }

    @Override
    public EntityHuman v_() {
        return human;
    }

    @Override
    public MerchantRecipeList getOffers(EntityHuman entityHuman) {

        if (!(entityHuman.activeContainer instanceof net.hyze.core.spigot.misc.enchantments.merchant.ContainerMerchant)) {
            return new MerchantRecipeList();
        }

        Inventory inventory = entityHuman.activeContainer.getBukkitView().getTopInventory();

        ItemStack first = inventory.getItem(0);
        ItemStack second = inventory.getItem(1);

        MerchantRecipeList list = new MerchantRecipeList();
        this.originalResult = null;

        Player player = (Player) entityHuman.getBukkitEntity();

        if (first != null) {
            if (second == null) {
                list.add(new MerchantRecipe(
                        CraftItemStack.asNMSCopy(first),
                        CraftItemStack.asNMSCopy(DEFAULT_SECOND_ITEM),
                        CraftItemStack.asNMSCopy(DEFAULT_RESULT_ITEM)
                ));
            } else {
                MerchantRecipeEvent event = new MerchantRecipeEvent(player, first, second);

                Bukkit.getServer().getPluginManager().callEvent(event);

                if (event.isCancelled() || event.getResult() == null) {
                    list.add(new MerchantRecipe(
                            CraftItemStack.asNMSCopy(first),
                            CraftItemStack.asNMSCopy(DEFAULT_SECOND_ITEM),
                            CraftItemStack.asNMSCopy(DEFAULT_RESULT_ITEM)
                    ));
                } else {
                    ItemStack second0 = second.clone();
                    second0.setAmount(1);

                    this.originalResult = event.getResult();

                    ItemBuilder resultPreview = ItemBuilder.of(this.originalResult)
                            .flags(ItemFlag.HIDE_ATTRIBUTES);

                    int enchantmentsCount = resultPreview.enchantments().size();
                    enchantmentsCount += CustomEnchantmentUtil.getEnchantments(this.originalResult).size();

                    // 55 * n(n + 1) / 2
                    int base = 55;
                    int multiplier = (enchantmentsCount * (enchantmentsCount + 1) / 2);
                    this.expCost = Math.max(base * multiplier, base);

                    resultPreview.lore("", "&fCusto: &6" + this.expCost + " XP");

                    if (PlayerUtils.getExp(player) < this.expCost) {
                        resultPreview.lore(
                                "",
                                "&cVocê não possui XP",
                                "&csuficiente para forjar",
                                "&ceste item."
                        );
                    } else {
                        resultPreview.lore(
                                "",
                                "&aClique para forjar!"
                        );
                    }

                    list.add(new MerchantRecipe(
                            CraftItemStack.asNMSCopy(first),
                            CraftItemStack.asNMSCopy(second0),
                            CraftItemStack.asNMSCopy(resultPreview.make())
                    ));
                }
            }
        }

        return list;
    }

    @Override
    public void a(MerchantRecipe mr) {

    }

    @Override
    public void a_(net.minecraft.server.v1_8_R3.ItemStack is) {
        MerchantRecipeList list = getOffers(human);
        updateOffers(list);
    }

    @Override
    public IChatBaseComponent getScoreboardDisplayName() {
        return new ChatComponentText(name);
    }

    public void updateOffers(MerchantRecipeList list) {
        PacketDataSerializer packetdataserializer = new PacketDataSerializer(Unpooled.buffer());

        packetdataserializer.writeInt(human.activeContainer.windowId);
        list.a(packetdataserializer);
        ((EntityPlayer) human).playerConnection.sendPacket(new PacketPlayOutCustomPayload("MC|TrList", packetdataserializer));

        ((EntityPlayer) human).updateInventory(human.activeContainer);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        EntityPlayer entityPlayer = ((CraftPlayer) event.getWhoClicked()).getHandle();

        if (!(entityPlayer.activeContainer instanceof ContainerMerchant)) {
            return;
        }

        try {
            Field field = entityPlayer.activeContainer.getClass().getSuperclass().getDeclaredField("merchant");
            field.setAccessible(true);
            IMerchant merchant = (IMerchant) field.get(entityPlayer.activeContainer);

            if (merchant != this) {
                return;
            }

            Inventory topInventory = event.getView().getTopInventory();

            if (event.getRawSlot() > 2) {

                if (!event.getClick().isShiftClick()) {
                    return;
                }

                ItemStack clickedItem;
                if ((clickedItem = event.getCurrentItem()) == null || clickedItem.getType() == Material.AIR) {
                    return;
                }

                for (int i = 0; i < 2; i++) {
                    ItemStack inTop = topInventory.getItem(i);

                    if (inTop == null || inTop.getType() == Material.AIR) {
                        topInventory.setItem(i, clickedItem);
                        event.setCurrentItem(null);
                        event.setCancelled(true);

                        updateOffers(getOffers(human));
                        return;
                    }
                }

                return;
            }


            ItemStack result = topInventory.getItem(2);

            if (event.getRawSlot() == 2) {

                if (result == null || result.getType().equals(Material.BARRIER)) {
                    event.setCancelled(true);
                    return;
                }

                Player player = (Player) event.getWhoClicked();

                if (PlayerUtils.getExp(player) < this.expCost) {
                    Message.ERROR.send(player, "Você não possui XP suficiente para forjar este item.");
                    event.setCancelled(true);
                    return;
                }

                PlayerUtils.changeExp(player, -this.expCost);

                topInventory.setItem(2, this.originalResult);
                Bukkit.getPluginManager().callEvent(new MerchantTradeEvent(player, this.originalResult));
            }

        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace();
            event.setCancelled(true);
        }
    }
}
