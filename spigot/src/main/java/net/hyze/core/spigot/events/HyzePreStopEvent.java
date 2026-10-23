package net.hyze.core.spigot.events;

import dev.utils.bukkit.stop.event.PreStopEvent;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

public class HyzePreStopEvent extends PreStopEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    private final int countdownSeconds;

    public HyzePreStopEvent(CommandSender sender, int countdownSeconds) {
        super(sender);

        this.countdownSeconds = countdownSeconds;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
