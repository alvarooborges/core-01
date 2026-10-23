package net.hyze.core.spigot.inventory;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Setter
public class PaginateInventory extends CustomInventory {

    private LinkedListMultimap<Integer, Icon> items = LinkedListMultimap.create();
        private int currentItemsBuildPage = 0;

    private LinkedHashMap<Integer, Icon> menu = Maps.newLinkedHashMap();

    protected ItemStack emptyIcon = new ItemBuilder(Material.WEB).name("&cVazio").make();
    protected boolean defaultHotbar = true;

    private int currentPageIndex = 0;

    private static final Integer[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private PaginateInventory(String title, int rows, int page, boolean defaultHotbar, LinkedHashMap<Integer, Icon> menu,
                              LinkedListMultimap<Integer, Icon> items) {
        super(rows * 9, title);

        this.currentPageIndex = page;
        this.defaultHotbar = defaultHotbar;
        this.menu = menu;
        this.items = items;

    }

    protected PaginateInventory(String title) {
        super(54, title);
    }

    @Override
    public void onOpen(InventoryOpenEvent event0) {
        drawPage();

        if (!defaultHotbar) {
            return;
        }

        super.onOpen(event0);
    }

    protected void drawPage() {
        resetMenuSlots();
        resetArrowsSlots();
        resetItemsSlots();

        if (getViewers().size() == 1) {
            setupBackOrClose((Player) getViewers().get(0));
        }

        for (Map.Entry<Integer, Icon> entry : menu.entrySet()) {
            Integer menuSlot = entry.getKey();
            Icon menuIcon = entry.getValue();
            super.setItem(menuSlot, menuIcon.getIcon(), menuIcon.getInventoryClick());
        }

        if (items.values().isEmpty()) {
            super.setItem(22, emptyIcon);
            return;
        }

        int totalPages = 1;

        if (!items.isEmpty()) {
            totalPages = (int) Math.ceil((double) items.size() / (double) SLOTS.length);
        }

        if (currentPageIndex > 0) {
            super.setItem(18,
                    new ItemBuilder(Material.ARROW).name("&aPágina Anterior: " + currentPageIndex).make(),
                    event -> {
                        currentPageIndex--;
                        drawPage();
                    });
        }

        /*
         * Caso não seja a última página, é colocado o botão de avançar.
         */
        if (currentPageIndex < (totalPages - 1)) {
            super.setItem(26,
                    new ItemBuilder(Material.ARROW).name("&aPróxima Página: " + (currentPageIndex + 2)).make(),
                    event -> {
                        currentPageIndex++;
                        drawPage();
                    });
        }

        AtomicInteger slot = new AtomicInteger(0);

        for (Icon icon : items.get(currentPageIndex)) {
            super.setItem(SLOTS[slot.getAndIncrement()], icon.getIcon(), icon.getInventoryClick());
        }
    }

    public void resetMenuSlots() {
        for (int i = getSize() - 9; i < getSize(); i++) {
            setItem(i, null);
        }
    }

    public void resetArrowsSlots() {
        setItem(18, null);
        setItem(26, null);
    }

    public void resetItemsSlots() {
        for (int s : SLOTS) {
            super.setItem(s, null);
        }
    }

    public void clearItems() {
        this.items.clear();
        this.currentItemsBuildPage = 0;
    }

    public void clearMenus() {
        this.menu.clear();
    }

    public void resetCurrentPage() {
        this.currentPageIndex = 0;
    }

    public void addMenu(Integer slot, ItemStack icon, Consumer<InventoryClickEvent> inventoryClick) {
        this.menu.put(slot, new PaginateInventory.Icon(icon, inventoryClick));
    }

    public void addMenu(Integer slot, ItemStack icon) {
        this.menu.put(slot, new PaginateInventory.Icon(icon));
    }

    @Override
    public void addItem(ItemStack icon, Consumer<InventoryClickEvent> inventoryClick) {

        if (this.items.containsKey(this.currentItemsBuildPage)
                && this.items.get(this.currentItemsBuildPage).size() == SLOTS.length) {
            this.currentItemsBuildPage++;
        }

        this.items.put(this.currentItemsBuildPage, new PaginateInventory.Icon(icon, inventoryClick));
    }

    @Override
    public void addItem(ItemStack icon, Runnable inventoryClick) {
        addItem(icon, event -> inventoryClick.run());
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        super.onClick(event);
    }

    @Override
    public HashMap<Integer, ItemStack> addItem(ItemStack... icon) {
        for (ItemStack stack : icon) {
            if (stack == null) {
                continue;
            }

            this.addItem(stack, (Consumer) null);
        }

        return Maps.newHashMap();
    }

    public static PaginateInventoryBuilder builder() {
        return new PaginateInventoryBuilder();
    }

    @Getter
    @RequiredArgsConstructor
    @AllArgsConstructor
    private static class Icon {

        private final ItemStack icon;
        private Consumer<InventoryClickEvent> inventoryClick;

    }

    public static class PaginateInventoryBuilder {

        private final LinkedListMultimap<Integer, PaginateInventory.Icon> items = LinkedListMultimap.create();
        private final LinkedHashMap<Integer, PaginateInventory.Icon> menu = Maps.newLinkedHashMap();

        private ItemStack emptyIcon = new ItemBuilder(Material.WEB).name("&cVazio").make();
        private int page = 0;
        private boolean defaultHotbar = true;

        public PaginateInventoryBuilder item(ItemStack icon, Consumer<InventoryClickEvent> inventoryClick) {
            if (this.items.containsKey(this.page) && this.items.get(this.page).size() == SLOTS.length) {
                this.page++;
            }

            this.items.put(this.page, new PaginateInventory.Icon(icon, inventoryClick));
            return this;
        }

        public PaginateInventoryBuilder menu(Integer slot, ItemStack icon, Consumer<InventoryClickEvent> inventoryClick) {
            this.menu.put(slot, new PaginateInventory.Icon(icon, inventoryClick));
            return this;
        }

        public PaginateInventoryBuilder emptyIcon(ItemStack emptyIcon) {
            this.emptyIcon = emptyIcon;
            return this;
        }

        /**
         * Caso seja true, os icones de voltar e avançar estarão sempre visiveis
         * na última linha do inventário.
         *
         * @param value
         * @return
         */
        public PaginateInventoryBuilder defaultHotbar(boolean value) {
            this.defaultHotbar = value;
            return this;
        }

        public PaginateInventory build(String title) {
            int rows = this.items.size() >= (SLOTS.length - 7) ? 6 : 5;
            return new PaginateInventory(title, rows, 0, this.defaultHotbar, this.menu, this.items);
        }
    }
}
