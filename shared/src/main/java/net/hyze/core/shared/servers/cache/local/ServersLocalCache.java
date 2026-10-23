package net.hyze.core.shared.servers.cache.local;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.cache.local.LocalCache;
import net.hyze.core.shared.cache.local.utils.CaffeineScheduler;
import net.hyze.core.shared.servers.Server;

import java.util.concurrent.TimeUnit;

public class ServersLocalCache implements LocalCache {

    private final LoadingCache<Server, Long> ONLINE_COUNT_CACHE = Caffeine.newBuilder()
            .scheduler(CaffeineScheduler.getInstance())
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build(server -> {
                return CoreProvider.Cache.Redis.SERVERS.provide().countUsersByServer(server);
            });

    public long getOnlineCount(Server server) {
        return ONLINE_COUNT_CACHE.get(server);
    }
}
