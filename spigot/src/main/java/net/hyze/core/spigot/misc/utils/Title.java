package net.hyze.core.spigot.misc.utils;

import com.comphenix.packetwrapper.WrapperPlayServerTitle;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import net.hyze.core.shared.messages.MessageUtils;
import net.hyze.core.shared.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@Builder
public class Title {

    @Builder.Default
    private final String title = ChatColor.RESET.toString();

    @Builder.Default
    private final String subTitle = ChatColor.RESET.toString();

    private final int fadeIn;

    private final int fadeOut;

    @Builder.Default
    private int stay = 1;

    @Builder.Default
    private final boolean translateColorCodes = true;

    public void send(Player... players) {
        List<WrapperPlayServerTitle> packets = Lists.newLinkedList();

        if (fadeIn > 0 || stay > 0 || fadeOut > 0) {
            WrapperPlayServerTitle wrapper = new WrapperPlayServerTitle();
            wrapper.setAction(EnumWrappers.TitleAction.TIMES);
            wrapper.setFadeIn(fadeIn);
            wrapper.setFadeOut(fadeOut);
            wrapper.setStay(stay);
            packets.add(wrapper);
        }

        {
            WrapperPlayServerTitle wrapper = new WrapperPlayServerTitle();
            wrapper.setAction(EnumWrappers.TitleAction.TITLE);
            wrapper.setTitle(WrappedChatComponent.fromText(
                    translateColorCodes
                            ? MessageUtils.translateFormat(title)
                            : title
            ));
            packets.add(wrapper);
        }

        {
            WrapperPlayServerTitle wrapper = new WrapperPlayServerTitle();
            wrapper.setAction(EnumWrappers.TitleAction.SUBTITLE);
            wrapper.setTitle(WrappedChatComponent.fromText(
                    translateColorCodes
                            ? MessageUtils.translateFormat(subTitle)
                            : subTitle
            ));
            packets.add(wrapper);
        }

        for (Player player : players) {
            for (WrapperPlayServerTitle wrapper : packets) {
                wrapper.sendPacket(player);
            }
        }
    }

    public void send(User... users) {
        Arrays.stream(users).map(user -> Bukkit.getPlayerExact(user.getNick())).filter(Objects::nonNull).forEach(this::send);
    }

    public void send() {
        ImmutableSet.copyOf(Bukkit.getOnlinePlayers()).forEach(this::send);
    }

    public static void clear(Player... players) {
        WrapperPlayServerTitle wrapper = new WrapperPlayServerTitle();
        wrapper.setAction(EnumWrappers.TitleAction.CLEAR);

        for (Player player : players) {
            wrapper.sendPacket(player);
        }
    }

}
