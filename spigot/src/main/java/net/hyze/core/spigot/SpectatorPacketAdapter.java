package net.hyze.core.spigot;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;

public class SpectatorPacketAdapter extends PacketAdapter {

    public SpectatorPacketAdapter(Plugin plugin) {
        super(plugin, PacketType.Play.Server.PLAYER_INFO);
    }
}
