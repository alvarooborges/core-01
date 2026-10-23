
package net.hyze.core.shared.providers;

import dev.utils.echo.Echo;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.contracts.Provider;
import org.greenrobot.eventbus.EventBus;

import java.util.function.Supplier;

public class EchoProvider implements Provider<Echo> {

    private final String suffix;
    private final Supplier<RedisProvider> redisCacheProvide;
    private final EventBus bus;

    private Echo echo;

    public EchoProvider(
            String suffix,
            Supplier<RedisProvider> redisCacheProvide,
            EventBus bus
    ) {
        this.suffix = suffix;
        this.redisCacheProvide = redisCacheProvide;
        this.bus = bus;
    }

    @Override
    public synchronized void prepare() {
        if (echo != null) {
            return;
        }

        RedisProvider redisProvider = redisCacheProvide.get();

        if (redisProvider == null) {
            throw new IllegalStateException(
                    "RedisProvider não foi configurado para o Echo."
            );
        }

        if (redisProvider.provide() == null) {
            throw new IllegalStateException(
                    "Redis ainda não foi preparado antes do EchoProvider."
            );
        }

        echo = new Echo(
                CoreProvider.getApp().getId(),
                suffix,
                redisProvider.provide(),
                bus
        );

        System.out.println("[HyzeCore] EchoProvider preparado.");
    }

    @Override
    public synchronized Echo provide() {
        return echo;
    }

    @Override
    public synchronized void shut() {
        if (echo != null) {
            echo.shutdown();
            echo = null;
        }
    }
}
