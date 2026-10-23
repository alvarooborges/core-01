package net.hyze.core.spigot.misc.stackmobs.listeners;

import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import net.hyze.core.shared.messages.MessageUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChunkLoadListener implements Listener {

    @EventHandler
    public void on(ChunkLoadEvent event) {

        Set<Entity> entities = Sets.newHashSet(event.getChunk().getEntities());

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity) {
                String name = entity.getCustomName();

                if (name == null || name.isEmpty()) {
                    continue;
                }

                name = MessageUtils.stripColor(name);

                Pattern pattern = Pattern.compile("[0-9]+[x] ");
                Matcher matcher = pattern.matcher(name);

                if (!matcher.find()) {
                    continue;
                }

                String rawSize = matcher.group(0).replaceAll("[^\\d]", "");

                Integer size = Ints.tryParse(rawSize);

                if (size == null) {
                    continue;
                }

                if (size > 5) {
                    StackMobsListener.spawnNewEntity(size, 0, entity);
                }

                entity.remove();
            }
        }
    }
}
