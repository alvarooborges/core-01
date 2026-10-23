package net.hyze.core.shared.misc.party;

import net.hyze.core.shared.misc.party.cache.redis.PartyRedisCache;
import net.hyze.core.shared.providers.RedisCacheProvider;
import net.hyze.core.shared.providers.RedisProvider;

public class PartyProvider {

    public static void prepare(RedisProvider redisProvider) {
        Redis.REDIS = redisProvider;

        Cache.Redis.PARTY.prepare();
    }

    public static class Redis {

        public static RedisProvider REDIS;

    }

    protected static class Cache {

        protected static class Redis {

            protected static final RedisCacheProvider<PartyRedisCache> PARTY = new RedisCacheProvider(
                    new PartyRedisCache()
            );

        }
    }

}
