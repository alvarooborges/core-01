package net.hyze.core.shared.providers;

import com.google.common.collect.Lists;
import net.hyze.core.shared.cache.local.LocalCache;
import net.hyze.core.shared.contracts.Provider;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalCacheProvider<T extends LocalCache> implements Provider<T> {

    private T cache;

    private Class<T> cacheClass;
    private Object[] args;

    public LocalCacheProvider(T cache) {
        this.cache = cache;
    }

    public LocalCacheProvider(Class<T> cacheClass, Object... args) {
        this.cacheClass = cacheClass;
        this.args = args;
    }

    @Override
    public void prepare() {

        if (cacheClass != null) {
            try {
                List<Object> list = Lists.newLinkedList();

                if (args.length > 0) {
                    Collections.addAll(list, args);
                }

                cache = cacheClass.getConstructor(list.stream()
                        .map(Object::getClass)
                        .toArray(Class[]::new)
                ).newInstance(list.toArray());

            } catch (Throwable ex) {
                Logger.getLogger(LocalCacheProvider.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        cache.populate();
    }

    @Override
    public void shut() {
    }

    @Override
    public T provide() {
        return cache;
    }
}
