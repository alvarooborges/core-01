package net.hyze.core.spigot.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.misc.utils.InventoryUtils;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IInventory;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class CustomInventory extends CraftInventory implements ICustomInventory {

    protected Map<Integer, ClickListener> listeners = Maps.newHashMap();

    @Setter
    private Consumer<InventoryCloseEvent> onClose;

    @Setter
    private Consumer<InventoryOpenEvent> onOpen;

    private final MinecraftInventory minecraftInventory;

    public CustomInventory(int size, String title) {
        super(new MinecraftInventory(size, title));

        this.minecraftInventory = ((MinecraftInventory) this.inventory);
        this.minecraftInventory.init(this);
    }

    /*
     *
     */
    @Override
    public ClickListener getListener(int slot) {
        return listeners.get(slot);
    }

    @Override
    public void clear() {
        super.clear();
        listeners.clear();
    }

    @Override
    public void clear(int index) {
        super.clear(index);
        listeners.remove(index);
    }

    @Override
    public void setItem(int index, ItemStack item) {
        super.setItem(index, item);

        if (item == null) {
            listeners.remove(index);
        }
    }

    public void setItem(int y, int x, ItemStack item) {
        this.setItem(y * 9 + x, item);
    }

    @Override
    public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> callback) {
        this.setItem(slot, item);

        if (item != null && callback != null) {
            listeners.put(slot, (ConsumerClickListener) callback::accept);
        }
    }

    public void setItem(int slot, ItemStack item, Runnable callback) {
        this.setItem(slot, item, event -> {
            if (callback != null) {
                callback.run();
            }
        });
    }

    @Override
    public void addItem(ItemStack item, Consumer<InventoryClickEvent> consumer) {
        ItemStack[] contents = this.getContents(); //este método gera muitos objetos ao ser chamado, por isso só 1 vez.

        for (int i = 0; i < this.getSize(); i++) {
            if (contents[i] == null || contents[i].getType() == Material.AIR) {
                this.setItem(i, item, consumer);
                break;
            }
        }
    }

    public void addItem(ItemStack item, Runnable consumer) {
        this.addItem(item, event -> consumer.run());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onOpen(InventoryOpenEvent event) {
        setupBackOrClose((Player) event.getPlayer());

        if (this.onOpen != null) {
            this.onOpen.accept(event);
        }
    }

    protected void setupBackOrClose(Player player) {

        if (backOrCloseItem != null) {
            Inventory back = null;

            final LinkedList<Inventory> list;

            if (player.hasMetadata("custom_inventory_queue")) {
                list = (LinkedList<Inventory>) player.getMetadata("custom_inventory_queue").get(0).value();

                if (!list.isEmpty()) {
                    back = list.peekFirst();
                }

            }

            if (back != null) {
                backItem(backOrCloseItem, back);
            } else {
                closeItem(backOrCloseItem);
            }
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        if (this.onClose != null) {
            this.onClose.accept(event);
        }
    }

    public void backItem(Consumer<InventoryClickEvent> consumer) {
        backItem(this.getSize() - 5, consumer);
    }

    public void backItem(int slot, Consumer<InventoryClickEvent> consumer) {
        setItem(slot, BACK_ARROW, consumer);
    }

    //
    public void backItem(Inventory previous) {
        backItem(this.getSize() - 5, previous);
    }

    public void backItem(int slot, @Nonnull Inventory previous) {
        setItem(slot, BACK_ARROW, event -> event.getWhoClicked().openInventory(previous));
    }

    public void closeItem(int slot) {
        setItem(slot, CLOSE_BARRIER, event -> event.getWhoClicked().closeInventory());
    }


    protected Integer backOrCloseItem;

    public void backOrCloseItem() {
        backOrCloseItem(this.getSize() - 5);
    }

    public void backOrCloseItem(int slot) {
        backOrCloseItem = slot;
    }


    public static class MinecraftInventory extends Container implements IInventory {

        public net.minecraft.server.v1_8_R3.ItemStack[] items;

        @Setter
        @Getter
        private int maxStackSize = MAX_STACK;

        @Getter
        private final List<HumanEntity> viewers;

        @Getter
        private final String name;

        @Getter
        private final InventoryType type;

        public MinecraftInventory(int size, String title) {
            Validate.notNull(title, "Title cannot be null");
            this.items = new net.minecraft.server.v1_8_R3.ItemStack[size];
            this.name = title;
            this.viewers = Lists.newArrayList();
            this.type = InventoryType.CHEST;
        }

        @Override
        public int getSize() {
            return items.length;
        }

        @Override
        public net.minecraft.server.v1_8_R3.ItemStack getItem(int i) {
            return items[i];
        }

        @Override
        public net.minecraft.server.v1_8_R3.ItemStack splitStack(int i, int j) {
            net.minecraft.server.v1_8_R3.ItemStack stack = this.getItem(i);
            net.minecraft.server.v1_8_R3.ItemStack result;
            if (stack == null) {
                return null;
            }
            if (stack.count <= j) {
                this.setItem(i, null);
                result = stack;
            } else {
                result = CraftItemStack.copyNMSStack(stack, j);
                stack.count -= j;
            }

            this.update();

            return result;
        }

        @Override
        public net.minecraft.server.v1_8_R3.ItemStack splitWithoutUpdate(int i) {
            net.minecraft.server.v1_8_R3.ItemStack stack = this.getItem(i);
            net.minecraft.server.v1_8_R3.ItemStack result;
            if (stack == null) {
                return null;
            }
            if (stack.count <= 1) {
                this.setItem(i, null);
                result = stack;
            } else {
                result = CraftItemStack.copyNMSStack(stack, 1);
                stack.count -= 1;
            }
            return result;
        }

        @Override
        public void setItem(int i, net.minecraft.server.v1_8_R3.ItemStack itemstack) {
            items[i] = itemstack;
            if (itemstack != null && this.getMaxStackSize() > 0 && itemstack.count > this
                    .getMaxStackSize()) {
                itemstack.count = this.getMaxStackSize();
            }
        }

        @Override
        public void update() {
        }

        @Override
        public boolean a(EntityHuman entityhuman) {
            return true;
        }

        @Override
        public net.minecraft.server.v1_8_R3.ItemStack[] getContents() {
            return items;
        }

        @Override
        public void onOpen(CraftHumanEntity who) {
            viewers.add(who);
        }

        public void onClose(CraftHumanEntity who) {
            viewers.remove(who);
        }

        @Override
        public InventoryHolder getOwner() {
            return null;
        }

        public boolean b(int i, net.minecraft.server.v1_8_R3.ItemStack itemstack) {
            return true;
        }

        @Override
        public void startOpen(EntityHuman entityHuman) {

            Player player = (Player) entityHuman.getBukkitEntity();

            Inventory back = InventoryUtils.getCurrent(player);

            if (back == null) {
                player.removeMetadata("custom_inventory_queue", CoreSpigotPlugin.getInstance());
            } else {

                if (((CraftInventory) back).getInventory() instanceof CustomInventory.MinecraftInventory) {

                    IInventory backIInventory = ((CraftInventory) back).getInventory();

                    CustomInventory backParent = (CustomInventory) ((CustomInventory.MinecraftInventory) backIInventory)
                            .getParent();

                    if (backParent.getClass().isAssignableFrom(parent.getClass())) {
                        return;
                    }

                    LinkedList<Inventory> list;

                    if (player.hasMetadata("custom_inventory_queue")) {
                        list = (LinkedList<Inventory>) player.getMetadata("custom_inventory_queue").get(0).value();
                    } else {
                        list = Lists.newLinkedList();
                        player.setMetadata("custom_inventory_queue", new FixedMetadataValue(CoreSpigotPlugin.getInstance(), list));
                    }

                    if (!list.isEmpty()) {

                        Inventory first = list.peekFirst();

                        IInventory firstIInventory = ((CraftInventory) first).getInventory();

                        CustomInventory firstParent = (CustomInventory) ((CustomInventory.MinecraftInventory) firstIInventory)
                                .getParent();

                        if (firstParent.getClass().isAssignableFrom(parent.getClass())) {
                            list.removeFirst();
                        } else {
                            list.addFirst(back);
                        }

                    } else {
                        list.addFirst(back);
                    }
                }
            }
        }

        @Override
        public void closeContainer(EntityHuman entityHuman) {

        }

        @Override
        public int getProperty(int i) {
            return 0;
        }

        @Override
        public void b(int i, int i1) {

        }

        @Override
        public int g() {
            return 0;
        }

        @Override
        public void l() {

        }

        @Override
        public boolean hasCustomName() {
            return name != null;
        }

        @Override
        public IChatBaseComponent getScoreboardDisplayName() {
            return new ChatComponentText(name);
        }
    }

}
