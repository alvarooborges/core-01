package net.hyze.core.spigot;

import net.hyze.core.shared.CoreWrapper;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CoreSpigotWrapper extends CoreWrapper {

    @Override
    public void sendMessage(String playerName, String message) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null) {
            player.sendMessage(message);
        }
    }

    @Override
    public void sendMessage(String playerName, BaseComponent... message) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null) {
            player.spigot().sendMessage(message);
        }
    }
}
