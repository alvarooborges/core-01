package net.hyze.core.spigot.echo.packets;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import java.util.Optional;

import dev.utils.echo.BufferIO;
import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import dev.utils.echo.packet.AbstractEchoPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerInfoDataUpdatePacket extends AbstractEchoPacket {

    private WrappedGameProfile profile;
    private int latency;
    private EnumWrappers.NativeGameMode gameMode;
    private WrappedChatComponent displayName;

    @Override
    public void write(EchoByteBufferOutput buffer) {
        BufferIO.writeUUID(buffer, this.profile.getUUID());
        buffer.writeString(this.profile.getName());
        buffer.writeInt(this.latency);
        BufferIO.writeEnum(buffer, this.gameMode);

        buffer.writeString(Optional.ofNullable(this.displayName).map(WrappedChatComponent::getJson).orElse(null));
    }

    @Override
    public void read(EchoByteBufferInput buffer) {
        this.profile = new WrappedGameProfile(
                BufferIO.readUUID(buffer),
                buffer.readString()
        );
        this.latency = buffer.readInt();
        this.gameMode = BufferIO.readEnum(buffer, EnumWrappers.NativeGameMode.class, EnumWrappers.NativeGameMode.SURVIVAL);

        String json = buffer.readString();

        if (json != null) {
            this.displayName = WrappedChatComponent.fromJson(buffer.readString());
        }
    }

    public static PlayerInfoDataUpdatePacket fromData(PlayerInfoData data) {
        return new PlayerInfoDataUpdatePacket(data.getProfile(), data.getLatency(), data.getGameMode(), data.getDisplayName());
    }
}
