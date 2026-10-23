package net.hyze.core.shared.punishments.storage.specs.categories;

import com.google.common.base.Enums;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.punishments.PunishmentCategory;
import net.hyze.core.shared.punishments.PunishmentLevel;
import net.hyze.core.shared.punishments.PunishmentType;
import net.hyze.core.shared.storage.repositories.specs.SelectSqlSpec;
import org.springframework.jdbc.core.ResultSetExtractor;

@RequiredArgsConstructor
public abstract class SelectCategoriesSpec extends SelectSqlSpec<Set<PunishmentCategory>> {

    @Override
    public ResultSetExtractor<Set<PunishmentCategory>> getResultSetExtractor() {

        return (ResultSet result) -> {

            Set<PunishmentCategory> output = Sets.newHashSet();

            while (result.next()) {

                Integer id = result.getInt("id");
                String name = result.getString("name");
                String displayName = result.getString("display_name");
                Group group = Enums.getIfPresent(Group.class, result.getString("group_id")).or(Group.GAME_MASTER);
                String description = result.getString("description");
                LinkedList<PunishmentLevel> levels = parseLevels(result.getString("levels"));
                boolean enabled = result.getBoolean("enabled");
                
                output.add(new PunishmentCategory(id, name, displayName, group, description, levels, enabled));

            }

            return output;
        };

    }

    private LinkedList<PunishmentLevel> parseLevels(String rawLevels) {

        LinkedList<PunishmentLevel> output = Lists.newLinkedList();

        JsonArray array = (JsonArray) new JsonParser().parse(rawLevels);

        for (JsonElement jsonElement : array) {
            JsonObject object = jsonElement.getAsJsonObject();
            PunishmentType type = CoreProvider.Cache.Local.PUNISHMENTS.provide().getType(object.get("type").getAsString());

            if (type == null) {
                continue;
            }

            String duration = object.get("duration").getAsString();
            output.add(new PunishmentLevel(duration, type));
        }

        return output;

    }

}
