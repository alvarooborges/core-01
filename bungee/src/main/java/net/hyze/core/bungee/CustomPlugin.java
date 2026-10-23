package net.hyze.core.bungee;

import com.google.common.base.Enums;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.apps.App;
import net.hyze.core.shared.apps.AppStatus;
import net.hyze.core.shared.apps.AppType;
import net.hyze.core.shared.apps.ServerStatus;
import net.hyze.core.shared.echo.packets.app.AppStartedPacket;
import net.hyze.core.shared.servers.Server;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.conf.YamlConfig;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public abstract class CustomPlugin extends Plugin {

    public final boolean prepareProvider;

    @Getter
    private App app;

    @Getter
    private InetSocketAddress address;

    private boolean startNotify = false;

    @Override
    public void onLoad() {
        YamlConfig yamlConfig = new YamlConfig();
        yamlConfig.load();

        if (this.prepareProvider) {
            try {

                for (ListenerInfo info : yamlConfig.getListeners()) {

                    InetSocketAddress address = info.getHost();

                    if (address != null) {
                        this.address = address;
                        break;
                    }
                }



                String rawId = System.getProperty("net.hyze.id");
                String rawServer = System.getProperty("net.hyze.server");
                String rawDisplayName = System.getProperty("net.hyze.name");
                String rawType = System.getProperty("net.hyze.type");

                if (rawId != null && rawServer != null && rawDisplayName != null && rawType != null) {
                    Server server = Enums.getIfPresent(Server.class, rawServer).orNull();
                    AppType type = AppType.getById(rawType);

                    app = new App(rawId, rawDisplayName, type, address, server);

                    CoreProvider.prepare(app);
                } else {
                    int port = this.address.getPort();
                    app = CoreProvider.prepare(port);
                }


                app.setStatus(new ServerStatus(
                        app.getId(),
                        app.getType(),
                        app.getServer(),
                        app.getAddress()
                ));

            } catch (Exception ex) {
                Logger.getGlobal().log(Level.SEVERE, "Failed to prepare core provider", ex);
                ProxyServer.getInstance().stop();
            }
        }
    }

    @Override
    public void onEnable() {
        if (this.prepareProvider) {
            App app = CoreProvider.getApp();

            getProxy().getScheduler().schedule(this, () -> {
                Collection<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers();

                ((ServerStatus) app.getStatus()).setOnlineCount(players.size());

                CoreProvider.Cache.Redis.APPS_STATUS.provide().update(app.getStatus());

                if (!startNotify) {
                    CoreProvider.Redis.ECHO.provide().publish(new AppStartedPacket(app));
                    startNotify = true;
                }

            }, 1, 1, TimeUnit.SECONDS);
        }
    }

    @Override
    public void onDisable() {
        if (this.prepareProvider) {
            CoreProvider.shut();
        }
    }

    public void registerServerApp(AppStatus status) {
        System.out.println("Registrando: " + status.getAppId());

        if (status.getAppId().equalsIgnoreCase(getApp().getId())) {
            System.out.println(String.format("O app %s é o mesmo app que o proxy", status.getAppId()));
            return;
        }

        if (!status.getType().isAllowProxyRegistry()) {
            System.out.println(String.format(
                    "O app %s não pode ser registrado.",
                    status.getAppId()
            ));
            return;
        }

        System.out.println(String.format("Registrando servidor %s", status.getAppId()));

        getProxy().getConfig().getServers().put(
                status.getAppId(),
                getProxy().constructServerInfo(status.getAppId(), status.getAddress(), status.getAppId(), false)
        );
    }
}
