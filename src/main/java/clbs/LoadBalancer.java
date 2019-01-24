package clbs;

import org.zeromq.ZMQ;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Chen Feng
 * @version 1.0 2018/3/27
 */
public class LoadBalancer implements Runnable, Closeable {
    private final ZMQ.Context context;
    private final ZMQ.Socket frontend;
    private final ZMQ.Socket backend;
    private final Queue<String> workerQueue;

    LoadBalancer() {
        context = ZMQ.context(1);
        frontend = context.socket(ZMQ.DEALER);
        frontend.setIdentity("web-receiver".getBytes());
        backend = context.socket(ZMQ.ROUTER);
        workerQueue = new LinkedList<>();
    }

    @Override
    public void run() {
        frontend.connect("tcp://127.0.0.1:5210");
        backend.bind("inproc://loadBalancer");

        ExecutorService service = Executors.newFixedThreadPool(5);
        int clientNbr;
        for (clientNbr = 0; clientNbr < 5; clientNbr++) {
            String clientName = "web-receiver" + clientNbr;
            service.submit(new Receiver(clientName, "inproc://loadBalancer"));
            workerQueue.add(clientName);
        }

        System.out.println("start load balance.");
        while (!Thread.currentThread().isInterrupted()) {
            String clientAddr = frontend.recvStr();
            String request = frontend.recvStr();
            String workerAddr = workerQueue.poll();

            System.out.println("client: " + clientAddr + " request: " + request + " workerAddr: " + workerAddr);
            backend.sendMore(workerAddr);
            backend.sendMore(clientAddr);
            backend.send(request);

            workerQueue.add(workerAddr);
        }
    }

    @Override
    public void close() throws IOException {
        frontend.close();
        backend.close();
        context.term();
        System.out.println("loadBalancer exited.");
    }
}
