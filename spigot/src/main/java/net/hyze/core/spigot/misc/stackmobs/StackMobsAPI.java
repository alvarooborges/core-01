package net.hyze.core.spigot.misc.stackmobs;

import com.google.common.collect.Sets;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.apps.AppType;
import net.hyze.core.shared.messages.MessageUtils;
import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.CoreSpigotSettings;
import net.hyze.core.spigot.misc.stackmobs.listeners.ChunkLoadListener;
import net.hyze.core.spigot.misc.stackmobs.listeners.CreatureSpawnListener;
import net.hyze.core.spigot.misc.stackmobs.listeners.StackMobsListener;
import net.hyze.core.spigot.misc.stackmobs.tasks.MergeTask;
import net.hyze.core.spigot.misc.utils.NMS;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class StackMobsAPI {

    public static final String STACK_SIZE_TAG = "stackmobs:stack-size";
    public static final String NEW_STACK_ENTITY_TAG = "stackmobs:stack-size";
    public static final String PREVENT_STACK_TAG = "stackmobs:prevent-stack";
    public static final String SINGLE_KILL_TAG = "stackmobs:single-kill";

    public static Vector CHECK_AREA = new Vector(7, 4, 7);

    public static final Set<CreatureSpawnEvent.SpawnReason> NO_STACK_REASONS = Sets.newConcurrentHashSet();
    public static final Set<EntityType> NO_STACK_TYPES = Sets.newConcurrentHashSet();

    private final static Set<AppType> appTypes = Sets.newHashSet();

    private static boolean ENABLED = false;

    public static void disable() {
        ENABLED = false;
    }

    public static void enable(AppType appType, AppType... appTypes) {
        if (ENABLED) {
            return;
        }

        StackMobsAPI.appTypes.add(appType);
        StackMobsAPI.appTypes.addAll(Arrays.asList(appTypes));

        if (!StackMobsAPI.appTypes.contains(CoreProvider.getApp().getType())) {
            return;
        }

        ENABLED = true;

        PluginManager pluginManager = CoreSpigotPlugin.getInstance().getServer().getPluginManager();
        pluginManager.registerEvents(new CreatureSpawnListener(), CoreSpigotPlugin.getInstance());
        pluginManager.registerEvents(new StackMobsListener(), CoreSpigotPlugin.getInstance());
        pluginManager.registerEvents(new ChunkLoadListener(), CoreSpigotPlugin.getInstance());

        new MergeTask().runTaskTimer(CoreSpigotPlugin.getInstance(), 0, 20 * 3);
    }

    public static StackedEntityWrapper getStackedEntity(Entity entity) {
        return new StackedEntityWrapper(entity);
    }


    public static void updateEntityName(Entity entity) {
        StackedEntityWrapper stacked = new StackedEntityWrapper(entity);

        if (!stacked.isStackingPrevented() && stacked.hasStackSizeTag()) {
            entity.setCustomName(StackMobsAPI.getMobName(entity, stacked.getSize()));
            entity.setCustomNameVisible(true);
        }
    }

    public static boolean handle(Entity entity, CreatureSpawnEvent.SpawnReason reason, Location location, int maxStackSize) {
        return handle(entity, reason, location, maxStackSize, CHECK_AREA);
    }

    public static boolean handle(Entity entity, CreatureSpawnEvent.SpawnReason reason, Location location, int maxStackSize, Vector checkRadius) {

        StackedEntityWrapper stacked = new StackedEntityWrapper(entity);

        if (stacked.isStackingPrevented()) {
            return false;
        }

        if (stacked.getSize() >= maxStackSize) {
            return true;
        }

        EntityType type = entity.getType();

        if (type == EntityType.ARMOR_STAND || type == EntityType.CREEPER) {
            return false;
        }

        if (NO_STACK_REASONS.contains(reason)) {
            return false;
        }

        for (Entity nearby : location.getWorld().getNearbyEntities(location, checkRadius.getX(), checkRadius.getY(), checkRadius.getZ())) {

            if (type != nearby.getType() || nearby.isDead()) {
                continue;
            }

            if (type == EntityType.SKELETON) {
                Skeleton nearbySkeleton = (Skeleton) nearby;
                Skeleton skeleton = (Skeleton) entity;

                if (nearbySkeleton.getSkeletonType() != skeleton.getSkeletonType()) {
                    continue;
                }
            }

            StackedEntityWrapper nearbyStacked = new StackedEntityWrapper(nearby);

            if (nearbyStacked.isStackingPrevented()) {
                continue;
            }

            if (nearbyStacked.getSize() >= maxStackSize) {
                return true;
            }

            nearbyStacked.setSize(Math.min(nearbyStacked.getSize() + 1, maxStackSize));
            nearby.setCustomName(StackMobsAPI.getMobName(nearby, nearbyStacked.getSize()));

            return true;
        }

        return false;
    }

    public static void setup(Entity entity) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entity).getHandle();

        if (nmsEntity instanceof EntityInsentient) {
            NMS.clearEntitySelectors((EntityInsentient) nmsEntity);
        }

        if (nmsEntity instanceof EntityLiving) {
            EntityLiving livingEntity = (EntityLiving) nmsEntity;

            if (livingEntity.getAttributeMap().a(GenericAttributes.ATTACK_DAMAGE) == null) {
                livingEntity.getAttributeMap().b(GenericAttributes.ATTACK_DAMAGE);
            }

            livingEntity.getAttributeMap().a(GenericAttributes.ATTACK_DAMAGE).setValue(0D);

            nmsEntity.getDataWatcher().watch(15, (byte) 1);
        }

        StackedEntityWrapper stacked = new StackedEntityWrapper(entity);
        entity.setCustomName(StackMobsAPI.getMobName(entity, stacked.getSize()));
    }

    public static Set<Entity> getLoadedEntities() {
        Set<Entity> entities = Sets.newHashSet();

        for (Player player : Bukkit.getOnlinePlayers()) {
            entities.addAll(player.getNearbyEntities(20, 20, 20));
        }

        return entities.stream()
                .filter(entity -> CoreSpigotSettings.getInstance().getWorldName().equalsIgnoreCase(entity.getWorld().getName()))
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> entity.hasMetadata(STACK_SIZE_TAG))
                .collect(Collectors.toSet());
    }

    public static String getMobName(Entity entity, int size) {
        return getMobName(entity, size, ((LivingEntity) entity).getHealth());
    }

    public static String getMobName(Entity entity, int size, double health) {
        EntityType type = entity.getType();
        String[] names = type.name().replace("_", " ").toLowerCase().split(" ");
        String name = "";

        for (String name1 : names) {
            name = name1.substring(0, 1).toUpperCase() + name1.substring(1).toLowerCase() + " ";
        }

        if (name.equals("")) {
            name = type.name().replace("_", " ").substring(0, 1).toUpperCase() + type.name().replace("_", " ").substring(1).toLowerCase();
        }

        return MessageUtils.translateColorCodes(String.format(
                "&e%sx %s &7| &f%.0f &c❤",
                size,
                name.trim(),
                health
        ));
    }
}
