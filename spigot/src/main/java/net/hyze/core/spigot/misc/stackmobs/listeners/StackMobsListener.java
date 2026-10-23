package net.hyze.core.spigot.misc.stackmobs.listeners;

import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.misc.stackmobs.StackMobsAPI;
import net.hyze.core.spigot.misc.stackmobs.StackedEntityWrapper;
import net.hyze.core.spigot.misc.stackmobs.events.StackMobDeathEvent;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class StackMobsListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void on(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity entity = (LivingEntity) event.getEntity();

        if (!entity.hasMetadata(StackMobsAPI.STACK_SIZE_TAG)) {
            return;
        }

        StackedEntityWrapper stacked = new StackedEntityWrapper(entity);

        double health = Math.max(0, entity.getHealth() - event.getFinalDamage());

        entity.setCustomName(StackMobsAPI.getMobName(entity, stacked.getSize(), health));
    }

    @EventHandler
    public void on(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }

        LivingEntity entity = event.getEntity();
        EntityType type = entity.getType();

        if (StackMobsAPI.NO_STACK_TYPES.contains(type)) {
            return;
        }

        StackedEntityWrapper stacked = new StackedEntityWrapper(entity);

        StackMobDeathEvent stackMobDeathEvent = new StackMobDeathEvent(stacked, 1, event);

        Bukkit.getPluginManager().callEvent(stackMobDeathEvent);

        int withdraw = stackMobDeathEvent.getDeathAmount();

        if (withdraw >= stacked.getSize()) {
            entity.remove();
            return;
        }

        Entity newEntity = spawnNewEntity(stacked.getSize(), withdraw, entity);
        entity.remove();

        if (newEntity != null) {
            StackMobsAPI.updateEntityName(newEntity);
        }
    }

    public static Entity spawnNewEntity(int oldSize, int withdraw, Entity dead) {

        dead.removeMetadata(StackMobsAPI.STACK_SIZE_TAG, CoreSpigotPlugin.getInstance());

        try {
            World world = ((CraftWorld) dead.getWorld()).getHandle();

            net.minecraft.server.v1_8_R3.Entity cloneHandle = ((CraftEntity) dead).getHandle().getClass()
                    .getConstructor(World.class)
                    .newInstance(world);

            cloneHandle.fromMobSpawner = true;

            Location location = dead.getLocation();

            cloneHandle.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

            Entity clone = cloneHandle.getBukkitEntity();

            clone.setMetadata(StackMobsAPI.NEW_STACK_ENTITY_TAG, new FixedMetadataValue(CoreSpigotPlugin.getInstance(), true));
            world.addEntity(cloneHandle, CreatureSpawnEvent.SpawnReason.CUSTOM);

            StackMobsAPI.setup(clone);

            if (dead instanceof LivingEntity) {
                LivingEntity deathLiving = (LivingEntity) dead;

                ((LivingEntity) clone).setMaxHealth(deathLiving.getMaxHealth());
                ((LivingEntity) clone).setHealth(deathLiving.getMaxHealth());
            }

            if (clone instanceof Zombie) {
                ((Zombie) clone).setBaby(false);
                ((Zombie) clone).setVillager(false);
            }

            if (clone instanceof Slime) {
                ((Slime) clone).setSize(((Slime) dead).getSize());
            }

            StackedEntityWrapper stacked = new StackedEntityWrapper(clone);
            stacked.setSize(oldSize - withdraw);

            ((LivingEntity) clone).setCanPickupItems(false);

            return clone;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }
}
