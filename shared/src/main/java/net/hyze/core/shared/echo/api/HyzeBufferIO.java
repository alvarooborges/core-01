package net.hyze.core.shared.echo.api;

import dev.utils.echo.BufferIO;
import dev.utils.echo.buffer.EchoByteBufferInput;
import dev.utils.echo.buffer.EchoByteBufferOutput;
import net.hyze.core.shared.apps.App;
import net.hyze.core.shared.apps.AppType;
import net.hyze.core.shared.misc.utils.Position;
import net.hyze.core.shared.servers.Server;
import net.hyze.core.shared.world.location.SerializedLocation;

import java.util.Optional;

public class HyzeBufferIO {

    public static void writeSerializedLocation(EchoByteBufferOutput buffer, SerializedLocation location) {
        buffer.writeString(Optional.ofNullable(location).map(SerializedLocation::toString).orElse(null));
    }

    public static SerializedLocation readSerializedLocation(EchoByteBufferInput buffer) {
        String str = buffer.readString();

        if (str != null) {
            return SerializedLocation.of(str);
        }

        return null;
    }

    public static void writeApp(EchoByteBufferOutput buffer, App app) {
        if (app != null) {
            buffer.writeBoolean(true);
            buffer.writeString(app.getId());
            buffer.writeString(app.getDisplayName());
            BufferIO.writeString(buffer, app.getType().getId());
            BufferIO.writeAddress(buffer, app.getAddress());
            BufferIO.writeEnum(buffer, app.getServer());
        } else {
            buffer.writeBoolean(false);
        }
    }

    public static App readApp(EchoByteBufferInput buffer) {
        boolean valid = buffer.readBoolean();

        if (!valid) {
            return null;
        }

        return new App(
                buffer.readString(),
                buffer.readString(),
                AppType.getById(buffer.readString()),
                BufferIO.readAddress(buffer),
                BufferIO.readEnum(buffer, Server.class)
        );
    }

    public static void writePosition(EchoByteBufferOutput buffer, Position position) {

        if(position == null) {
            buffer.writeBoolean(false);
            return;
        }

        buffer.writeBoolean(true);
        buffer.writeDouble(position.getX());
        buffer.writeDouble(position.getY());
        buffer.writeDouble(position.getZ());
        buffer.writeFloat(position.getYaw());
        buffer.writeFloat(position.getPitch());
    }


    public static Position readPosition(EchoByteBufferInput buffer) {

        if (!buffer.readBoolean()) {
            return null;
        }

        return new Position(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readFloat(),
                buffer.readFloat()
        );
    }
}
