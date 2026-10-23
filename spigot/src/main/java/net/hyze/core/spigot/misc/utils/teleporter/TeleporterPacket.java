package net.hyze.core.spigot.misc.utils.teleporter;

import dev.utils.echo.BufferIO;
import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.hyze.core.shared.apps.AppType;
import net.hyze.core.shared.echo.api.HyzeBufferIO;
import net.hyze.core.shared.echo.api.ServerEchoPacket;
import net.hyze.core.shared.misc.utils.Position;
import net.hyze.core.shared.user.User;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Optional;

@NoArgsConstructor
public class TeleporterPacket extends ServerEchoPacket {

    private Integer userId;
    private Integer toUserId;
    private AppType toAppType;
    private Position toPosition;
    private boolean kickOnError;
    private BaseComponent[] welcomeMessage;

    public TeleporterPacket(@NonNull User user, Teleporter teleporter) {
        this.userId = user.getId();
        this.toUserId = Optional.ofNullable(teleporter.getToUser()).map(User::getId).orElse(null);
        this.toAppType = teleporter.getToAppType();
        this.toPosition = teleporter.getToPosition();
        this.kickOnError = teleporter.isKickOnError();
        this.welcomeMessage = teleporter.getWelcomeMessage();
    }

    @Override
    public void write(EchoByteBufferOutput buffer) {

        buffer.writeInt(userId);

        if (toUserId != null) {
            buffer.writeBoolean(true);
            buffer.writeInt(toUserId);
        } else {
            buffer.writeBoolean(false);
        }

        buffer.writeString(Optional.ofNullable(toAppType).map(AppType::getId).orElse(null));
        HyzeBufferIO.writePosition(buffer, toPosition);
        buffer.writeBoolean(kickOnError);

        if (welcomeMessage != null) {
            buffer.writeBoolean(true);
            buffer.writeString(ComponentSerializer.toString(welcomeMessage));
        } else {
            buffer.writeBoolean(false);
        }

    }

    @Override
    public void read(EchoByteBufferInput buffer) {

        userId = buffer.readInt();

        if (buffer.readBoolean()) {
            toUserId = buffer.readInt();
        }

        toAppType = AppType.getById(buffer.readString());
        toPosition = HyzeBufferIO.readPosition(buffer);
        kickOnError = buffer.readBoolean();

        if (buffer.readBoolean()) {
            welcomeMessage = ComponentSerializer.parse(buffer.readString());
        }

    }
}
