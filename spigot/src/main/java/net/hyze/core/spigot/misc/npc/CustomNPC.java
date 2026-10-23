package net.hyze.core.spigot.misc.npc;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.npc.MetadataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.misc.hologram.Hologram;
import net.hyze.core.spigot.misc.hologram.HologramPosition;
import net.hyze.core.spigot.misc.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class CustomNPC {

    public static final List<CustomNPC> INSTANCES = Lists.newArrayList();

    @Getter
    private final String name;

    private final EntityType entityType;

    @Setter
    @Getter
    protected String id;

    @Setter
    @Getter
    protected Location location;

    @Setter
    protected Location lookAt;

    @Setter
    protected boolean lookClose = true;

    @Getter
    protected NPC npc;

    protected Hologram hologram;

    public CustomNPC(EntityType entityType, Location location) {
        this(entityType, location, location);
    }

    public CustomNPC(EntityType entityType, Location location, Location lookAt) {
        this.name = "[NPC]";
        this.entityType = entityType;
        this.location = location.clone();
        this.lookAt = lookAt.clone();

        INSTANCES.add(this);
    }

    public double getHologramOffset() {
        return 2.3;
    }

    public void spawn() {
        this.npc = CoreSpigotPlugin.getInstance().getNpcRegistry().createNPC(entityType, this.name);

        MetadataStore data = this.npc.data();
        data.set("NPC_WRAPPER", this);
        data.setPersistent(NPC.AMBIENT_SOUND_METADATA, "");
        data.setPersistent(NPC.HURT_SOUND_METADATA, "");
        data.setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, false);

        this.npc.getTrait(LookClose.class).lookClose(this.lookClose);

        Location finalLocation = this.location.clone();

        if (this.lookAt != null) {
            finalLocation = finalLocation.setDirection(this.lookAt.toVector().subtract(this.location.toVector()));
        }

        if (!finalLocation.getChunk().isLoaded()) {
            finalLocation.getChunk().load();
        }

        preSpawn();

        this.npc.spawn(finalLocation);

        postSpawn();

        if (this.getClass().isAnnotationPresent(NPCInfo.class)) {

            NPCInfo info = this.getClass().getAnnotation(NPCInfo.class);

            if (!info.id().isEmpty()) {
                this.setId(info.id());
            }

            List<String> lines = Lists.newLinkedList();

            if (!info.name().isEmpty()) {
                lines.add(info.name());
            }

            if (info.lines().length > 0) {

                lines.addAll(Arrays.asList(info.lines()));

                if (this instanceof IExtraHologram) {
                    lines.addAll(Arrays.asList(((IExtraHologram) this).getExtraHologramLines()));
                }
            }

            if (!lines.isEmpty()) {

                if (hologram != null) {
                    hologram.destroy();
                }

                Location hologramLocation = location.clone().add(0, getHologramOffset(), 0);
                hologram = new Hologram(HologramPosition.UP);

                Collections.reverse(lines);

                for (String line : lines) {
                    hologram.line(line);
                }

                hologram.spawn(hologramLocation);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (npc.getEntity() == null || npc.getEntity().isDead()) {
                            return;
                        }

                        if (hologram != null) {
                            hologram.teleport(npc.getEntity().getLocation().clone().add(0, getHologramOffset(), 0));
                        }
                    }
                }.runTaskTimer(CoreSpigotPlugin.getInstance(), 20, 20 * 10);
            }
        }
    }

    protected void preSpawn() {
    }

    protected void postSpawn() {
    }

    public void teleport(Location location) {

        if (this.npc != null) {
            this.npc.destroy();
        }

        this.location = location;

        if (hologram != null) {
            hologram.teleport(location.clone().add(0, getHologramOffset(), 0));
        }

        spawn();
    }

    public void handleClick(User user, NPCClickEvent event) {

    }
}