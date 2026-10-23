package net.hyze.core.spigot.misc.hologram;

import com.google.common.collect.Lists;

import java.util.LinkedList;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

@RequiredArgsConstructor
public class Hologram {

    @Getter
    private final LinkedList<HologramLine> lines = Lists.newLinkedList();

    private final HologramPosition hologramPosition;

    @Getter
    private boolean destroyed;

    @Getter
    private Location location;

    public Hologram line(String text) {
        lines.add(new HologramLine(this).text(text));
        return this;
    }

    public Hologram line(int position, String text) {
        lines.add(position, new HologramLine(this).text(text));
        return this;
    }

    public void update(int position, String text) {
        if (this.lines.get(position) != null) {
            this.lines.get(position).update(text);
        }
    }

    public void update() {
        this.lines.forEach(HologramLine::update);
    }

    public void spawn(Location location) {
        this.location = location.clone();

        location.getChunk().load();

        Location hologramLocation = location.clone();

        for (HologramLine hologramLine : this.lines) {
            hologramLine.spawn(hologramLocation);
            hologramLocation = hologramLocation.add(0, this.hologramPosition.getValue(), 0);
        }
    }

    public void destroy() {
        this.destroyed = true;
        this.lines.forEach(HologramLine::destroy);
    }

    public void teleport(Location loc) {
        this.location = loc.clone();

        Location hologramLocation = loc.clone();

        for (HologramLine hologramLine : this.lines) {
            ArmorStand armorStand = hologramLine.getArmorStand();

            if (armorStand != null) {
                armorStand.teleport(hologramLocation);
            }

            hologramLocation = hologramLocation.add(0, this.hologramPosition.getValue(), 0);
        }
    }
}
