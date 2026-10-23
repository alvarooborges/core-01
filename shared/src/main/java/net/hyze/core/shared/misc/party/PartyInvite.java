package net.hyze.core.shared.misc.party;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.user.User;

@Getter
@RequiredArgsConstructor
public class PartyInvite {

    /**
     * Usuário que foi convidado.
     */
    private final Integer userId;

    /**
     * Horário que o convite foi criado.
     */
    private final Long createdAt;

    public PartyInvite(User user) {
        this(user.getId(), System.currentTimeMillis());
    }

}
