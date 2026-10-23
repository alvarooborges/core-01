package net.hyze.core.spigot.connect.events;

import lombok.Getter;
import lombok.Setter;
import net.hyze.core.shared.echo.packets.user.connect.UserConnectHandShakePacket;
import net.hyze.core.shared.user.User;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class AsyncUserConnectHandShakeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final User user;
    private final ConnectSide side;
    private final UserConnectHandShakePacket packet;

    public AsyncUserConnectHandShakeEvent(User user, ConnectSide side, UserConnectHandShakePacket packet) {
        super(true);

        this.user = user;
        this.side = side;
        this.packet = packet;
    }

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
