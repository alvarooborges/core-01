package net.hyze.core.spigot.misc.utils;

import com.google.common.collect.Sets;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Set;

public class EntityUtils {

    public static Set<Entity> getNearbyEntities(Entity entity, int x, int y, int z) {
        CraftEntity craftEntity = (CraftEntity) entity;

        AxisAlignedBB axisalignedbb = craftEntity.getHandle().getBoundingBox().grow(x, y, z);

        Set<Entity> out = Sets.newHashSet();

        LocationUtils.getNearbyEntities(entity.getWorld(), entity, axisalignedbb, out);

        return out;
    }

    public static boolean isStucked(LivingEntity entity) {
        for (int y = entity.getLocation().getBlockY(); y <= entity.getEyeLocation().getBlockY(); y++) {
            Location location = new Location(entity.getWorld(), entity.getLocation().getBlockX(), y, entity.getLocation().getBlockZ());
            Material type = location.getBlock().getType();
            if (type.isSolid() && !LocationUtils.BLOCKS_NONFULL_LIST.contains(type)) {
                return true;
            }
        }
        return false;
    }

    public static int getPlayerTotalExp(Player player) {
        int level = player.getLevel();
        long totalInLvl = level <= 16 ? Math.round(Math.pow(level, 2) + (6 * level))
                : (level <= 31
                ? Math.round((2.5 * Math.pow(level, 2)) - (40.5 * level) + 360)
                : Math.round((4.5 * Math.pow(level, 2)) - (162.5 * level) + 2220));
        return (int) totalInLvl + Math.round(player.getExpToLevel() * player.getExp());
    }

}
