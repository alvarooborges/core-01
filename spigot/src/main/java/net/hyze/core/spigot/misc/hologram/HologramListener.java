package net.hyze.core.spigot.misc.hologram;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.hyze.core.spigot.CoreSpigotPlugin;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.List;
import java.util.Map;

public class HologramListener implements Listener {

    private final Map<Pair<String, Long>, List<Hologram>> autoRespawn = Maps.newConcurrentMap();

    @EventHandler(priority = EventPriority.LOW)
    public void event(ChunkLoadEvent event) {

        long hash = toLong(event.getChunk().getX(), event.getChunk().getZ());

        List<Hologram> holograms;
        if ((holograms = autoRespawn.remove(Pair.of(event.getWorld().getName(), hash))) != null && !holograms.isEmpty()) {

            Bukkit.getScheduler().runTaskLater(CoreSpigotPlugin.getInstance(), () -> {
                for (Hologram hologram : holograms) {

                    if (hologram.isDestroyed()) {
                        continue;
                    }

                    hologram.spawn(hologram.getLocation());
                }
            }, 1);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void event(ChunkUnloadEvent event) {

        for (Entity entity : event.getChunk().getEntities()) {

            if (entity instanceof ArmorStand) {

                HologramLine line = HologramLine.SPAWNED_LINES.get(entity);

                if (line == null) {
                    continue;
                }

                Hologram hologram = line.getHologram();

                if (hologram.isDestroyed()) {
                    continue;
                }

                long hash = toLong(event.getChunk().getX(), event.getChunk().getZ());

                Pair<String, Long> key = Pair.of(event.getWorld().getName(), hash);

                List<Hologram> list = autoRespawn.getOrDefault(key, Lists.newArrayList());

                if (list.contains(hologram)) {
                    continue;
                }

                list.add(hologram);

                autoRespawn.put(key, list);

                for (HologramLine h : hologram.getLines()) {
                    h.destroy();
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void event(WorldUnloadEvent event) {

        for (Entity entity : event.getWorld().getEntities()) {
            if (entity instanceof ArmorStand) {

                HologramLine line = HologramLine.SPAWNED_LINES.get(entity);

                if (line == null) {
                    continue;
                }

                line.destroy();
            }
        }

        autoRespawn.keySet()
                .removeIf(pair -> pair.getLeft().equalsIgnoreCase(event.getWorld().getName()));
    }

    public static long toLong(int msw, int lsw) {
        return ((long) msw << 32) + lsw - Integer.MIN_VALUE;
    }
}
