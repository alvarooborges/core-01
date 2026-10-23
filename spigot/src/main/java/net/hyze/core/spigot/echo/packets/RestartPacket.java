package net.hyze.core.spigot.echo.packets;

import com.google.common.collect.Sets;
import java.util.Set;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RestartPacket extends AbstractEchoPacket {

    private int seconds;
    private Set<String> apps = Sets.newHashSet();

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(this.seconds);

        buffer.writeInt(this.apps.size());
        this.apps.stream().forEach(m -> buffer.writeString(m));

    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        this.seconds = buffer.readInt();

        int j = buffer.readInt();

        for (int i = 0; i < j; i++) {
            this.apps.add(buffer.readString());
        }

    }

}
