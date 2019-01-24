import org.zeromq.ZMQ;

/**
 * @author Chen Feng
 * @version 1.0 2018/4/19
 */
public class Test {
    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.DEALER);
        socket.setIdentity("f3-sender".getBytes());

        boolean res = socket.connect("tcp://192.168.24.238:5209");
        if (!res) {
            return;
        }
        System.out.println("Connected.");

        socket.sendMore("adas-receiver");
        socket.send("test1");
        System.out.println("Sent out test1.");

        socket.sendMore("adas-receiver");
        socket.send("test2");
        System.out.println("Sent out test2.");

        socket.sendMore("adas-receiver");
        socket.send("test2");
        System.out.println("Sent out test3.");

        socket.close();
        context.term();
    }
}
