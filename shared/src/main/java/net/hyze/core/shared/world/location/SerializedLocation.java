package net.hyze.core.shared.world.location;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.*;
import net.hyze.core.shared.CoreConstants;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.apps.App;
import net.hyze.core.shared.misc.utils.Position;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Setter
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SerializedLocation extends Position implements Cloneable {

    private String appId;

    @NonNull
    private String worldName = "world";

    public SerializedLocation(String appId, String worldName, double x, double y, double z) {
        this(appId, worldName, x, y, z, 0, 0);
    }

    public SerializedLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        this(null, worldName, x, y, z, yaw, pitch);
    }

    public SerializedLocation(String worldName, double x, double y, double z) {
        this(null, worldName, x, y, z, 0, 0);
    }

    public SerializedLocation(String appId, @NonNull String worldName, double x, double y, double z, float yaw, float pitch) {
        super(x, y, z, yaw, pitch);

        this.appId = appId;
        this.worldName = worldName;
    }

    public <U extends LocationParser<T>, T> T parser(U parser) {
        return parser.apply(this);
    }

    public App getApp() {
        return CoreProvider.Cache.Local.APPS.provide().get(appId);
    }

    @Override
    public String toString() {
        try {
            return CoreConstants.JACKSON.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static SerializedLocation of(String appId, String worldName, Position position) {
        return new SerializedLocation(
                appId,
                worldName,
                position.getX(),
                position.getY(),
                position.getZ(),
                position.getYaw(),
                position.getPitch()
        );
    }

    public static SerializedLocation of(String string) {
        if (string != null) {
            try {
                return CoreConstants.JACKSON.readValue(string, SerializedLocation.class);
            } catch (IOException ex) {
                Logger.getGlobal().log(Level.SEVERE, null, ex);
            }
        }

        return null;
    }

    @Override
    public SerializedLocation clone() {
        return new SerializedLocation(appId, worldName, x, y, z, yaw, pitch);
    }
}
