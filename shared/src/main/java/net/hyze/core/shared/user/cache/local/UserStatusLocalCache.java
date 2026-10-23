package net.hyze.core.shared.user.cache.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Maps;
import lombok.NonNull;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.apps.App;
import net.hyze.core.shared.apps.BukkitStatus;
import net.hyze.core.shared.cache.local.LocalCache;
import net.hyze.core.shared.cache.local.utils.CaffeineScheduler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UserStatusLocalCache implements LocalCache {

    protected final Cache<String, Map<String, String>> CACHE_BY_ID = Caffeine.newBuilder()
            .scheduler(CaffeineScheduler.getInstance())
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build();

    private String get(String nick, String key) {
        Map<String, String> map = CACHE_BY_ID.getIfPresent(nick);

        if (map != null) {
            return map.get(key);
        }

        return null;
    }

    private void put(String nick, String key, String value) {
        Map<String, String> map = CACHE_BY_ID.getIfPresent(nick);

        if (map == null) {
            map = Maps.newHashMap();
        }

        map.put(key, value);

        CACHE_BY_ID.put(nick, map);
    }

    public BukkitStatus getBukkitStatus(@NonNull String nick) {

        String appId = get(nick, "bukkit_app");

        if (appId != null) {
            return CoreProvider.Cache.Local.APPS_STATUS.provide().fetch(appId, BukkitStatus.class);
        }

        App app = CoreProvider.Cache.Redis.USERS_STATUS.provide().getBukkitApp(nick);

        if (app != null) {
            put(nick, "server", app.getId());

            return CoreProvider.Cache.Local.APPS_STATUS.provide().fetch(app.getId(), BukkitStatus.class);
        }

        return null;
    }
}
