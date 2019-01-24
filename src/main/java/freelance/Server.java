package freelance;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Chen Feng
 * @version 1.0 2018/6/19
 */
public class Server implements Runnable {
    private final String name;
    private final ZContext ctx;
    private final ZMQ.Socket socket;

    public Server(String name, String endpoint) {
        this.name = name;
        this.ctx = new ZContext();
        this.socket = this.ctx.createSocket(ZMQ.ROUTER);
        this.socket.setIdentity(name.getBytes());
        this.socket.bind(endpoint);
    }

    @Override
    public void run() {
        System.out.println(this.name + " is ready.");
        while (!Thread.currentThread().isInterrupted()) {
            ZMsg request = ZMsg.recvMsg(this.socket);
            if (request != null) {
                request.dump(System.out);
            }

            if (request == null) {
                break;          //  Interrupted
            }

            //  Frame 0: Identity of client
            //  Frame 1: PING, or client control frame
            //  Frame 2: request body
            ZFrame identity = request.pop();
            ZFrame control = request.pop();
            ZMsg reply = new ZMsg();
            if (control.toString().equals("PING"))
                reply.add("PONG");
            else {
                reply.add(control);
                //reply.add("OK");
            }
            request.destroy();
            reply.push(identity);
            reply.dump(System.out);
            reply.send(this.socket);
        }
        //ctx.destroy();
        System.out.println(this.name + " exited.");
    }

    void destroy() {
        ctx.destroy();
    }

    public static void main(String[] args) throws InterruptedException {
        Server server1 = new Server("tcp://127.0.0.1:5555", "tcp://*:5555");
        Server server2 = new Server("tcp://127.0.0.1:5556", "tcp://*:5556");
        Server server3 = new Server("tcp://127.0.0.1:5557", "tcp://*:5557");
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(server1);
        executorService.submit(server2);
        executorService.submit(server3);

        Thread.sleep(10000);
        server1.destroy();
    }

}
