package demo;

import com.google.common.cache.Cache;
import kilim.Pausable;

import java.util.concurrent.Callable;

/**
 * Created by adamshuang on 8/26/18.
 */
public class CoroutineCache<K, V>{
    private final Cache<K, V> cache;
    public CoroutineCache(Cache<K, V> cache){
        this.cache = cache;
    }

    public Cache<K, V> getCache() {
        return cache;
    }

    public V get(K key, CoroutineCache.XCallable<? extends V> valueLoader) throws Pausable, Exception {
        V value = cache.getIfPresent(key);
        if (value == null) {
            value = valueLoader.load();
            if (value != null) {
                cache.get(key, valueLoader);
            }
        }
        return value;
    }

    public static abstract class XCallable<V> implements Callable<V> {
        protected V value;

        public final V load() throws Pausable, Exception {
            this.value = loadFromRemote();
            return value;
        }

        protected abstract V loadFromRemote() throws Pausable, Exception;

        @Override
        public final V call() throws Exception {
            return value;
        }

    }
}