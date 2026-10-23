package net.hyze.core.shared.group;

import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.hyze.core.shared.messages.MessageUtils;
import net.md_5.bungee.api.ChatColor;

import java.util.Calendar;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum Group {
    GAME_MASTER("Diretor", "Diretor", ChatColor.DARK_RED, 50),
    MANAGER("Admin", "Admin", ChatColor.RED, 49),
    ADMINISTRATOR("Admin", "Admin", ChatColor.RED, 48),
    MODERATOR("Moderador", "Moderador", ChatColor.GREEN, 47),
    HELPER("Suporte", "Suporte", ChatColor.DARK_AQUA, 46),
    YOUTUBER("Parceiro", "Parceiro", ChatColor.BLUE, 45),
    BUILDER("Construtor", "Construtor", ChatColor.GREEN, 44),
    //--
    HEAVENLY("Celestial", "Celestial", ChatColor.AQUA, 43),
    DIVINE("Divino", "Divino", ChatColor.GOLD, 42),
    ARCANE("Arcano", "Arcano", ChatColor.LIGHT_PURPLE, 41),
    //--
    DEFAULT("Membro", "", ChatColor.GRAY, 40),
    ;

    @Setter
    private String displayNameRaw = "";

    @Setter
    private String tagRaw = "";

    @Setter
    private ChatColor color = ChatColor.GRAY;

    @Setter
    private int priority = 0;

    public boolean isHigher(Group group) {
        return group != null && this.getPriority() > group.getPriority();
    }

    public boolean isSameOrHigher(Group group) {
        return group != null && this.getPriority() >= group.getPriority();
    }

    public String getTagStriped() {
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', this.tagRaw));
    }

    public String getTag() {
        return MessageUtils.translateColorCodes(this.tagRaw);
    }

    public String getDisplayNameStriped() {
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', this.displayNameRaw));
    }

    public String getDisplayName() {
        return MessageUtils.translateColorCodes(this.displayNameRaw);
    }

    public String getDisplayTag() {
        return MessageUtils.translateColorCodes(String.format(
                "%s[%s]",
                this.color,
                this.tagRaw
        ));
    }

    public String getDisplayTag(String nick) {
        if (this.tagRaw.isEmpty()) {
            return this.color + nick;
        }

        return MessageUtils.translateColorCodes(this.getDisplayTag() + " " + nick);
    }

    public static Group FALLBACK_GROUP = Group.DEFAULT;

    public static Optional<Group> getById(String id) {

        if (id != null) {
            for (Group group : values()) {
                if (group.name().equalsIgnoreCase(id)) {
                    return Optional.of(group);
                }
            }
        }

        return Optional.absent();
    }
}
