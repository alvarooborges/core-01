package net.hyze.core.shared.apps.cache.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.apps.App;
import net.hyze.core.shared.apps.AppStatus;
import net.hyze.core.shared.cache.local.LocalCache;
import net.hyze.core.shared.cache.local.utils.CaffeineScheduler;

import java.util.concurrent.TimeUnit;

public class AppStatusLocalCache implements LocalCache {

    private final Cache<String, AppStatus> CACHE_BY_ID = Caffeine.newBuilder()
            .scheduler(CaffeineScheduler.getInstance())
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .build();

    @SuppressWarnings("unchecked")
    public <T extends AppStatus> T fetch(App app, Class<T> statusClass) {
        return (T) CACHE_BY_ID.get(app.getId(), id -> CoreProvider.Cache.Redis.APPS_STATUS.provide().fetch(id, statusClass));
    }

    @SuppressWarnings("unchecked")
    public <T extends AppStatus> T fetch(String appId, Class<T> statusClass) {
        return (T) CACHE_BY_ID.get(appId, id -> CoreProvider.Cache.Redis.APPS_STATUS.provide().fetch(id, statusClass));
    }
}
