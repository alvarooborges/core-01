package net.hyze.core.shared.echo.packets.config;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ServersReloadPacket extends AbstractEchoPacket {

    @Override
    public void write(EchoByteBufferOutput buffer) {
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
    }

}
