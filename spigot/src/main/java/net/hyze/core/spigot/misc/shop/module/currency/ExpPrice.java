package net.hyze.core.spigot.misc.shop.module.currency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.misc.shop.module.AbstractModule;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import net.hyze.core.spigot.misc.utils.PlayerUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class ExpPrice extends AbstractPrice {

    @Getter
    private final int exp;

    @Override
    public AbstractModule.State state(User user) {

        Player player = Bukkit.getPlayerExact(user.getNick());

        if (player == null || !player.isOnline()) {
            return AbstractModule.State.ERROR;
        }

        if (PlayerUtils.getExp(player) < exp) {
            return AbstractModule.State.ERROR;
        }

        return AbstractModule.State.SUCCESS;
    }

    @Override
    public String format() {
        return String.format(
                "%s%s XP %s(%.1f níveis)",
                ChatColor.GOLD.toString(),
                this.exp,
                ChatColor.DARK_GRAY,
                PlayerUtils.getLevelFromExp(this.exp)
        );
    }

    @Override
    public ItemBuilder buildIcon(User user) {
        ItemBuilder icon = ItemBuilder.of(Material.GOLD_INGOT);

        icon.name((this.state(user) == AbstractModule.State.SUCCESS ? "&a" : "&c") + this.getName());

        if (this.state(user) == AbstractModule.State.SUCCESS) {
            icon.lore("&7Você gastará " + this.format() + "&7.");
        } else {
            icon.lore("Você não possui XP")
                    .lore("suficiente para efetuar")
                    .lore("esta compra.");
        }

        return icon;
    }

    @Override
    public String getName() {
        return "XP";
    }

    @Override
    public boolean transaction(User user) {

        Player player = Bukkit.getPlayerExact(user.getNick());

        if (player == null || !player.isOnline()) {
            return false;
        }

        if (PlayerUtils.getExp(player) < exp) {
            return false;
        }

        PlayerUtils.changeExp(player, -this.exp);

        return true;
    }

    @Override
    public boolean needsConfirmation() {
        return false;
    }
}
