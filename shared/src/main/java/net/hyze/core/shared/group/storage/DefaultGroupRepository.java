package net.hyze.core.shared.group.storage;

import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.group.storage.specs.DeleteGroupSpec;
import net.hyze.core.shared.group.storage.specs.InsertGroupSpec;
import net.hyze.core.shared.group.storage.specs.SelectGroupsByUserSpec;
import net.hyze.core.shared.providers.MysqlDatabaseProvider;
import net.hyze.core.shared.servers.Server;
import net.hyze.core.shared.user.User;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class DefaultGroupRepository extends AbstractGroupRepository {

    public DefaultGroupRepository(MysqlDatabaseProvider databaseProvider) {
        super(databaseProvider);
    }

    public Map<Server, List<Group>> fetchByUser(User user, EnumSet<Server> servers) {
        return query(new SelectGroupsByUserSpec(user, servers));
    }

    public boolean addGroup(User user, Server server, Group group) {
        return query(new InsertGroupSpec(user, server, group));
    }

    public boolean removeGroup(User user, Server server, Group group) {
        return query(new DeleteGroupSpec(user, server, group));
    }
}
