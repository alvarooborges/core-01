package net.hyze.core.shared.echo.packets;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendDiscordLogPacket extends AbstractEchoPacket {

    private String message;
    private String channel;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeString(message);
        buffer.writeString(channel);
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        message = buffer.readString();
        channel = buffer.readString();
    }
}
