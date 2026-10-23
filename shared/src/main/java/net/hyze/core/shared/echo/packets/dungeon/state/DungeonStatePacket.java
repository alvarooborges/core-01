package net.hyze.core.shared.echo.packets.dungeon.state;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DungeonStatePacket extends AbstractEchoPacket {

    private final String dungeonId;

    private final EnumState state;

    @Override
    public void write(EchoByteBufferOutput buffer) {

    }

    @Override
    public void read(EchoByteBufferInput buffer) {

    }

    public enum EnumState {

        EMPTY, POPULATED

    }
}
