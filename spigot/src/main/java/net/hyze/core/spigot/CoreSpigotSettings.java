package net.hyze.core.spigot;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.apps.AppType;
import net.hyze.core.shared.misc.utils.Position;
import net.hyze.core.shared.misc.utils.RandomUtils;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class CoreSpigotSettings {

    @Getter
    private static CoreSpigotSettings instance;

    @Singular
    private final Map<AppType, String> appTypeWorldNames;

    @Singular
    private final Map<String, Position> appSpawnPositions;

    @Singular
    private final Map<AppType, List<Position>> appTypeSpawnPositions;

    public Position getSpawnPosition() {
        if (appSpawnPositions.containsKey(CoreProvider.getApp().getId())) {
            return appSpawnPositions.get(CoreProvider.getApp().getId());
        }

        return getSpawnPosition(CoreProvider.getApp().getType());
    }

    public Position getSpawnPosition(AppType type) {
        if(!appTypeSpawnPositions.containsKey(type)){
            return new Position(0, 64, 0);
        }
        List<Position> positions = appTypeSpawnPositions.get(type);
        return RandomUtils.randomKey(positions);
    }

    public String getWorldName() {
        return getWorldName(CoreProvider.getApp().getType());
    }

    public String getWorldName(AppType type) {
        return appTypeWorldNames.getOrDefault(type, "world");
    }

    public static CoreSpigotSettingsBuilder builder() {
        return new Builder0();
    }

    public static class Builder0 extends CoreSpigotSettingsBuilder {

        @Override
        public CoreSpigotSettings build() {
            CoreSpigotSettings settings = super.build();

            CoreSpigotSettings.instance = settings;

            return settings;
        }
    }
}
