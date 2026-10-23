package net.hyze.core.shared.echo.packets;

import com.google.common.collect.Sets;
import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Optional;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendMessagePacket extends AbstractEchoPacket {

    protected Set<Integer> usersIds;
    protected BaseComponent[] components;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(Optional.ofNullable(this.usersIds).map(Set::size).orElse(0));

        if (this.usersIds != null) {
            this.usersIds.forEach(buffer::writeInt);
        }

        buffer.writeString(ComponentSerializer.toString(this.components));
    }

    @Override
    public void read(EchoByteBufferInput buffer) {

        this.usersIds = Sets.newHashSet();

        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            this.usersIds.add(buffer.readInt());
        }

        this.components = ComponentSerializer.parse(buffer.readString());
    }
}
