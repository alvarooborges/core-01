package net.hyze.core.shared.apps;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.hyze.core.shared.apps.AppType;
import net.hyze.core.shared.apps.ServerStatus;
import net.hyze.core.shared.servers.Server;

import java.net.InetSocketAddress;

@NoArgsConstructor
public class BukkitStatus extends ServerStatus {

    @Setter
    @Getter
    private Double recentTps = 0.0;

    public BukkitStatus(String appId, AppType type, Server server, InetSocketAddress address, boolean maintenance) {
        super(appId, type, server, address);
        this.maintenance = maintenance;
    }
}
