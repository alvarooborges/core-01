package net.hyze.core.shared.echo.packets.user;

import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import dev.utils.echo.packet.AbstractEchoPacketResponse;
import dev.utils.echo.packet.IEchoRespondable;
import lombok.*;

@Getter
@NoArgsConstructor
@RequiredArgsConstructor
public class UserOnlineVerifyPacket extends AbstractEchoPacket
        implements IEchoRespondable<UserOnlineVerifyPacket.VerifyResponse> {

    @NonNull
    private Integer userId;

    @Setter
    @Getter
    private VerifyResponse response;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        buffer.writeInt(userId);
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        userId = buffer.readInt();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyResponse extends AbstractEchoPacketResponse {

        private boolean isOnline;

        @Override
        public void write(EchoByteBufferOutput buffer) {
            buffer.writeBoolean(isOnline);
        }

        @Override
        public void read(EchoByteBufferInput buffer) {
            isOnline = buffer.readBoolean();
        }
    }
}
