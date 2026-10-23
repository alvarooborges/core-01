package net.hyze.core.shared.echo.packets;

import com.google.common.collect.Sets;
import dev.utils.echo.BufferIO;
import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import dev.utils.shared.EnumUtils;
import lombok.*;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.servers.Server;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BroadcastMessagePacket extends AbstractEchoPacket {

    protected BaseComponent[] components;

    @Singular
    protected Set<Group> groups;
    protected boolean groupStrict;
    protected Server server;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeString(ComponentSerializer.toString(this.components));

        buffer.writeInt(groups.size());

        for (Group group : groups) {
            BufferIO.writeEnum(buffer, group);
        }

        buffer.writeBoolean(groupStrict);

        buffer.writeBoolean(server != null);
        if (server != null) {
            buffer.writeString(server.name());
        }
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        this.components = ComponentSerializer.parse(buffer.readString());

        this.groups = Sets.newHashSet();

        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            Group group = BufferIO.readEnum(buffer, Group.class);

            if (group != null) {
                groups.add(group);
            }
        }

        this.groupStrict = buffer.readBoolean();

        if (buffer.readBoolean()) {
            this.server = EnumUtils.of(Server.class, buffer.readString(), null);
        }
    }

}
