package net.hyze.core.spigot.misc.npc;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class NPCScoreboard {

    private static Scoreboard scoreboard;
    private static Team team;

    public static void setup() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        team = scoreboard.registerNewTeam("npcs");
        team.setNameTagVisibility(NameTagVisibility.NEVER);
    }

    public static Scoreboard getScoreboard() {
        return scoreboard;
    }

    public static Team getTeam() {
        return team;
    }

}