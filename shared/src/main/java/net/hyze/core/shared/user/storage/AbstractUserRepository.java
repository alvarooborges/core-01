package net.hyze.core.shared.user.storage;

import java.util.List;
import java.util.UUID;
import net.hyze.core.shared.providers.MysqlDatabaseProvider;
import net.hyze.core.shared.storage.repositories.MysqlRepository;
import net.hyze.core.shared.user.User;

public abstract class AbstractUserRepository extends MysqlRepository {

    public AbstractUserRepository(MysqlDatabaseProvider databaseProvider) {
        super(databaseProvider);
    }

    public abstract User create(String nick, UUID uuid, String plainPassword);

    public abstract User insertOrUpdate(User user);

    public abstract void update(User user);

    public abstract User fetchById(Integer id);

    public abstract User fetchByNick(String nick);

    public abstract User fetchByEmail(String email);

    public abstract Long fetchLastLogin(User user);

    public abstract List<User> fetchAssociateUsers(User user, int days);

    public abstract int fetchCash(User user);

    public abstract boolean decrementCash(User user, int amount);

    public abstract boolean incrementCash(User user, int amount);

    public abstract boolean defineCash(User user, int amount);

}
