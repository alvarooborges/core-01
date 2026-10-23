package net.hyze.core.shared.apps;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.misc.report.ReportManager;

import java.io.IOException;
import java.util.Map;

@Getter
@JsonSerialize(using = AppType.Serializer.class)
@JsonDeserialize(using = AppType.Deserializer.class)
public class AppType {

    private static final Map<String, AppType> APP_TYPE_MAP = Maps.newHashMap();

    public static FactionsAppType FACTIONS_WORLD = new FactionsAppType("FACTIONS_WORLD", true, true);
    public static FactionsAppType FACTIONS_SPAWN = new FactionsAppType("FACTIONS_SPAWN", true, true);
    public static FactionsAppType FACTIONS_MINE = new FactionsAppType("FACTIONS_MINE", true, false);
    public static FactionsAppType FACTIONS_VIP = new FactionsAppType("FACTIONS_VIP", true, false);
    public static FactionsAppType FACTIONS_END = new FactionsAppType("FACTIONS_END", true, false);
    public static FactionsAppType FACTIONS_NETHER = new FactionsAppType("FACTIONS_NETHER", true, true);
    public static FactionsAppType FACTIONS_TESTS = new FactionsAppType("FACTIONS_TESTS", true, false);
    public static FactionsAppType FACTIONS_EXCAVATION = new FactionsAppType("FACTIONS_EXCAVATION", true, false);
    public static FactionsAppType FACTIONS_WAR = new FactionsAppType("FACTIONS_WAR", true, false);
    public static FactionsAppType FACTIONS_LOSTFORTRESS = new FactionsAppType("FACTIONS_LOSTFORTRESS", true, false);
    public static FactionsAppType FACTIONS_ARENA = new FactionsAppType("FACTIONS_ARENA", true, false);
    public static FactionsAppType FACTIONS_SAFE = new FactionsAppType("FACTIONS_SAFE", true, false);
    public static FactionsAppType FACTIONS_DUNGEON_HUB = new FactionsAppType("FACTIONS_DUNGEON_HUB", true, false);

    public static AppType LOBBY = new AppType("LOBBY", true);
    public static AppType PROXY = new AppType("PROXY", false);
    public static AppType GENERIC = new AppType("GENERIC", false);
    public static AppType DUNGEON = new AppType("DUNGEON", true);
    public static AppType DUNGEON_MASTER = new AppType("DUNGEON_MASTER", false);
    public static AppType DUNGEON_SLAVE = new AppType("DUNGEON_SLAVE", false);
    public static AppType BUILD = new AppType("BUILD", true);

    private final String id;
    private final boolean allowProxyRegistry;

    public AppType(String id, boolean allowProxyRegistry) {
        this.id = id;
        this.allowProxyRegistry = allowProxyRegistry;

        APP_TYPE_MAP.put(id, this);
    }

    public boolean isCurrent() {
        return CoreProvider.getApp().getType() == this;
    }

    public static AppType getById(String id) {
        return id == null ? null : APP_TYPE_MAP.get(id);
    }

    public static boolean anyCurrent(AppType... types) {
        for (AppType type : types) {
            if (type.isCurrent()) {
                return true;
            }
        }

        return false;
    }

    @Getter
    public static class FactionsAppType extends AppType {

        private final boolean allowClaim;

        public FactionsAppType(String id, boolean allowProxyRegistry, boolean allowClaim) {
            super(id, allowProxyRegistry);

            this.allowClaim = allowClaim;
        }

        public static boolean isCurrentAllowClaim() {
            return CoreProvider.getApp().getType() instanceof FactionsAppType
                    && ((FactionsAppType) CoreProvider.getApp().getType()).isAllowClaim();
        }
    }

    public static class Serializer extends StdSerializer<AppType> {

        protected Serializer() {
            super(AppType.class);
        }

        @Override
        public void serialize(AppType value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.getId());
        }

    }

    public static class Deserializer extends StdDeserializer<AppType> {

        protected Deserializer() {
            super(AppType.class);
        }

        @Override
        public AppType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return AppType.getById(p.getValueAsString());
        }

    }
}
