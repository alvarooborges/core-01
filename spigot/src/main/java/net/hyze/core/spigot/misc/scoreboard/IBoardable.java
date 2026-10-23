package net.hyze.core.spigot.misc.scoreboard;

import org.bukkit.entity.Player;

public interface IBoardable {

    IBoard getBoard();

    void setBoard(IBoard board);

    Player getPlayer();
}
