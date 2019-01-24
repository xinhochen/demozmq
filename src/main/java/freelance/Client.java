package freelance;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMsg;

import java.nio.channels.Selector;

/**
 * @author Chen Feng
 * @version 1.0 2018/6/19
 */
public class Client {
    private static final int GLOBAL_TIMEOUT = 2500;
    private final ZContext ctx;
    private final ZMQ.Socket socket;
    private final ZMQ.Poller poller;
    private int servers;
    private int sequence;

    public Client() {
        this.servers = 0;
        this.sequence = 0;
        this.ctx = new ZContext();
        this.socket = ctx.createSocket(ZMQ.DEALER);
        this.poller = ctx.createPoller(1);
        this.poller.register(this.socket, ZMQ.Poller.POLLIN);
    }

    public void connect(String endpoint) {
        this.socket.connect(endpoint);
        this.servers++;
    }

    public void connect(String[] endpoints) {
        for (String endpoint : endpoints) {
            connect(endpoint);
        }
    }

    void sendMessage(String message) {
        System.out.println("Client send message: " + message);
        this.socket.send(message);
    }

    ZMsg request(ZMsg request) {
        //  Prefix request with sequence number and empty envelope
        String sequenceText = String.format("%d", ++sequence);
        request.push(sequenceText);
        request.push("");

        //  Blast the request to all connected servers
        int server;
        for (server = 0; server < servers; server++) {
            ZMsg msg = request.duplicate();
            msg.send(socket);
        }
        //  Wait for a matching reply to arrive from anywhere
        //  Since we can poll several times, calculate each one
        ZMsg reply = null;
        long endTime = System.currentTimeMillis() + GLOBAL_TIMEOUT;
        while (System.currentTimeMillis() < endTime) {
            if (poller.poll(endTime - System.currentTimeMillis()) == -1) {
                System.out.println("Interrupted.");
                break;
            }
            if (poller.pollin(0)) {
                //  Reply is [empty][sequence][OK]
                reply = ZMsg.recvMsg(socket);
                assert (reply.size() == 3);
                reply.pop();
                String sequenceStr = reply.popString();
                int sequenceNbr = Integer.parseInt(sequenceStr);
                if (sequenceNbr == sequence) {
                    System.out.println("sequence equals.");
                    break;
                }
                String content = reply.popString();
                System.out.println("Receive content: " + content);
                reply.destroy();
            }
        }
        request.destroy();
        return reply;

    }

    public void destroy() {
        this.ctx.destroy();
    }
}
