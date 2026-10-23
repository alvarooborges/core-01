package net.hyze.core.shared.apps;

import lombok.*;
import net.hyze.core.shared.servers.Server;

import java.net.InetSocketAddress;
import java.util.Date;

@Getter
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"appId"})
public class AppStatus {

    @NonNull
    protected String appId;

    @NonNull
    protected AppType type;

    protected Server server;

    @NonNull
    protected InetSocketAddress address;

    @NonNull
    protected Date onlineSince;

    @NonNull
    @Setter
    protected long usageMemory, freeMemory;

    @Setter
    protected boolean maintenance;

    @Setter
    protected Date redisUpdatedAt;

    public AppStatus(@NonNull String appId, @NonNull AppType type, Server server, @NonNull InetSocketAddress address) {
        this(appId, type, server, address, new Date());
    }

    public AppStatus(@NonNull String appId, @NonNull AppType type, Server server, @NonNull InetSocketAddress address, @NonNull Date onlineSince) {
        this(appId, type, server, address, onlineSince, 0, 0, false);
    }

    public AppStatus(@NonNull String appId, @NonNull AppType type, Server server, @NonNull InetSocketAddress address, @NonNull Date onlineSince, @NonNull long usageMemory, @NonNull long freeMemory, boolean maintenance) {
        this.appId = appId;
        this.type = type;
        this.server = server;
        this.address = address;
        this.onlineSince = onlineSince;
        this.usageMemory = usageMemory;
        this.freeMemory = freeMemory;
        this.maintenance = maintenance;
    }
}
