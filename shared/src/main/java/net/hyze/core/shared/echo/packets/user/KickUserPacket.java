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
public class KickUserPacket extends AbstractEchoPacket {

    private Integer userId;
    private String message;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(userId);
        buffer.writeString(message);
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        userId = buffer.readInt();
        message = buffer.readString();
    }

}
