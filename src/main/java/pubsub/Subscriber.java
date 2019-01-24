package pubsub;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

/**
 * @author Chen Feng
 * @version 1.0 2018/11/13
 */
public class Subscriber {

    private final ZContext context;
    private final ZMQ.Socket socket;

    private Subscriber(String name) {
        this.context = new ZContext(1);
        this.socket = context.createSocket(ZMQ.SUB);
        this.socket.setIdentity(name.getBytes());
        this.socket.connect("tcp://192.168.24.127:5563");
    }

    private void subscribe() {
        this.socket.subscribe("B");
        while(!Thread.currentThread().isInterrupted()) {
            ZMsg msg = ZMsg.recvMsg(this.socket);
            if (msg == null) {
                break;
            }
            msg.dump(System.out);
            msg.destroy();
        }
        this.context.destroy();
    }

    public static void main(String[] args) {
        Subscriber subscriber = new Subscriber("subscriber");
        subscriber.subscribe();
    }
}
