package net.hyze.core.shared.apps;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.hyze.core.shared.servers.Server;

import java.net.InetSocketAddress;
import java.util.Date;

@NoArgsConstructor
public class ServerStatus extends AppStatus {

    @Setter
    @Getter
    private Integer onlineCount = 0;

    @Setter
    @Getter
    private boolean restarting = false;

    public ServerStatus(String appId, AppType type, Server server, InetSocketAddress address) {
        super(appId, type, server, address, new Date(), 0, 0, false);

        this.onlineCount = 0;
    }
}
