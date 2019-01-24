import org.zeromq.ZMQ;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Chen Feng
 * @version 1.0 2018/3/26
 */
public class DealerToRouter {
    private static ZMQ.Context ctx;
    private static String address = "inproc://server";
    private static volatile boolean done;

    public static void main(String[] args) throws InterruptedException {
        ctx = ZMQ.context(1);

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(new ClientWorker());
        Thread.sleep(1000);
        executorService.submit(new ServerWorker());
    }

    private static class ServerWorker implements Runnable {

        @Override
        public void run() {
            ZMQ.Socket server = ctx.socket(ZMQ.ROUTER);
            server.bind(address);

            while (!Thread.currentThread().isInterrupted()) {
                String id = server.recvStr();
                String data = server.recvStr();

                System.out.println("Received: " + data);
                done = true;
            }
            ctx.term();
        }
    }

    private static class ClientWorker implements Runnable {

        @Override
        public void run() {
            ZMQ.Socket adasSocket = ctx.socket(ZMQ.DEALER);
            adasSocket.setIdentity("adas".getBytes());
            adasSocket.connect(address);

            int count = 0;
            while (!Thread.currentThread().isInterrupted()) {
                adasSocket.send(String.valueOf(count));

                System.out.println("Sent: #" + count++);
                if (done) {
                    break;
                }
            }
        }
    }
}
