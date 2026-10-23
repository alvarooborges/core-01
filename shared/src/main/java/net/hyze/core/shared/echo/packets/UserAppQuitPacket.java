package net.hyze.core.shared.echo.packets;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.hyze.core.shared.apps.App;
import net.hyze.core.shared.echo.api.HyzeBufferIO;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserAppQuitPacket extends AbstractEchoPacket {

    private Integer userId;
    private App app;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(userId);
        HyzeBufferIO.writeApp(buffer, app);
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        userId = buffer.readInt();
        app = HyzeBufferIO.readApp(buffer);
    }
}
