import org.zeromq.ZMQ;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Chen Feng
 * @version 1.0 2018/3/23
 */
public class AsyncServer {
    private static ZMQ.Context ctx;
    private static String address = "inproc://server";

    public static void main(String[] args) {
        ctx = ZMQ.context(1);

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(new ServerWorker());
        executorService.submit(new ClientWorker());
        executorService.submit(new SenderWorker("web"));
        executorService.submit(new SenderWorker("protocol"));
    }

    private static class ServerWorker implements Runnable {
        private final int totalSize = 3;

        @Override
        public void run() {
            ZMQ.Socket server = ctx.socket(ZMQ.ROUTER);
            server.bind(address);

            int clientSize = 0;
            while (!Thread.currentThread().isInterrupted()) {
                String id = server.recvStr();
                String data = server.recvStr();

                if (data.equals("ready") && clientSize++ <= totalSize) {
                    System.out.println(id + " is ready.");
                    if (clientSize == totalSize) {
                        wakeUp(server, "web");
                        wakeUp(server, "protocol");
                    }
                    continue;
                }

                server.sendMore("adas");
                server.sendMore(id);
                server.send(data);
            }
            ctx.term();
        }

        private void wakeUp(ZMQ.Socket server, String clientId) {
            server.sendMore(clientId);
            server.send("");
            System.out.println("wake up " + clientId);
        }
    }

    private static class ClientWorker implements Runnable {

        @Override
        public void run() {
            ZMQ.Socket adasSocket = ctx.socket(ZMQ.DEALER);
            adasSocket.setIdentity("adas".getBytes());
            adasSocket.connect(address);

            System.out.println("adas connected.");
            adasSocket.send("ready");
            while (!Thread.currentThread().isInterrupted()) {
                String id = adasSocket.recvStr();
                String data = adasSocket.recvStr();
                System.out.println(String.format("id: %s, data: %s", id, data));
            }
        }
    }

    private static class SenderWorker implements Runnable {
        private String name;

        private SenderWorker(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            ZMQ.Socket socket = ctx.socket(ZMQ.DEALER);
            socket.setIdentity(this.name.getBytes());
            socket.connect(address);
            System.out.println(this.name + " connected.");

            socket.send("ready");
            socket.recv();

            socket.send("Hello, this is " + this.name + ".");
        }
    }
}
