package net.hyze.core.shared.group.storage;

import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.providers.MysqlDatabaseProvider;
import net.hyze.core.shared.servers.Server;
import net.hyze.core.shared.storage.repositories.MysqlRepository;
import net.hyze.core.shared.user.User;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public abstract class AbstractGroupRepository extends MysqlRepository {

    public AbstractGroupRepository(MysqlDatabaseProvider databaseProvider) {
        super(databaseProvider);
    }

    public abstract Map<Server, List<Group>> fetchByUser(User user, EnumSet<Server> servers);

    public abstract boolean addGroup(User user, Server server, Group group);

    public abstract boolean removeGroup(User user, Server server, Group group);
}
