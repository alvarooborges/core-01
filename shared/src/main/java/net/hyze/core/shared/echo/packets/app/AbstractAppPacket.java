package net.hyze.core.shared.echo.packets.app;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.hyze.core.shared.apps.App;
import net.hyze.core.shared.echo.api.HyzeBufferIO;

@NoArgsConstructor
@AllArgsConstructor
public class AbstractAppPacket extends AbstractEchoPacket {

    @Getter
    private App app;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        HyzeBufferIO.writeApp(buffer, app);
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        app = HyzeBufferIO.readApp(buffer);
    }
}
