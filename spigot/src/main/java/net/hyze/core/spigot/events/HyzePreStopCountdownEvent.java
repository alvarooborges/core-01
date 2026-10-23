package net.hyze.core.spigot.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@RequiredArgsConstructor
public class HyzePreStopCountdownEvent extends Event {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final int originalCountdown;
    private final int currentCountdown;

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
