package net.hyze.core.spigot.misc.utils;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.Collection;

public class PlayerUtils {

    public static void clearInventory(Player player) {
        player.setItemOnCursor(null);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
    }

    public static void clearEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    public static void healPlayer(Player player) {
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
    }

    public static void clear(Player player) {
        clearInventory(player);
        clearEffects(player);
        healPlayer(player);

        player.setExhaustion(0F);
        player.setFireTicks(0);
    }

    public static Player getLookingAt(Player player) {
        return getLookingAt(player, 5);
    }

    public static Player getLookingAt(Player player, int range) {
        return getLookingAt(player, 1, range);
    }

    public static Player getLookingAt(Player player, int minRange, int maxRange) {
        if (player == null || !player.isOnline()) {
            return null;
        }

        minRange = Math.max(Math.min(minRange, maxRange), 1);
        maxRange = Math.max(minRange, maxRange);

        Vector dir = player.getLocation().getDirection();
        Location loc = player.getEyeLocation().clone();

        for (int i = 0; i < minRange; i++) {
            loc.add(dir.getX(), dir.getY(), dir.getZ());
        }

        try {
            for (int i = minRange; i <= maxRange; i++) {
                loc.add(dir.getX(), dir.getY(), dir.getZ());

                Collection<Entity> nearby = LocationUtils.getNearbyEntities(loc, 1, 1, 1);

                if (!nearby.isEmpty()) {

                    for (Entity target : nearby) {
                        if (target != null) {
                            if (target instanceof Player && target.isValid() && player.canSee((Player) target)) {
                                return (Player) target;
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // EXP
    // Source: https://gist.github.com/Jikoo/30ec040443a4701b8980

    /**
     * Calculates a player's total exp based on level and progress to next.
     *
     * @param player the Player
     * @return the amount of exp the Player has
     * @see https://minecraft.gamepedia.com/Experience#Leveling_up
     */
    public static int getExp(Player player) {
        return getExpFromLevel(player.getLevel())
                + Math.round(getExpToNext(player.getLevel()) * player.getExp());
    }

    /**
     * Calculates total experience based on level.
     *
     * @param level the level
     * @return the total experience calculated
     * @see https://minecraft.gamepedia.com/Experience#Leveling_up
     * <p>
     * "One can determine how much experience has been collected to reach a level using the equations:
     * <p>
     * Total Experience = [Level]2 + 6[Level] (at levels 0-15)
     * 2.5[Level]2 - 40.5[Level] + 360 (at levels 16-30)
     * 4.5[Level]2 - 162.5[Level] + 2220 (at level 31+)"
     */
    public static int getExpFromLevel(int level) {
        if (level > 30) {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
        if (level > 15) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        }
        return level * level + 6 * level;
    }

    /**
     * Calculates level based on total experience.
     *
     * @param exp the total experience
     * @return the level calculated
     */
    public static double getLevelFromExp(long exp) {
        if (exp > 1395) {
            return (Math.sqrt(72 * exp - 54215) + 325) / 18;
        }
        if (exp > 315) {
            return Math.sqrt(40 * exp - 7839) / 10 + 8.1;
        }
        if (exp > 0) {
            return Math.sqrt(exp + 9) - 3;
        }
        return 0;
    }

    /**
     * @see https://minecraft.gamepedia.com/Experience#Leveling_up
     * <p>
     * "The formulas for figuring out how many experience orbs you need to get to the next level are as follows:
     * Experience Required = 2[Current Level] + 7 (at levels 0-15)
     * 5[Current Level] - 38 (at levels 16-30)
     * 9[Current Level] - 158 (at level 31+)"
     */
    private static int getExpToNext(int level) {
        if (level > 30) {
            return 9 * level - 158;
        }
        if (level > 15) {
            return 5 * level - 38;
        }
        return 2 * level + 7;
    }

    /**
     * Change a Player's exp.
     * <p>
     * This method should be used in place of {@link Player#giveExp(int)}, which does not properly
     * account for different levels requiring different amounts of experience.
     *
     * @param player the Player affected
     * @param exp    the amount of experience to add or remove
     */
    public static void changeExp(Player player, int exp) {
        exp += getExp(player);

        if (exp < 0) {
            exp = 0;
        }

        double levelAndExp = getLevelFromExp(exp);

        int level = (int) levelAndExp;
        player.setLevel(level);
        player.setExp((float) (levelAndExp - level));
    }

}
