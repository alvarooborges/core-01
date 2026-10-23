package net.hyze.core.shared.modules;

import dev.utils.shared.concurrent.NamedThreadFactory;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.apps.App;
import net.hyze.core.shared.echo.packets.app.AppStartedPacket;
import net.hyze.core.shared.exceptions.ApplicationAlreadyPreparedException;
import net.hyze.core.shared.providers.EchoProvider;
import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModuleLauncher {

    public static void initialize(String[] args, AbstractModule module) throws ApplicationAlreadyPreparedException {
        App app = module.getApp();

        CoreProvider.Redis.ECHO = new EchoProvider(
                "",
                () -> CoreProvider.Redis.REDIS_MAIN,
                EventBus.builder()
                        .logNoSubscriberMessages(false)
                        .throwSubscriberException(true)
                        .executorService(Executors.newCachedThreadPool(new NamedThreadFactory(
                                "Echo Background", true
                        )))
                        .build()
        );

        CoreProvider.prepare(app);

        try {
            module.onPrepared();
        } catch (IllegalStateException exception) {
            exception.printStackTrace();

            CoreProvider.shut();

            System.exit(1);
            return;
        }

        if (!module.getEchoChannels().isEmpty()) {

            ExecutorService service = Executors.newSingleThreadExecutor(new NamedThreadFactory(
                    "Echo Post Executor", true
            ));

            CoreProvider.Redis.ECHO.provide().subscribe(module.getEchoChannels(), (packet, runnable) -> {
                service.submit(runnable);
            });
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Status Executor", true));

        AtomicBoolean startNotified = new AtomicBoolean(false);

        executor.scheduleWithFixedDelay(() -> {
            CoreProvider.Cache.Redis.APPS_STATUS.provide().update(app.getStatus());

            if (!startNotified.get()) {
                startNotified.set(true);
                CoreProvider.Redis.ECHO.provide().publish(new AppStartedPacket(app));
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
}
