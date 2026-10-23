package net.hyze.core.shared.misc.party.echo;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.hyze.core.shared.CoreConstants;
import net.hyze.core.shared.misc.party.Party;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PartyDeletePacket extends AbstractEchoPacket {

    /**
     * Id da party.
     */
    private Party party;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeString(CoreConstants.GSON.toJson(this.party));
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        this.party = CoreConstants.GSON.fromJson(buffer.readString(), Party.class);
    }

}
