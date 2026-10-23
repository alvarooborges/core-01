package net.hyze.core.spigot.misc.playerdata.storage.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.hyze.core.shared.user.User;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@RequiredArgsConstructor
public class UserDataPreLoadEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final User user;

    @Setter
    private NBTTagCompound compound;

    @Setter
    private boolean cancelled;

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}