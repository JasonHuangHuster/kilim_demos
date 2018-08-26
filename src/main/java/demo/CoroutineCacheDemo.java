package demo;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import kilim.Pausable;

import java.util.concurrent.TimeUnit;


public class CoroutineCacheDemo {

    public static void main(String[] args) throws Exception {
        if (kilim.tools.Kilim.trampoline(false, args)) {
            return;
        }

        Cache<String, Integer> cache = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.SECONDS)
                .maximumSize(2000).build();

        CoroutineCache<String, Integer> cacheHelper = new CoroutineCache<String, Integer>(
                cache);
        cacheHelper.get("any_key", new CoroutineCache.XCallable<Integer>() {
            @Override
            protected Integer loadFromRemote() throws Pausable, Exception {
                return anyPausableMethod();
            }
            
        });

    }

    private static Integer anyPausableMethod() throws Pausable {

        return 1;
    }

}
