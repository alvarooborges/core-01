package net.hyze.core.shared.misc.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.apps.AppStatus;
import net.hyze.core.shared.apps.AppType;
import net.hyze.core.shared.apps.ServerStatus;
import net.hyze.core.shared.servers.Server;
import net.hyze.core.shared.user.User;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BalancerUtils {

    public static AppStatus fetchBestApp(User user, Server server, AppType type, User... friends) {

        Set<ServerStatus> statuses;

        if (server == null) {
            statuses = CoreProvider.Cache.Redis.APPS_STATUS.provide()
                    .fetchOnlineApps(type);
        } else {
            statuses = CoreProvider.Cache.Redis.APPS_STATUS.provide()
                    .fetchOnlineApps(server);
        }

        List<ServerStatus> filtered = statuses.stream()
                .filter(s -> s.getType().equals(type))
                .filter(s -> !s.isRestarting())
                .filter(s -> !s.isMaintenance())
                .filter(s -> s.getOnlineCount() < 250) // TODO tirar
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return null;
        }

        Collections.shuffle(filtered);

        return filtered.get(0);
    }
}
