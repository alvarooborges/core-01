package net.hyze.core.shared.echo.packets.user.connect;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import dev.utils.shared.EnumUtils;
import lombok.*;
import net.hyze.core.shared.echo.api.HyzeBufferIO;
import net.hyze.core.shared.misc.utils.Position;
import net.hyze.core.shared.user.User;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

@Getter
@NoArgsConstructor
@ToString
public class UserConnectHandShakePacket extends AbstractEchoPacket {

    private Integer userId;
    private String appId;
    private Position position;
    private int targetId;
    private ConnectReason reason;

    @Setter
    private ConnectConsent proxyConsent = ConnectConsent.PENDING;

    @Setter
    private ConnectConsent targetAppConsent = ConnectConsent.PENDING;

    @Setter
    private ConnectConsent currentAppConsent = ConnectConsent.PENDING;

    @Setter
    private BaseComponent[] welcomeMessage = null;

    private boolean kickOnError;

    public UserConnectHandShakePacket(User user, String appId, ConnectReason reason) {
        this(user, appId, null, reason);
    }

    public UserConnectHandShakePacket(User user, String appId, Position position, ConnectReason reason) {
        this.userId = user.getId();
        this.appId = appId;
        this.position = position;
        this.reason = reason;
    }

    public UserConnectHandShakePacket(User user, User targetUser, String appId, ConnectReason reason) {
        this.userId = user.getId();
        this.targetId = targetUser.getId();
        this.appId = appId;
        this.reason = reason;
    }

    public UserConnectHandShakePacket(Integer userId, String appId, Position position, ConnectReason reason,
                                      BaseComponent[] welcomeMessage, boolean kickOnError) {

        this.userId = userId;
        this.appId = appId;
        this.position = position;
        this.reason = reason;
        this.welcomeMessage = welcomeMessage;
        this.kickOnError = kickOnError;
    }

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(userId);
        buffer.writeString(appId);

        buffer.writeBoolean(position != null);
        if (position != null) {
            HyzeBufferIO.writePosition(buffer, position);
        }

        buffer.writeInt(targetId);
        buffer.writeString(reason.name());
        buffer.writeString(proxyConsent.name());
        buffer.writeString(targetAppConsent.name());
        buffer.writeString(currentAppConsent.name());

        buffer.writeBoolean(welcomeMessage != null);
        if (welcomeMessage != null) {
            buffer.writeString(ComponentSerializer.toString(this.welcomeMessage));
        }
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        userId = buffer.readInt();
        appId = buffer.readString();

        if (buffer.readBoolean()) {
            position = HyzeBufferIO.readPosition(buffer);
        }

        targetId = buffer.readInt();
        reason = EnumUtils.of(ConnectReason.class, buffer.readString(), null);
        proxyConsent = EnumUtils.of(ConnectConsent.class, buffer.readString(), null);
        targetAppConsent = EnumUtils.of(ConnectConsent.class, buffer.readString(), null);
        currentAppConsent = EnumUtils.of(ConnectConsent.class, buffer.readString(), null);

        if (buffer.readBoolean()) {
            welcomeMessage = ComponentSerializer.parse(buffer.readString());
        }
    }
}
