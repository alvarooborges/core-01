package net.hyze.core.spigot;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import dev.utils.echo.EchoOptions;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.echo.packets.user.connect.ConnectConsent;
import net.hyze.core.shared.echo.packets.user.connect.UserConnectHandShakePacket;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.misc.utils.TranslateItem;
import net.hyze.core.spigot.world.location.unserializer.BukkitLocationParser;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class CoreSpigotConstants {

    public static final TranslateItem TRANSLATE_ITEM = new TranslateItem();

    public static boolean STOPPING = false;

    public static final BukkitLocationParser LOCATION_PARSER = new BukkitLocationParser();

    public static BiFunction<Player, Player, Boolean> IS_HOSTILE = (p1, p2) -> true;

    public static final EnumMap<GameMode, Predicate<User>> ALLOW_GAMEMODE = Maps.newEnumMap(GameMode.class);

    static {
        Predicate<User> managersOnly = user -> {
            if (user.hasGroup(Group.MANAGER)) {
                return true;
            }

            Player player = Bukkit.getPlayerExact(user.getNick());

            return player != null && player.isOp();
        };

        ALLOW_GAMEMODE.put(GameMode.CREATIVE, managersOnly);
        ALLOW_GAMEMODE.put(GameMode.ADVENTURE, managersOnly);
        ALLOW_GAMEMODE.put(GameMode.SPECTATOR, user -> {
            return user.hasGroup(Group.MODERATOR) || user.hasStrictGroup(Group.BUILDER);
        });
    }

    public static class NBTKeys {

        // Entity
        public static final String ENTITY_OWNER_DAMAGE = "custom_entity_owner_damage";

        // double
        public static final String ENTITY_DAMAGE = "custom_entity_damage";

        // double
        public static final String ENTITY_TRUE_DAMAGE = "custom_entity_true_damage";

        // boolean
        public static final String PLAYER_FALL_DAMAGE_BYPASS = "player_fall_damage_bypass";

        // int
        public static final String CUSTOM_AMOUNT = "custom_amount";

    }

    public static class Databases {

        public static class Mysql {

            public static class Tables {

                public static final String USER_DATA_TABLE_NAME = "user_data";
            }
        }
    }
}
