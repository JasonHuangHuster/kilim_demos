package demo; /**
 * Created by adamshuang on 2018/8/22.
 */

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.ExecutionException;

import kilim.Pausable;
import kilim.Task;

public class KilimCacheLoader<KK, VV> extends CacheLoader<KK, VV> {

    public interface PausableFuture<KK> {
        void calcFutureValue(KK key, SettableFuture future) throws Pausable;
    }

    private final VV dummyValue;
    private final PausableFuture pausableFuture;


    public KilimCacheLoader(PausableFuture calcFutureValue, VV dummyValue) {
        this.pausableFuture = calcFutureValue;
        this.dummyValue = dummyValue;
    }

    @Override
    public VV load(KK key) {
        return (VV) dummyValue;
    }

    @Override
    public ListenableFuture reload(final KK key, VV oldValue) {
        final SettableFuture future = SettableFuture.create();
        Task.fork(() -> {
            if (pausableFuture == null) {
                calcFutureValue(key, future);
            } else {
                pausableFuture.calcFutureValue(key, future);
            }
        });

        return future;

    }

    public void calcFutureValue(KK key, SettableFuture future) throws Pausable {
        future.set(dummyValue);
    }


    public static class Getter<KK, VV> {
        LoadingCache<KK, VV> cache;
        int delay;
        VV dummyValue;

        public Getter(LoadingCache<KK, VV> cache, int delay, VV dummyValue) {
            this.cache = cache;
            this.delay = delay;
            this.dummyValue = dummyValue;
        }

        public VV get(KK key) throws Pausable {
            return getCache(cache, key, delay, dummyValue);
        }

    }


    /**
     * get a value from the cache asynchronously.
     * this method (or similar logic) must be used for all access.
     * if the key is not available immediately this method pauses until it is ready
     *
     * @param <KK>  the key type
     * @param <VV>  the value type
     * @param cache the cache to access
     * @param key   the key to search for
     * @param delay the number of milliseconds to suspend if the value is not yet available
     * @return
     * @throws Pausable
     */
    public static <KK, VV> VV getCache(LoadingCache<KK, VV> cache, KK key, int delay, VV dummyValue) throws Pausable {
        VV result = null;
        while (true) {
            try {
                result = cache.get(key);
                if (result == dummyValue) {

                    cache.refresh(key);
                } else {
                    return result;
                }
            } catch (ExecutionException ex) {
            }
            Task.sleep(delay);
        }
    }

    /**
     * get a value from the cache asynchronously.
     * this method (or similar logic) must be used for all access.
     * if the key is not available immediately this method pauses until it is ready
     *
     * @param <KK>  the key type
     * @param <VV>  the value type
     * @param cache the cache to access
     * @param key   the key to search for
     * @return
     * @throws Pausable
     */
    public static <KK, VV> VV getCacheOneShot(LoadingCache<KK, VV> cache, KK key, int delay, VV dummyValue) throws Pausable {
        VV result = null;
        try {
            result = cache.get(key);
            if (result == dummyValue) {

                cache.refresh(key);
                Task.sleep(delay);
                return cache.get(key);
            } else {
                return result;
            }
        } catch (ExecutionException ex) {
        }
        return dummyValue;

    }
}
