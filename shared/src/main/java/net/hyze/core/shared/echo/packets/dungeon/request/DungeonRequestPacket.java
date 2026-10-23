package net.hyze.core.shared.echo.packets.dungeon.request;

import com.google.common.collect.Sets;
import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import dev.utils.echo.packet.AbstractEchoPacketResponse;
import dev.utils.echo.packet.IEchoRespondable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@NoArgsConstructor
@AllArgsConstructor
public class DungeonRequestPacket extends AbstractEchoPacket implements IEchoRespondable<DungeonRequestPacket.RequestCallback> {

    @Getter
    private int leaderId;

    @Getter
    private Collection<Integer> members;

    @Getter
    private String mapId;

    @Setter
    @Getter
    private RequestCallback response;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(leaderId);
        buffer.writeInt(members.size());

        for(Integer memberId : members) {
            buffer.writeInt(memberId);
        }

        buffer.writeString(this.mapId);
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        this.leaderId = buffer.readInt();
        this.members = Sets.newHashSet();

        int size = buffer.readInt();

        for(int i = 0; i < size; i++) {
            this.members.add(buffer.readInt());
        }

        this.mapId = buffer.readString();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class RequestCallback extends AbstractEchoPacketResponse {

        @Getter
        private boolean success;

        @Getter
        private String dungeon;

        @Override
        public void write(EchoByteBufferOutput buffer) {
            buffer.writeBoolean(success);
            buffer.writeString(dungeon);
        }

        @Override
        public void read(EchoByteBufferInput buffer) {
            this.success = buffer.readBoolean();
            this.dungeon = buffer.readString();
        }
    }

}
