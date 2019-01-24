package clbs;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.IOException;
import java.nio.channels.Selector;

/**
 * @author Chen Feng
 * @version 1.0 2018/3/27
 */
public class Receiver implements Runnable {

    private final String name;
    private final ZContext context;
    private final String host;
    private ZMQ.Socket socket;
    private int lifeTime;
    private int interval;

    private final static int HEARTBEAT_LIVENESS = 3;
    private final static int HEARTBEAT_INTERVAL = 1000;
    private final static int INTERVAL_INIT = 1000;
    private final static int INTERVAL_MAX = 32000;

    private final static String PPP_READY = "1" ;
    private final static String PPP_HEARTBEAT = "2";

    Receiver(String name, String host) {
        this.name = name;
        this.context = new ZContext(1);
        this.host = host;
        this.socket = createSocket();
        System.out.println("receiver connected.");
    }

    private ZMQ.Socket createSocket() {
        ZMQ.Socket socket = context.createSocket(ZMQ.DEALER);
        socket.connect(this.host);
        ZFrame frameReady = new ZFrame(PPP_READY);
        ZFrame frameName = new ZFrame(this.name);
        frameName.send(socket, ZFrame.MORE);
        frameReady.send(socket, 0);
        return socket;
    }

    @Override
    public void run() {
        // If liveness hits zero, queue is considered disconnected
        this.lifeTime = HEARTBEAT_LIVENESS;
        this.interval = INTERVAL_INIT;
        long heartbeat_at = System.currentTimeMillis() + HEARTBEAT_INTERVAL;

        ZFrame frameName = new ZFrame(this.name);
        ZFrame frameHeartbeat = new ZFrame(PPP_HEARTBEAT);
        ZMQ.PollItem pollItems[] = { new ZMQ.PollItem(this.socket, ZMQ.Poller.POLLIN) };
        Selector selector = null;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!Thread.currentThread().isInterrupted()) {
            int rc;
            try {
                rc = ZMQ.poll(selector, pollItems, HEARTBEAT_INTERVAL);
            } catch(Exception e) {
                pollItems = reconnect();
                continue;
            }
            if (rc == -1) {
                break;
            }
            if (pollItems[0].isReadable()) {
                ZMsg msg = ZMsg.recvMsg(this.socket);
                if (msg == null) {
                    break;
                }

                if (msg.size() == 2) {
                    System.out.println("msg: " + msg);
                } else if (msg.size() == 1){
                    ZFrame frame = msg.getFirst();
                    if (PPP_HEARTBEAT.equals(new String(frame.getData()))) {
                        this.lifeTime = HEARTBEAT_LIVENESS;
                    } else {
                        System.out.println("E: invalid message");
                    }
                    msg.destroy();
                } else {
                    System.out.println("E: invalid message");
                }
                interval = INTERVAL_INIT;
            } else if (--this.lifeTime == 0) {
                System.out.println ("W: heartbeat failure, can't reach queue.");
                pollItems = reconnect();
            }
            if (System.currentTimeMillis() > heartbeat_at) {
                heartbeat_at = System.currentTimeMillis() + HEARTBEAT_INTERVAL;
                frameName.send(this.socket, ZFrame.MORE);
                frameHeartbeat.send(this.socket, 0);
            }
        }
        this.context.destroy();
        System.out.println(name + " exited.");
    }

    private ZMQ.PollItem[] reconnect() {
        System.out.println (String.format("W: reconnecting in %d msec.", this.interval));
        try {
            Thread.sleep(this.interval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (this.interval < INTERVAL_MAX) {
            this.interval *= 2;
        }
        this.context.destroySocket(this.socket);
        this.socket = createSocket();
        ZMQ.PollItem pollItems[] = { new ZMQ.PollItem(this.socket, ZMQ.Poller.POLLIN) };
        this.lifeTime = HEARTBEAT_LIVENESS;
        return pollItems;
    }

    public void close() {
        this.context.destroy();
    }

    public static void main(String[] args) {
        Receiver receiver = new Receiver("test", "tcp://192.168.24.127:5210");
        receiver.run();
    }
}
