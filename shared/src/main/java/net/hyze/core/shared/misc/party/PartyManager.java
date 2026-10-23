package net.hyze.core.shared.misc.party;

import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.misc.party.echo.PartyDeletePacket;
import net.hyze.core.shared.misc.party.echo.PartyInvitePacket;
import net.hyze.core.shared.misc.party.echo.PartyJoinPacket;
import net.hyze.core.shared.misc.party.echo.PartyLeavePacket;
import net.hyze.core.shared.user.User;

public class PartyManager {

    public static void createParty(Party party) {
        PartyProvider.Cache.Redis.PARTY.provide().createParty(party);
        PartyProvider.Cache.Redis.PARTY.provide().updateUserParty(party.getLeader(), party);
    }

    public static void deleteParty(Party party) {
        PartyProvider.Cache.Redis.PARTY.provide().deleteParty(party);

        party.getUsers().forEach(
                targetId -> {
                    User targetUser = CoreProvider.Cache.Local.USERS.provide().get(targetId);
                    PartyProvider.Cache.Redis.PARTY.provide().updateUserParty(targetUser, null);
                }
        );

        PartyProvider.Cache.Redis.PARTY.provide().updateUserParty(party.getLeader(), null);
        CoreProvider.Redis.ECHO.provide().publish(new PartyDeletePacket(party));
    }

    public static Party getParty(User user) {
        return PartyProvider.Cache.Redis.PARTY.provide().fetchParty(user);
    }

    public static void invitePlayer(Party party, User user, User target) {
        party.getInvites().add(new PartyInvite(target));
        createParty(party);

        CoreProvider.Redis.ECHO.provide().publish(new PartyInvitePacket(party.getId().toString(), target, user));
    }

    public static void joinParty(Party party, User user) {
        Party previousParty = getParty(user);

        if (previousParty != null) {
            leaveParty(previousParty, user);
        }

        party.getUsers().add(user.getId());
        party.removeInvite(user);

        PartyProvider.Cache.Redis.PARTY.provide().updateParty(party);
        PartyProvider.Cache.Redis.PARTY.provide().updateUserParty(user, party);

        CoreProvider.Redis.ECHO.provide().publish(new PartyJoinPacket(user, party.getLeader()));
    }

    public static void leaveParty(Party party, User user) {
        if (party.isLeader(user)) {
            deleteParty(party);
            return;
        }

        boolean success = party.getUsers().remove(user.getId());

        if (!success) {
            throw new UnsupportedOperationException(user.getId() + " já não faz parte da party " + party.getId().toString());
        }

        PartyProvider.Cache.Redis.PARTY.provide().updateParty(party);
        PartyProvider.Cache.Redis.PARTY.provide().updateUserParty(user, null);

        CoreProvider.Redis.ECHO.provide().publish(new PartyLeavePacket(party, user));
    }

}
