package net.hyze.core.shared.misc.party;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.user.User;

@Getter
@AllArgsConstructor
public class Party {

    private final UUID id;

    private final Integer leaderUserId;

    private final List<Integer> users = Lists.newArrayList();

    private final List<PartyInvite> invites = Lists.newArrayList();

    public Party(User user) {
        this(UUID.randomUUID(), user.getId());
    }

    public PartyInvite getInvite(User target) {
        return this.invites.stream()
                .filter(invite -> invite.getUserId().equals(target.getId()))
                .findFirst()
                .orElse(null);
    }

    public boolean isLeader(User target) {
        return this.leaderUserId.equals(target.getId());
    }

    public User getLeader() {
        return CoreProvider.Cache.Local.USERS.provide().get(this.leaderUserId);
    }

    public void removeInvite(User target) {
        this.invites.removeIf(partyInvite -> partyInvite.getUserId().equals(target.getId()));
    }

    public List<User> getEveryone() {
        List<User> everyone = Lists.newArrayList();

        everyone.add(getLeader());

        getUsers().forEach(
                user -> everyone.add(CoreProvider.Cache.Local.USERS.provide().get(user))
        );

        return everyone;
    }
}
