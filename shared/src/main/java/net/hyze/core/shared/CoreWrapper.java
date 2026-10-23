package net.hyze.core.shared;

import lombok.Getter;
import lombok.Setter;
import net.hyze.core.shared.user.User;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class CoreWrapper {

    @Setter
    @Getter
    private static CoreWrapper wrapper = new EmptyCoreWrapper();

    public abstract void sendMessage(String player, String message);
    
    public abstract void sendMessage(String player, BaseComponent... message);

    public void sendMessage(User user, String message) {
        this.sendMessage(user.getNick(), message);
    }
}
