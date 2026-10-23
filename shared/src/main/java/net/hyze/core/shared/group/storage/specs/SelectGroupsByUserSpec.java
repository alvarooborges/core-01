package net.hyze.core.shared.group.storage.specs;

import com.google.common.base.Enums;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.CoreConstants;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.servers.Server;
import net.hyze.core.shared.storage.repositories.specs.SelectSqlSpec;
import net.hyze.core.shared.user.User;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class SelectGroupsByUserSpec extends SelectSqlSpec<Map<Server, List<Group>>> {

    private final User user;
    private final EnumSet<Server> servers;

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {

        return connection -> {

            StringBuilder builder = new StringBuilder();

            for (Server server : this.servers) {
                builder.append(String.format(
                        " `server_id` = '%s' OR",
                        server.getId()
                ));
            }

            String in = builder.toString();

            in = in.substring(0, in.length() - 2);

            String query = String.format(
                    "SELECT * FROM `%s` WHERE `user_id` = ? AND (%s);",
                    CoreConstants.Databases.Mysql.Tables.GROUP_USER_TABLE_NAME,
                    in
            );

         //   System.out.println(query);

            PreparedStatement statement = connection.prepareStatement(query);

            statement.setInt(1, this.user.getId());

            return statement;
        };
    }

    @Override
    public ResultSetExtractor<Map<Server, List<Group>>> getResultSetExtractor() {
        return (ResultSet rs) -> {
            Map<Server, List<Group>> out = Maps.newHashMap();

            while (rs.next()) {
                String serverId = rs.getString("server_id");
                String groupId = rs.getString("group_id");

                Optional<Server> optinal = Server.getById(serverId);

                if (optinal.isPresent()) {
                    Group group = Enums.getIfPresent(Group.class, groupId).orNull();

                    if (group != null) {
                        List groups = out.getOrDefault(optinal.get(), Lists.newArrayList());

                        groups.add(group);

                        out.put(optinal.get(), groups);
                    }
                }
            }

            for (Server server : this.servers) {
                if (!out.containsKey(server)) {
                    out.put(server, Lists.newArrayList(Group.DEFAULT));
                }
            }

            return out;
        };
    }
}