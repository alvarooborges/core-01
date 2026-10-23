package net.hyze.core.shared.user.cache.local;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.cache.local.CredentialLocalCache;
import net.hyze.core.shared.cache.local.utils.CaffeineScheduler;
import net.hyze.core.shared.servers.Server;
import net.hyze.core.shared.user.User;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class UserLocalCache extends CredentialLocalCache<User> {

    private final LoadingCache<Server, Set<User>> CACHE_BY_SERVER;

    public UserLocalCache() {
        this(-1L);
    }

    public UserLocalCache(Long ttlInMillis) {
        super(ttlInMillis);

        Caffeine<Object, Object> caffeine = Caffeine.newBuilder();

        if (ttlInMillis > 0) {
            caffeine.scheduler(CaffeineScheduler.getInstance())
                    .expireAfterAccess(10, TimeUnit.SECONDS);
        }

        CACHE_BY_SERVER = caffeine.build(CoreProvider.Cache.Redis.USERS_STATUS.provide()::fetchUsersByServer);
    }

    public Set<User> getOnlineUsersByServer(Server server) {
        return ImmutableSet.copyOf(CACHE_BY_SERVER.get(server));
    }

    @Override
    public CacheLoader<String, User> getLoaderByNick() {
        return (@NonNull String nick) -> {
            User user = CoreProvider.Repositories.USERS.provide().fetchByNick(nick);

            return user;
        };
    }

    @Override
    public CacheLoader<Integer, User> getLoaderById() {
        return (@NonNull Integer id) -> {
            User user = CoreProvider.Repositories.USERS.provide().fetchById(id);

            return user;
        };
    }
}
