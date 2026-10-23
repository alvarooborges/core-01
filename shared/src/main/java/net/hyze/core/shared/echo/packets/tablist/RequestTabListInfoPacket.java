package net.hyze.core.shared.echo.packets.tablist;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestTabListInfoPacket extends AbstractEchoPacket {

    private int userId;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(userId);
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        userId = buffer.readInt();
    }
}