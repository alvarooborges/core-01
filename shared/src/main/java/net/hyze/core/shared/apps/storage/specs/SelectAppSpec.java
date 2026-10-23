package net.hyze.core.shared.apps.storage.specs;

import net.hyze.core.shared.apps.App;
import net.hyze.core.shared.apps.AppType;
import net.hyze.core.shared.servers.Server;
import net.hyze.core.shared.storage.repositories.specs.SelectSqlSpec;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.net.InetSocketAddress;
import java.sql.ResultSet;

public abstract class SelectAppSpec extends SelectSqlSpec<App> {

    @Override
    public ResultSetExtractor<App> getResultSetExtractor() {
        return (ResultSet result) -> {
            while (result.next()) {
                String applicationType = result.getString("type");

                AppType appType = AppType.getById(applicationType);

                if (appType != null) {
                    Server server = null;
                    String serverId = result.getString("server_id");

                    if (serverId != null) {
                        server = Server.getById(serverId).orElse(null);
                    }

                    App app = new App(
                            result.getString("id"),
                            result.getString("display_name"),
                            appType,
                            new InetSocketAddress(result.getString("host"), result.getInt("port")),
                            server
                    );

                    if (server != null) {
                        server.addApp(app);
                    }

                    return app;
                }
            }

            return null;
        };
    }
}
