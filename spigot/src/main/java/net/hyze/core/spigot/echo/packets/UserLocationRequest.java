package net.hyze.core.spigot.echo.packets;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import dev.utils.echo.packet.AbstractEchoPacketResponse;
import dev.utils.echo.packet.IEchoRespondable;
import lombok.*;
import net.hyze.core.shared.echo.api.HyzeBufferIO;
import net.hyze.core.shared.world.location.SerializedLocation;
import net.hyze.core.spigot.echo.packets.UserLocationRequest.UserLocationResponse;

@NoArgsConstructor
@RequiredArgsConstructor
public class UserLocationRequest extends AbstractEchoPacket implements IEchoRespondable<UserLocationResponse> {

    @Getter
    @Setter
    private UserLocationResponse response;

    @NonNull
    @Getter
    private Integer userId;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(userId);
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        userId = buffer.readInt();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserLocationResponse extends AbstractEchoPacketResponse {

        @Getter
        private SerializedLocation location;

        @Override
        public void write(EchoByteBufferOutput buffer) {
            HyzeBufferIO.writeSerializedLocation(buffer, this.location);
        }

        @Override
        public void read(EchoByteBufferInput buffer) {
            this.location = HyzeBufferIO.readSerializedLocation(buffer);
        }
    }
}
