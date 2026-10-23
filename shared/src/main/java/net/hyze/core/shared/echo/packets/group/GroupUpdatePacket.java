package net.hyze.core.shared.echo.packets.group;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import dev.utils.shared.EnumUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.hyze.core.shared.group.Group;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GroupUpdatePacket extends AbstractEchoPacket {

    private Integer userId;
    private Integer targetId;
    private Group group;
    private GroupUpdateAction action;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(userId);
        buffer.writeInt(targetId);
        buffer.writeString(this.group.name());
        buffer.writeString(this.action.name());
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        this.userId = buffer.readInt();
        this.targetId = buffer.readInt();
        this.group = EnumUtils.of(Group.class, buffer.readString(), null);
        this.action = EnumUtils.of(GroupUpdateAction.class, buffer.readString(), null);
    }

}
