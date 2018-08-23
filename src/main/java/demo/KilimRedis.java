package demo;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kilim.Mailbox;
import kilim.Task;

/**
 * Created by adamshuang on 8/23/18.
 */
public class KilimRedis {


    public static void main(String[] args) {

        if (kilim.tools.Kilim.trampoline(false, args)) {

            return;
        }

        Mailbox mailbox = new Mailbox();
        RedisClient client = RedisClient.create("redis://localhost");
        RedisAsyncCommands<String, String> commands = client.connect().async();
        System.out.println(commands.get("adams"));;
        RedisFuture<String> redisFuture = commands.get("adams");
        redisFuture.thenRun(new Runnable() {
            @Override
            public void run() {
                mailbox.putb(1);
            }
        });

        Task.fork(()->
        {
            mailbox.get();
            System.out.println(redisFuture.get());

        });



    }
}
