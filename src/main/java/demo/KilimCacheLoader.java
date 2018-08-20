package demo;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.ExecutionException;
import kilim.Pausable;
import kilim.Task;

public class KilimCacheLoader<KK,VV> extends CacheLoader<KK,VV> {
    public interface PausableFuture {
        void body(SettableFuture future) throws Pausable;
    }

    private static final Object dummy = new Object();
    PausableFuture body;
    public KilimCacheLoader() {}
    public KilimCacheLoader(PausableFuture body) { this.body = body; }

    public VV load(KK key) {
        return (VV) dummy;
    }

    public ListenableFuture reload(KK key,VV oldValue) {
        SettableFuture future = SettableFuture.create();
        Task.fork(() -> {
            if (body==null) body(future);
            else body.body(future);
        });
        return future;
    }

    public void body(SettableFuture future) throws Pausable {}
    
    public static <KK,VV> VV get(LoadingCache<KK,VV> cache,KK key) throws Pausable {
        VV result = null;
        while (true) {
            try {
                result = cache.get(key);
                if (result==dummy)
                    cache.refresh(key);
                else
                    return result;
            }
            catch (ExecutionException ex) {}
            Task.sleep(100);
        }
    }
}
