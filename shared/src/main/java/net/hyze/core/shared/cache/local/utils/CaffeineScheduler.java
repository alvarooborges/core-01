package net.hyze.core.shared.cache.local.utils;

import com.github.benmanes.caffeine.cache.Scheduler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CaffeineScheduler implements Scheduler {

    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static CaffeineScheduler instance;

    @Override
    public @NonNull Future<?> schedule(@NonNull Executor executor, @NonNull Runnable runnable, @Positive long time, @NonNull TimeUnit timeUnit) {
        return scheduler.schedule(() -> executor.execute(runnable), time, timeUnit);
    }

    public static CaffeineScheduler getInstance() {
        if (instance == null) {
            instance = new CaffeineScheduler();
        }

        return instance;
    }
}
