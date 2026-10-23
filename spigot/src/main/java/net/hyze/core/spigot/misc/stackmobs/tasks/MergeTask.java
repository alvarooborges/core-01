package net.hyze.core.spigot.misc.stackmobs.tasks;

import net.hyze.core.spigot.misc.stackmobs.StackMobsAPI;
import net.hyze.core.spigot.misc.stackmobs.StackedEntityWrapper;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.scheduler.BukkitRunnable;

public class MergeTask extends BukkitRunnable {

    @Override
    public void run() {
        for (Entity entity : StackMobsAPI.getLoadedEntities()) {
            if (!entity.isDead()) {
                StackedEntityWrapper stacked = new StackedEntityWrapper(entity);

                if (stacked.getSize() >= 300) {
                    continue;
                }

                if (!stacked.isStackingPrevented() && stacked.hasStackSizeTag()) {

                    double xLoc = StackMobsAPI.CHECK_AREA.getX();
                    double yLoc = StackMobsAPI.CHECK_AREA.getY();
                    double zLoc = StackMobsAPI.CHECK_AREA.getZ();

                    Location loc = entity.getLocation();

                    EntityType type = entity.getType();

                    for (Entity nearby : loc.getWorld().getNearbyEntities(loc, xLoc, yLoc, zLoc)) {
                        if (type != nearby.getType() || nearby.isDead()) {
                            continue;
                        }

                        if (nearby.getEntityId() == entity.getEntityId()) {
                            continue;
                        }

                        StackedEntityWrapper nearbyStacked = new StackedEntityWrapper(nearby);

                        if (nearbyStacked.isStackingPrevented()) {
                            continue;
                        }

                        if (stacked.getSize() + nearbyStacked.getSize() >= 300) {
                            continue;
                        }

                        if (type == EntityType.SKELETON) {
                            Skeleton skeleton = (Skeleton) entity;
                            Skeleton nearbySkeleton = (Skeleton) nearby;

                            if (skeleton.getSkeletonType() != nearbySkeleton.getSkeletonType()) {
                                continue;
                            }
                        }

                        stacked.setSize(stacked.getSize() + nearbyStacked.getSize());
                        nearby.remove();
                    }
                }
            }
        }
    }
}
