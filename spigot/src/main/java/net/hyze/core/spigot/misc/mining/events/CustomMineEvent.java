package net.hyze.core.spigot.misc.mining.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
public class CustomMineEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;

    private final List<ItemStack> drops;

    public CustomMineEvent(Player player, List<ItemStack> drops) {
        this.drops = drops;
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
