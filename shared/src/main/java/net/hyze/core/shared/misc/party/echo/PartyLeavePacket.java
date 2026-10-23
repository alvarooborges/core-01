package net.hyze.core.shared.misc.party.echo;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.hyze.core.shared.CoreConstants;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.misc.party.Party;
import net.hyze.core.shared.user.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PartyLeavePacket extends AbstractEchoPacket {

    /**
     * Id da party que ele estava.
     */
    private Party party;

    /**
     * Usuário que saiu da party.
     */
    private User user;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeString(CoreConstants.GSON.toJson(this.party));
        buffer.writeInt(this.user.getId());
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        this.party = CoreConstants.GSON.fromJson(buffer.readString(), Party.class);
        this.user = CoreProvider.Cache.Local.USERS.provide().get(buffer.readInt());
    }

}
