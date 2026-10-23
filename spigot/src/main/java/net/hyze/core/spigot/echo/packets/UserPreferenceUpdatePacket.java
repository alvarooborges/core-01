package net.hyze.core.spigot.echo.packets;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@NoArgsConstructor
public class UserPreferenceUpdatePacket extends AbstractEchoPacket {

    @NonNull
    @Getter
    private Integer userId;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(userId);
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        userId = buffer.readInt();
    }

}
