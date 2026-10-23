package net.hyze.core.spigot.misc.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public interface IBoard {

    Player getPlayer();

    Scoreboard getScoreboard();

    void set(int score, String text);

    void setTitle(String title);

    void reset(int score);

}
