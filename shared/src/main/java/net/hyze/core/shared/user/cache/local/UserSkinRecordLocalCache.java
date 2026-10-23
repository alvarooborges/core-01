package net.hyze.core.shared.user.cache.local;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.cache.local.LocalCache;
import net.hyze.core.shared.cache.local.utils.CaffeineScheduler;
import net.hyze.core.shared.skins.SkinRecord;
import net.hyze.core.shared.user.User;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class UserSkinRecordLocalCache implements LocalCache {

    private final LoadingCache<User, Optional<SkinRecord>> CACHE = Caffeine.newBuilder()
            .scheduler(CaffeineScheduler.getInstance())
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build(user -> Optional.ofNullable(CoreProvider.Repositories.SKINS.provide().fetch(user)));

    public SkinRecord get(User user) {
        return Optional.ofNullable(CACHE.get(user))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElse(null);
    }
}
