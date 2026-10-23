package net.hyze.core.shared.echo.packets.server;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import dev.utils.shared.EnumUtils;
import net.hyze.core.shared.servers.Server;
import net.hyze.core.shared.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserServerJoinPacket extends AbstractEchoPacket {

    private Integer userId;
    private Server server;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(userId);
        buffer.writeString(server.name());
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        userId = buffer.readInt();
        server = EnumUtils.of(Server.class, buffer.readString()).orElse(null);
    }

}
