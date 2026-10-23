package net.hyze.core.spigot.misc.utils;

import com.comphenix.packetwrapper.WrapperPlayServerChat;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

public class ActionBarMessage {

    private String text;


    public ActionBarMessage text(String text) {
        this.text = ChatColor.translateAlternateColorCodes('&', text);
        return this;
    }

    public void send(Player... players) {
        WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromText(text);

        WrapperPlayServerChat wrapperPlayServerChat = new WrapperPlayServerChat();
        wrapperPlayServerChat.setMessage(wrappedChatComponent);
        wrapperPlayServerChat.setPosition((byte) 2);

        for (Player player : players) {
            if (player.isOnline()) {
                wrapperPlayServerChat.sendPacket(player);
            }
        }
    }
}
