package net.hyze.core.spigot;

import co.aikar.timings.Timings;
import com.google.common.base.Enums;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import dev.utils.shared.concurrent.NamedThreadFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.apps.App;
import net.hyze.core.shared.apps.AppType;
import net.hyze.core.shared.apps.BukkitStatus;
import net.hyze.core.shared.echo.packets.app.AppStartedPacket;
import net.hyze.core.shared.exceptions.ApplicationAlreadyPreparedException;
import net.hyze.core.shared.exceptions.InvalidApplicationException;
import net.hyze.core.shared.servers.Server;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.support.ExecutorServiceAdapter;

import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

@NoArgsConstructor
public abstract class CustomPlugin extends JavaPlugin {

    @Getter
    protected boolean prepareProvider = false;

    public CustomPlugin(boolean prepareProvider) {
        this.prepareProvider = prepareProvider;
    }

    @Override
    public void onLoad() {
        if (this.prepareProvider) {
            try {
                String ip = Bukkit.getIp();
                int port = Bukkit.getPort();

                String rawId = System.getProperty("net.hyze.id");
                String rawServer = System.getProperty("net.hyze.server");
                String rawDisplayName = System.getProperty("net.hyze.name");
                String rawType = System.getProperty("net.hyze.type");

                App app;

                if (rawId != null && rawServer != null && rawDisplayName != null && rawType != null) {
                    Server server = Enums.getIfPresent(Server.class, rawServer).orNull();
                    AppType type = AppType.getById(rawType);

                    app = new App(rawId, rawDisplayName, type, new InetSocketAddress(ip, port), server);

                    CoreProvider.prepare(app);
                } else {
                    app = CoreProvider.prepare(port);
                }

                BukkitStatus status = buildStatus(app);

                app.setStatus(status);
            } catch (InvalidApplicationException | ApplicationAlreadyPreparedException ex) {
                Logger.getGlobal().log(Level.SEVERE, "Failed to prepare core provider", ex);
                Bukkit.shutdown();
            }
        }
    }

    public BukkitStatus buildStatus(App app) {
        return new BukkitStatus(
                app.getId(),
                app.getType(),
                app.getServer(),
                app.getAddress(),
                false
        );
    }

    @Override
    public void onEnable() {
        if (this.prepareProvider) {

            App app = CoreProvider.getApp();

            AtomicBoolean startNotified = new AtomicBoolean(false);

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Status Executor", true));

            BukkitStatus status = ((BukkitStatus) app.getStatus());

            executor.scheduleWithFixedDelay(() -> {

                status.setOnlineCount(Bukkit.getOnlinePlayers().size());
                status.setRecentTps(MinecraftServer.getServer().recentTps[0]);

                if (CustomPlugin.this.isEnabled()) {
                    CoreProvider.Cache.Redis.APPS_STATUS.provide().update(status);

                    if (!startNotified.get()) {
                        startNotified.set(true);
                        CoreProvider.Redis.ECHO.provide().publish(new AppStartedPacket(app));
                    }
                } else {
                    executor.shutdownNow();
                }
            }, 1, 1, TimeUnit.SECONDS);
        }

        Timings.setHistoryInterval(20);
    }

    @Override
    public void onDisable() {
        try {
            if (this.prepareProvider) {
                CoreProvider.Cache.Redis.APPS_STATUS.provide().delete(CoreProvider.getApp());

                CoreProvider.shut();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
