package net.hyze.core.spigot.misc.utils;

import net.hyze.core.spigot.CoreSpigotPlugin;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class BossBarMessage extends BukkitRunnable {

    private String title;

    private final Map<Player, EntityWither> withers = new HashMap<>();

    public BossBarMessage(String title) {
        this.title = title;

        runTaskTimer(CoreSpigotPlugin.getInstance(), 0, 10);
    }

    public void addPlayer(Player player) {
        if (withers.containsKey(player)) {
            return;
        }

        EntityWither wither = new EntityWither(((CraftWorld) player.getWorld()).getHandle());

        Location location = getWitherLocation(player.getLocation());

        wither.setCustomName(title);
        wither.setInvisible(true);
        wither.setLocation(location.getX(), location.getY(), location.getZ(), 0, 0);

        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(wither);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

        withers.put(player, wither);
    }

    public void removePlayer(Player player) {
        EntityWither wither = withers.remove(player);

        if (wither != null) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(wither.getId());

            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void clear() {
        withers.forEach((player, entityWither) -> {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entityWither.getId());

            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        });

        withers.clear();
    }

    public void setTitle(String title) {
        this.title = title;

        for (Map.Entry<Player, EntityWither> entry : withers.entrySet()) {
            EntityWither wither = entry.getValue();

            wither.setCustomName(title);

            PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(wither.getId(), wither.getDataWatcher(), true);
            ((CraftPlayer) entry.getKey()).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void setProgress(double progress) {
        for (Map.Entry<Player, EntityWither> entry : withers.entrySet()) {

            EntityWither wither = entry.getValue();
            wither.setHealth((float) (progress * wither.getMaxHealth()));

            PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(wither.getId(), wither.getDataWatcher(), true);

            ((CraftPlayer) entry.getKey()).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public Location getWitherLocation(Location location) {
        return location.add(location.getDirection().multiply(60));
    }

    @Override
    public void run() {
        for (Map.Entry<Player, EntityWither> entry : withers.entrySet()) {

            EntityWither wither = entry.getValue();
            Player player = entry.getKey();

            Location location = getWitherLocation(player.getLocation());

            wither.setLocation(location.getX(), location.getY(), location.getZ(), 0, 0);
            PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(wither);

            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

}