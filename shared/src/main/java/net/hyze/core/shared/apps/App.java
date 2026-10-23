package net.hyze.core.shared.apps;

import net.hyze.core.shared.servers.Server;

import java.net.InetSocketAddress;
import java.util.Objects;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Deprecated
public class App {

    @NonNull
    private final String id;

    @NonNull
    private final String displayName;

    @NonNull
    private final AppType type;

    @NonNull
    private final InetSocketAddress address;

    private final Server server;

    @Setter
    private AppStatus status;

    public boolean isSame(String id) {
        return this.id.equals(id);
    }

    public boolean isSameServer(@NonNull App app) {
        return this.server != null && Objects.equals(server, app.getServer());
    }
}
