package net.hyze.core.spigot.misc.scoreboard.bukkit;

import com.google.common.collect.ImmutableSet;
import net.hyze.core.spigot.misc.scoreboard.IBoard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Set;

public interface IHealthBoard extends IBoard {

    default void updateHealth(boolean selfUpdate) {
        Player player = this.getPlayer();

        Set<? extends Player> players = ImmutableSet.copyOf(Bukkit.getOnlinePlayers());

        for (Player target : players) {

            if (selfUpdate) {
                Objective selfObjective = this.getScoreboard().getObjective(DisplaySlot.BELOW_NAME);

                if (selfObjective != null) {
                    selfObjective.getScore(target.getName()).setScore((int) target.getHealth());
                }
            }

            Scoreboard targetBoard = target.getScoreboard();

            if (targetBoard == null) {
                return;
            }

            Objective targetObjective = targetBoard.getObjective(DisplaySlot.BELOW_NAME);

            if (targetObjective == null) {
                return;
            }

            Score targetScore = targetObjective.getScore(player.getName());

            if (targetScore == null) {
                return;
            }

            targetScore.setScore((int) player.getHealth());
        }
    }

    default void registerHealth(Scoreboard scoreboard) {
        Objective healthObjective = scoreboard.registerNewObjective(ChatColor.RED + "❤", "dummy");
        healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }


    default void updateHealth() {
        updateHealth(false);
    }
}
