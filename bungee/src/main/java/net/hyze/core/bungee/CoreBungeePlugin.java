
package net.hyze.core.bungee;

import com.google.common.collect.Lists;
import dev.utils.echo.Echo;
import dev.utils.shared.concurrent.NamedThreadFactory;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.echo.packets.app.AppStartedPacket;
import net.hyze.core.shared.providers.EchoProvider;
import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoreBungeePlugin extends CustomPlugin {

    private ExecutorService executorService;

    public CoreBungeePlugin() {
        super(false);

        CoreBungeeWrapper.setWrapper(new CoreBungeeWrapper());

        CoreProvider.Redis.ECHO = new EchoProvider(
                "",
                () -> CoreProvider.Redis.REDIS_MAIN,
                EventBus.builder()
                        .logNoSubscriberMessages(false)
                        .throwSubscriberException(true)
                        .executorService(
                                Executors.newCachedThreadPool(
                                        new NamedThreadFactory(
                                                "Echo Background Executor",
                                                true
                                        )
                                )
                        )
                        .build()
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Echo echo = CoreProvider.Redis.ECHO.provide();

        if (echo == null) {
            throw new IllegalStateException(
                    "Echo não foi inicializado. Verifique o CoreProvider.prepare()."
            );
        }

        executorService = Executors.newSingleThreadExecutor(
                new NamedThreadFactory("Echo Post Executor", true)
        );

        echo.subscribe(
                Lists.newArrayList(Echo.BASE_CHANNEL),
                (packet, runnable) -> executorService.submit(runnable)
        );

        getLogger().info("Echo inicializado e inscrito no canal Hyze.");
    }

    @Override
    public void onDisable() {
        if (executorService != null) {
            executorService.shutdown();
        }

        super.onDisable();
    }
}
