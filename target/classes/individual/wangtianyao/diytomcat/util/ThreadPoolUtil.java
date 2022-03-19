package individual.wangtianyao.diytomcat.util;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {
    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(20,
            100,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>());

    public static void run(Runnable r){
        pool.execute(r);
    }
}
