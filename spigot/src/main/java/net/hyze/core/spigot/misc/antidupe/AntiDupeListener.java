package net.hyze.core.spigot.misc.antidupe;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.echo.packets.BroadcastMessagePacket;
import net.hyze.core.shared.group.Group;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class AntiDupeListener implements Listener {

    private static void check(Player player, List<ItemStack> list) {
        UUID uuid;

        Set<UUID> adIds = Sets.newHashSet();

        for (ItemStack stack : list) {

            if (stack != null && stack.getType() != Material.AIR) {
                ItemBuilder builder = ItemBuilder.of(stack, true);

                if (builder.hasNbt("ad_id")) {
                    uuid = UUID.fromString(builder.nbtString("ad_id"));

                    if (!adIds.contains(uuid)) {
                        adIds.add(uuid);
                    } else {

                        ComponentBuilder hover = new ComponentBuilder("Dupe\n")
                                .color(ChatColor.GRAY)
                                .append(player.getName() + " está com " + stack.getType() + " " + uuid.toString() + ".\n");

                        ComponentBuilder msg = new ComponentBuilder("\u26A0\u26A0 Dupe " + player.getName())
                                .bold(true)
                                .color(ChatColor.RED)
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.create()))
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + player.getName() + " alert"));

                        BroadcastMessagePacket messagePacket = BroadcastMessagePacket.builder()
                                .components(msg.create())
                                .groups(Collections.singleton(Group.MANAGER))
                                .build();

                        CoreProvider.Redis.ECHO.provide().publish(messagePacket);

                    }
                }
            }
        }
    }

    @EventHandler
    public void on(PlayerDropItemEvent event) {
        PlayerInventory inventory = event.getPlayer().getInventory();

        ItemStack[] items = inventory.getContents();
        ItemStack[] armor = inventory.getArmorContents();

        List<ItemStack> list = Lists.newArrayList(items);
        list.addAll(Arrays.asList(armor));
        list.add(event.getItemDrop().getItemStack());

        check(event.getPlayer(), list);
    }

    @EventHandler
    public void on(PlayerPickupItemEvent event) {
        PlayerInventory inventory = event.getPlayer().getInventory();

        ItemStack[] items = inventory.getContents();
        ItemStack[] armor = inventory.getArmorContents();

        List<ItemStack> list = Lists.newArrayList(items);
        list.addAll(Arrays.asList(armor));
        list.add(event.getItem().getItemStack());

        check(event.getPlayer(), list);
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        PlayerInventory inventory = event.getPlayer().getInventory();

        ItemStack[] items = inventory.getContents();
        ItemStack[] armor = inventory.getArmorContents();

        List<ItemStack> list = Lists.newArrayList(items);
        list.addAll(Arrays.asList(armor));

        check(event.getPlayer(), list);
    }
}
