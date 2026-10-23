package net.hyze.core.shared.log;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.user.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Log {

    public static class Cash {

        public static boolean add(User user, String serverId, String description, int postingValue, int previousBalance, int subsequentBalance) {
            return CoreProvider.Repositories.CASH_LOG.provide().insertLog(user, serverId, description, postingValue, previousBalance, subsequentBalance);
        }

    }

}
