package net.hyze.core.shared.echo.packets.user.connect;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import dev.utils.shared.EnumUtils;
import lombok.*;
import net.hyze.core.shared.apps.AppType;
import net.hyze.core.shared.echo.api.HyzeBufferIO;
import net.hyze.core.shared.misc.utils.Position;
import net.hyze.core.shared.servers.Server;
import net.hyze.core.shared.user.User;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendUserToAppTypePacket extends AbstractEchoPacket {

    @NonNull
    private Integer userId;

    private Server server;

    private AppType type;

    @Builder.Default
    private ConnectReason reason = ConnectReason.PLUGIN;

    private Position position;

    private BaseComponent[] welcomeMessage = null;

    private boolean kickOnError;

    public SendUserToAppTypePacket(User user, AppType type, ConnectReason reason) {
        this(user.getId(), null, type, reason);
    }

    public SendUserToAppTypePacket(Integer userId, Server server, AppType type, ConnectReason reason) {
        this(userId, server, type, reason, null);
    }

    public SendUserToAppTypePacket(User user, AppType type, ConnectReason reason, Position position) {
        this(user.getId(), null, type, reason, position);
    }

    public SendUserToAppTypePacket(@NonNull Integer userId, Server server, AppType type, ConnectReason reason, Position position, BaseComponent[] welcomeMessage) {
        this(userId, server, type, reason, position, welcomeMessage, false);
    }

    public SendUserToAppTypePacket(@NonNull Integer userId, Server server, AppType type, ConnectReason reason, Position position) {
        this(userId, server, type, reason, position, null, false);
    }

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(userId);
        buffer.writeString(server != null ? server.name() : null);
        buffer.writeString(type.getId());
        buffer.writeString(reason.name());

        buffer.writeBoolean(position != null);
        if (position != null) {
            HyzeBufferIO.writePosition(buffer, position);
        }

        buffer.writeBoolean(welcomeMessage != null);
        if (welcomeMessage != null) {
            buffer.writeString(ComponentSerializer.toString(this.welcomeMessage));
        }

        buffer.writeBoolean(kickOnError);
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        userId = buffer.readInt();
        server = EnumUtils.of(Server.class, buffer.readString(), null);
        type = AppType.getById(buffer.readString());
        reason = EnumUtils.of(ConnectReason.class, buffer.readString(), null);

        if (buffer.readBoolean()) {
            position = HyzeBufferIO.readPosition(buffer);
        }

        if (buffer.readBoolean()) {
            welcomeMessage = ComponentSerializer.parse(buffer.readString());
        }

        kickOnError = buffer.readBoolean();
    }
}
