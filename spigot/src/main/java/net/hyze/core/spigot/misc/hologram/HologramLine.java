package net.hyze.core.spigot.misc.hologram;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.hyze.core.shared.messages.MessageUtils;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Map;
import java.util.WeakHashMap;

@Getter
@RequiredArgsConstructor
public class HologramLine {

    public static final Map<Entity, HologramLine> SPAWNED_LINES = new WeakHashMap<>();

    private ArmorStand armorStand;
    private String text;

    private final Hologram hologram;

    public HologramLine text(String text) {
        this.text = text;
        return this;
    }

    public HologramLine update(String text) {
        this.text = text;
        this.update();
        return this;
    }

    public HologramLine update() {
        if (this.armorStand != null) {
            this.armorStand.setCustomName(MessageUtils.translateColorCodes(this.text));
        }

        return this;
    }

    public void spawn(Location location) {

        if (this.armorStand != null) {
            destroy();
        }

        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) location.getWorld()).getHandle();

        HologramArmorStand hologramArmorStand = new HologramArmorStand(world);

        hologramArmorStand.setPosition(location.getX(), location.getY(), location.getZ());

        world.addEntity(hologramArmorStand, CreatureSpawnEvent.SpawnReason.CUSTOM);

        this.armorStand = (ArmorStand) hologramArmorStand.getBukkitEntity();

        SPAWNED_LINES.put(this.armorStand, this);

        this.update();
    }

    public void destroy() {
        if (this.armorStand != null) {
            this.armorStand.remove();
            this.armorStand = null;
        }
    }
}
