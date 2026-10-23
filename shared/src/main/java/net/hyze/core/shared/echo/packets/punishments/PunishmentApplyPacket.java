package net.hyze.core.shared.echo.packets.punishments;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PunishmentApplyPacket extends AbstractEchoPacket {

    private Integer userId;
    private Integer applierId;
    private String reason;
    private String proof;
    private Long duration;
    private String type;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(this.userId);
        buffer.writeInt(this.applierId);
        buffer.writeString(this.reason);
        buffer.writeString(this.proof);
        buffer.writeLong(this.duration);
        buffer.writeString(this.type);
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        this.userId = buffer.readInt();
        this.applierId = buffer.readInt();
        this.reason = buffer.readString();
        this.proof = buffer.readString();
        this.duration = buffer.readLong();
        this.type = buffer.readString();
    }

}
