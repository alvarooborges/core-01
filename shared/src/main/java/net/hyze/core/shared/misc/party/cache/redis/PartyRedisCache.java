package net.hyze.core.shared.misc.party.cache.redis;

import net.hyze.core.shared.CoreConstants;
import net.hyze.core.shared.cache.redis.RedisCache;
import net.hyze.core.shared.misc.party.Party;
import net.hyze.core.shared.misc.party.PartyProvider;
import net.hyze.core.shared.user.User;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class PartyRedisCache implements RedisCache {

    private final String PARTY = "party";
    private final String PARTY_USERS = "party.users";

    public void createParty(Party party) {
        try (Jedis jedis = PartyProvider.Redis.REDIS.provide().getResource()) {
            Pipeline pipeline = jedis.pipelined();
            pipeline.hset(PARTY, party.getId().toString(), CoreConstants.GSON.toJson(party));
            pipeline.sync();
        }
    }

    public void deleteParty(Party party) {
        try (Jedis jedis = PartyProvider.Redis.REDIS.provide().getResource()) {
            Pipeline pipeline = jedis.pipelined();
            pipeline.hdel(PARTY, party.getId().toString());
            pipeline.sync();
        }
    }

    public void updateUserParty(User user, Party party) {
        try (Jedis jedis = PartyProvider.Redis.REDIS.provide().getResource()) {
            Pipeline pipeline = jedis.pipelined();

            if (party == null) {
                pipeline.hdel(PARTY_USERS, String.valueOf(user.getId()));
            } else {
                pipeline.hset(PARTY_USERS, String.valueOf(user.getId()), party.getId().toString());
            }

            pipeline.sync();
        }
    }

    public Party fetchParty(User user) {
        try (Jedis jedis = PartyProvider.Redis.REDIS.provide().getResource()) {

            String partyId = jedis.hget(PARTY_USERS, String.valueOf(user.getId()));

            if (partyId == null) {
                return null;
            }

            String partyString = jedis.hget(PARTY, partyId);

            if (partyString == null) {
                return null;
            }

            return CoreConstants.GSON.fromJson(partyString, Party.class);
        }
    }

    public void updateParty(Party party) {
        createParty(party);
    }
}
