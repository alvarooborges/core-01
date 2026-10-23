package net.hyze.core.shared.echo.packets.user;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserTellPacket extends AbstractEchoPacket {

    private Integer senderId;
    private Integer targetId;
    private String message;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(senderId);
        buffer.writeInt(targetId);
        buffer.writeString(message);
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        senderId = buffer.readInt();
        targetId = buffer.readInt();
        message = buffer.readString();
    }
}
