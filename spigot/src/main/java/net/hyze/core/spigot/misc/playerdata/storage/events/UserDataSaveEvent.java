package net.hyze.core.spigot.misc.playerdata.storage.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.user.User;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@RequiredArgsConstructor
public class UserDataSaveEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final User user;
    private final NBTTagCompound compound;

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
