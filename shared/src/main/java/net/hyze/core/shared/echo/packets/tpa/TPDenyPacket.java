package net.hyze.core.shared.echo.packets.tpa;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import net.hyze.core.shared.echo.api.ServerEchoPacket;
import net.hyze.core.shared.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TPDenyPacket extends ServerEchoPacket {

    private Integer requesterId;
    private Integer targetId;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(requesterId);
        buffer.writeInt(targetId);
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        requesterId = buffer.readInt();
        targetId = buffer.readInt();
    }
}
