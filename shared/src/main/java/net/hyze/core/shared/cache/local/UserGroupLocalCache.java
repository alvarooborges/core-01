package net.hyze.core.shared.cache.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.cache.local.utils.CaffeineScheduler;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.servers.Server;
import net.hyze.core.shared.user.User;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class UserGroupLocalCache implements LocalCache {

    private final Cache<User, LoadingCache<Server, List<Group>>> CACHE = Caffeine.newBuilder()
            .weakKeys()
            .build();

    public List<Group> get(@NonNull User user, @NonNull Server server) {
        return getCache(user).get(server);
    }

    /**
     * Função optimizada pra buscar todos os grupos de uma lista de app.
     * <p>
     * Caso não exista cache de grupos para um ou mais app solicitado, a função
     * irá buscar de uma só vez no banco de dados.
     */
    public SetMultimap<Server, Group> get(@NonNull User user, Server... servers) {
        SetMultimap<Server, Group> out = HashMultimap.create();

        for (Server server : servers) {
            LoadingCache<Server, List<Group>> cache = getCache(user);

            List<Group> groups = cache.get(server);

            if (groups != null) {
                out.putAll(server, groups);
            }
        }

        return out;
    }

    public void remove(@NonNull User user) {
        CACHE.invalidate(user);
    }

    private LoadingCache<Server, List<Group>> getCache(User user) {
        LoadingCache<Server, List<Group>> cache = CACHE.getIfPresent(user);

        if (cache == null) {
            cache = Caffeine.newBuilder()
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .scheduler(CaffeineScheduler.getInstance())
                    .build(server -> {
                        return CoreProvider.Repositories.GROUPS.provide()
                                .fetchByUser(user, EnumSet.of(server))
                                .get(server);
                    });

            CACHE.put(user, cache);
        }

        return cache;
    }
}
