package net.hyze.core.spigot.misc.scoreboard;

import lombok.NonNull;
import net.hyze.core.shared.misc.cooldowns.UserCooldowns;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.misc.combat.Combat;
import net.hyze.core.spigot.misc.combat.CombatManager;

public class ScoreboardUtils {

    public static boolean updateCooldowns(@NonNull User user, IBoardable boardable, int labelIndex, int combatIndex,
                                                                        int enderperlIndex) {

        Combat combat = CombatManager.getCombat(user);
        boolean enderPearlCooldown = !UserCooldowns.hasEnded(user, "enderpearl");

        if (combat != null || enderPearlCooldown) {
            boardable.getBoard().set(labelIndex, " &cTempo de Espera:");

            if (combat == null) {
                boardable.getBoard().reset(combatIndex);
            } else {
                boardable.getBoard().set(combatIndex, String.format("   &7▪ &fCombate: &a%ss", combat.getRemainingSeconds()));
            }

            if (!enderPearlCooldown) {
                boardable.getBoard().reset(enderperlIndex);
            } else {
                int secs = UserCooldowns.getSecondsLeft(user, "enderpearl");
                boardable.getBoard().set(enderperlIndex, String.format("   &7▪ &fPérola do Fim: &a%ss", secs));
            }

            return true;
        }

        return false;
    }
}
