package net.hyze.core.shared.servers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.hyze.core.shared.apps.App;

import java.util.Optional;
import java.util.Set;

@ToString(exclude = "apps")
public enum Server {

    FACTIONS_ALPHA("factions-alpha")
    ;

    @Getter
    @Expose
    private final String id;

    @Getter
    private final String envPath;

    @Getter
    @Setter
    private ServerType type;

    @Getter
    @Setter
    private String displayName;

    @Getter
    @Setter
    private int maxPlayers = 150;

    private final Set<App> apps = Sets.newHashSet();

    Server(String id) {
        this.id = id;
        this.envPath = id.replace("-", "_");
    }

    public ImmutableList<App> getApps() {
        return ImmutableList.<App>builder().addAll(apps).build();
    }

    public void addApp(App app) {
        apps.add(app);
    }

    public static Optional<Server> getById(String id) {

        if (id != null) {
            for (Server server : values()) {
                if (server.getId().equals(id)) {
                    return Optional.of(server);
                }
            }
        }

        return Optional.empty();
    }
}
