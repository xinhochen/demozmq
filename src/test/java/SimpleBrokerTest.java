import clbs.Sender;
import clbs.SimpleReceiver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Chen Feng
 * @version 1.0 2019/1/24
 */
public class SimpleBrokerTest {
    private final static int total = 1;

    private final static String receiverHost = "tcp://192.168.24.127:5210";

    private final static String senderHost = "tcp://192.168.24.127:5211";

    public static void main(String[] args) throws InterruptedException {
        char[] data = new char[10];
        for (int i = 0; i < data.length; i++) {
            data[i] = '1';
        }
        String sendData = new String(data);

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        SimpleReceiver receiver1 = new SimpleReceiver("test1", receiverHost, total);
        executorService.submit(receiver1);

        SimpleReceiver receiver2 = new SimpleReceiver("test2", receiverHost, total);
        executorService.submit(receiver2);

        Thread.sleep(1000);

        executorService.submit(() -> {
            int count = 0;
            Sender sender1 = new Sender("sender1", senderHost);
            long start = System.nanoTime();
            while (!Thread.currentThread().isInterrupted()) {
                sender1.sendMsg(sendData, "test1");
                //Thread.sleep(1000);
                if (++count == total) {
                    break;
                }
            }
            System.out.println("Duration: sender1 " + (System.nanoTime() - start) / 1e9);
            sender1.close();
        });

        executorService.submit(() -> {
            int count = 0;
            Sender sender2 = new Sender("sender2", senderHost);
            long start = System.nanoTime();
            while (!Thread.currentThread().isInterrupted()) {
                sender2.sendMsg(sendData, "test2");
                //Thread.sleep(1000);
                if (++count == total) {
                    break;
                }
            }
            System.out.println("Duration: sender2 " + (System.nanoTime() - start) / 1e9);
            sender2.close();
        });
        executorService.shutdown();
    }
}
