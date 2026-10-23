package net.hyze.core.shared.echo.packets.user.connect;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.*;

import javax.annotation.Nullable;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserConnectHandShakeErrorPacket extends AbstractEchoPacket {

    @NonNull
    private Integer userId;

    @Nullable
    private String appId;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(userId);
        buffer.writeString(appId);
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        userId = buffer.readInt();
        appId = buffer.readString();
    }
}
