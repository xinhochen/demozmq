package pubsub;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * @author Chen Feng
 * @version 1.0 2018/11/13
 */
public class Publisher {

    private final ZContext context;
    private final ZMQ.Socket socket;

    private Publisher(String name) {
        this.context = new ZContext(1);
        this.socket = context.createSocket(ZMQ.PUB);
        this.socket.setIdentity(name.getBytes());
        this.socket.bind("tcp://*:5563");
    }

    private void publish(String message) {
        this.socket.send(message);
    }

    private void close() {
        this.context.destroy();
    }

    public static void main(String[] args) {
        Publisher publisher = new Publisher("publisher");
        int count = 0;
        System.out.println("Start publish.");
        while (!Thread.currentThread().isInterrupted()) {
            publisher.publish(String.format("B #%d", count++));
        }
        publisher.close();
    }
}
