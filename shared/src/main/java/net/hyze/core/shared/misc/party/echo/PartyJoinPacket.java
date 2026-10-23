package net.hyze.core.shared.misc.party.echo;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.user.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PartyJoinPacket extends AbstractEchoPacket {

    /**
     * Usuário que aceitou o convite de party.
     */
    private User user;

    /**
     * Usuário que enviou o convite de party.
     */
    private User target;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(this.user.getId());
        buffer.writeInt(this.target.getId());
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        this.user = CoreProvider.Cache.Local.USERS.provide().get(buffer.readInt());
        this.target = CoreProvider.Cache.Local.USERS.provide().get(buffer.readInt());
    }

}
