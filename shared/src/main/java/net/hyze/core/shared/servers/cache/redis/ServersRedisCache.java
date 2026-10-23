package net.hyze.core.shared.servers.cache.redis;

import com.google.common.collect.Lists;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.cache.redis.RedisCache;
import net.hyze.core.shared.redis.RedisScriptManager;
import net.hyze.core.shared.servers.Server;
import redis.clients.jedis.Jedis;

import java.util.Collections;

public class ServersRedisCache implements RedisCache {

    public long countUsersByServer(Server server) {
        try (Jedis jedis = CoreProvider.Redis.REDIS_MAIN.provide().getResource()) {
            Object object = RedisScriptManager.execute(
                    jedis,
                    "countUsersByServer.lua",
                    Collections.emptyList(),
                    Lists.newArrayList(server.name())
            );

            if (object != null) {
                return (long) object;
            }

            return 0;
        }
    }

}
