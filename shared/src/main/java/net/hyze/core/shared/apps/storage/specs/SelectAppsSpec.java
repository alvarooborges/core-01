package net.hyze.core.shared.apps.storage.specs;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.hyze.core.shared.apps.App;
import net.hyze.core.shared.apps.AppType;
import net.hyze.core.shared.servers.Server;
import net.hyze.core.shared.storage.repositories.specs.SelectSqlSpec;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.net.InetSocketAddress;
import java.sql.ResultSet;
import java.util.List;

public abstract class SelectAppsSpec extends SelectSqlSpec<List<App>> {

    @Override
    public ResultSetExtractor<List<App>> getResultSetExtractor() {
        return (ResultSet result) -> {
            List<App> out = Lists.newArrayList();

            while (result.next()) {
                String applicationType = result.getString("type");

                AppType optional = AppType.getById(applicationType);

                if (optional != null) {

                    Server server = null;
                    String serverId = result.getString("server_id");

                    if (serverId != null) {
                        server = Server.getById(serverId).orElse(null);
                    }

                    App app = new App(
                            result.getString("id"),
                            result.getString("display_name"),
                            optional,
                            new InetSocketAddress(result.getString("host"), result.getInt("port")),
                            server
                    );

                    if (server != null) {
                        server.addApp(app);
                    }

                    out.add(app);
                }
            }

            return out;
        };
    }
}
