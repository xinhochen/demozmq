package clbs;

import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Chen Feng
 * @version 1.0 2018/3/27
 */
public class Sender implements Closeable {

    private final String name;
    private final ZMQ.Context context;
    private final ZMQ.Socket socket;

    public Sender(String name, String host) {
        this.name = name;
        this.context = ZMQ.context(1);
        this.socket = context.socket(ZMQ.DEALER);
        this.socket.setLinger(1000);
        this.socket.setIdentity(name.getBytes());
        this.socket.connect(host);
    }

    public void sendMsg(String message, String address) {
        ZMsg msg = ZMsg.newStringMsg(address, message);
        msg.send(this.socket);
        //this.socket.sendMore(address);
        //this.socket.send(message);
    }

    @Override
    public void close() {
        this.socket.close();
        this.context.term();
    }

    public static void main(String[] args) {
        Sender sender = new Sender("sender", "tcp://192.168.24.127:5211");
        int count = 1;
        long start = System.nanoTime();
        while (!Thread.currentThread().isInterrupted()) {
            sender.sendMsg("#" + count++, "test");
            //sender.sendMsg("#", "test");
            if (count > 200000) {
                break;
            }
        }
        System.out.println("Duration: " + (System.nanoTime() - start) / 1e9);
        sender.close();
    }
}
