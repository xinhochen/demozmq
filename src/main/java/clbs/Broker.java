package clbs;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Chen Feng
 * @version 1.0 2018/3/27
 */
public class Broker implements Runnable, Closeable {
    private final ZContext ctx;
    private final ZMQ.Socket frontend;
    private final ZMQ.Socket backend;
    private final Map<String, String> receivers;

    private final static int HEARTBEAT_INTERVAL = 1000;

    Broker() {
        this.ctx = new ZContext(1);
        this.frontend = ctx.createSocket(ZMQ.ROUTER);
        this.backend = ctx.createSocket(ZMQ.ROUTER);
        this.receivers = new HashMap<>();
    }

    @Override
    public void run() {
        frontend.bind("tcp://*:5211");
        backend.bind("tcp://*:5210");

        ZMQ.Poller poller = this.ctx.createPoller(2);
        poller.register(frontend, ZMQ.Poller.POLLIN);
        poller.register(backend, ZMQ.Poller.POLLIN);
        while (!Thread.currentThread().isInterrupted()) {
            boolean workersAvailable = receivers.size() > 0;
            int rc = poller.poll(HEARTBEAT_INTERVAL);
            if (rc == -1) {
                System.out.println("poll error.");
                break;
            }
            if (poller.pollin(1)) {
                String id = backend.recvStr();
                String name = backend.recvStr();
                //System.out.println("id: " + id + " name: " + name + " connected.");
                receivers.put(name, id);
            }
            // message from sender
            if (poller.pollin(0)) {
                //String id = frontend.recvStr();
                //String address = frontend.recvStr();
                //String data = frontend.recvStr();
                ZMsg msg = ZMsg.recvMsg(frontend);
                //System.out.println("recv msg: " + msg);
                ZFrame id = msg.pop();
                ZFrame address = msg.pop();

                //msg.dump(System.out);
                msg.push(id);
                msg.push(address);
                msg.send(backend);

                //backend.sendMore(address);
                //backend.sendMore(id);
                //backend.send(data);
                msg.destroy();
            }
        }
    }

    @Override
    public void close() throws IOException {
        ctx.destroy();
        System.out.println("broker closed.");
    }

    public static void main(String[] args) {
        Broker broker = new Broker();
        broker.run();
    }
}
