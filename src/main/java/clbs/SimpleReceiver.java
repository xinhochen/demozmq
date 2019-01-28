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
    private final int total;

    public SimpleReceiver(String name, String host, int total) {
        this.name = name;
        this.context = new ZContext(1);
        this.host = host;
        this.socket = createSocket();
        this.socket.setLinger(1000);
        this.socket.setIdentity(name.getBytes());
        this.total = total;
        System.out.println("receiver " + name + " connected.");
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
        int count = 0;
        ZMsg msg;
        long start = System.nanoTime();
        while (!Thread.currentThread().isInterrupted()) {
            msg = ZMsg.recvMsg(socket);
            if (msg == null) {
                break;
            }
            msg.dump(System.out);
            count++;
            //System.out.println(name + " receive: " + count);
            if (count == total) {
                break;
            }
        }
        this.context.destroy();
        // System.out.println(name + " exit.");
        System.out.println("Duration: " + name + " " + (System.nanoTime() - start) / 1e9);
    }

    public static void main(String[] args) {
        SimpleReceiver receiver = new SimpleReceiver("test", "tcp://192.168.24.127:5210", 100);
        receiver.run();
    }
}
