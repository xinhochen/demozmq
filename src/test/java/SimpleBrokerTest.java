import clbs.Sender;
import clbs.SimpleReceiver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Chen Feng
 * @version 1.0 2019/1/24
 */
public class SimpleBrokerTest {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        SimpleReceiver receiver = new SimpleReceiver("test", "tcp://192.168.24.127:5210");
        executorService.submit(receiver);

        Thread.sleep(1000);

        Sender sender = new Sender("sender", "tcp://192.168.24.127:5211");
        executorService.submit(() -> {
            int count = 0;
            long start = System.nanoTime();
            while (!Thread.currentThread().isInterrupted()) {
                sender.sendMsg("#" + count++, "test");
                //Thread.sleep(1000);
                if (count == 100000) {
                    break;
                }
            }
            System.out.println("Duration: " + (System.nanoTime() - start) / 1e9);
            sender.close();
        });
    }
}
