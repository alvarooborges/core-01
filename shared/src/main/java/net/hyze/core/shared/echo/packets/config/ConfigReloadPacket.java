package net.hyze.core.shared.echo.packets.config;

import com.google.common.collect.Maps;
import dev.utils.echo.BufferIO;
import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigReloadPacket extends AbstractEchoPacket {

    private Map<String, String> config;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(this.config.size());

        for (Map.Entry<String, String> entry : this.config.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            BufferIO.writeString(buffer, key);
            BufferIO.writeString(buffer, value);
        }
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        this.config = Maps.newHashMap();

        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            this.config.put(BufferIO.readString(buffer), BufferIO.readString(buffer));
        }
    }

}
