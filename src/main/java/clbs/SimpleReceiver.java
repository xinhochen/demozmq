package clbs;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

/**
 * @author Chen Feng
 * @version 1.0 2019/1/24
 */
public class SimpleReceiver implements Runnable {

    private final String name;
    private final ZContext context;
    private final String host;
    private ZMQ.Socket socket;

    public SimpleReceiver(String name, String host) {
        this.name = name;
        this.context = new ZContext(1);
        this.host = host;
        this.socket = createSocket();
        System.out.println("receiver connected.");
    }

    private ZMQ.Socket createSocket() {
        ZMQ.Socket socket = context.createSocket(ZMQ.DEALER);
        if (!socket.connect(this.host)) {
            throw new RuntimeException("Connection failed.");
        }
        return socket;
    }

    @Override
    public void run() {
        ZMsg msg;
        while (!Thread.currentThread().isInterrupted()) {
            msg = ZMsg.recvMsg(socket);
            if (msg == null) {
                break;
            }
            msg.dump(System.out);
        }
        this.context.destroy();
    }

    public static void main(String[] args) {
        SimpleReceiver receiver = new SimpleReceiver("test", "tcp://192.168.24.127:5210");
        receiver.run();
    }
}
