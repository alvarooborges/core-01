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
public class TabListInfoPacket extends AbstractEchoPacket {

    private int userId;
    private String[][] data;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(userId);
        buffer.writeInt(data.length);
        buffer.writeInt(data[0].length);

        for (int column = 0; column < data.length; column++) {
            for (int j = 0; j < data[0].length; j++) {
                buffer.writeString(data[column][j]);
            }
        }
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        userId = buffer.readInt();
        data = new String[buffer.readInt()][buffer.readInt()];

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                data[i][j] = buffer.readString();
            }
        }
    }
}
