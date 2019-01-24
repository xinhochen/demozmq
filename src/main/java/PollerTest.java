import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

/**
 * @author Chen Feng
 * @version 1.0 2018/3/26
 */
public class PollerTest {
    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.ROUTER);
        socket.bind("tcp://*:6980");

        new Thread(new ClientWorker("client")).start();
        while (!Thread.currentThread().isInterrupted()) {
            String id = socket.recvStr();
            String message = socket.recvStr();
            System.out.println("Received message: " + message + " from " + id);
            socket.sendMore(id);
            socket.send("ack");
            //System.out.println("ack");
        }
    }

    private static class ClientWorker implements Runnable {

        private final ZMQ.Context context;
        private final ZMQ.Socket socket;

        private ClientWorker(String name) {
            context = ZMQ.context(1);
            socket = context.socket(ZMQ.DEALER);
            socket.setIdentity(name.getBytes());
            socket.connect("tcp://127.0.0.1:6980");
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(100);
                    socket.send("Hello.");
                    ZMsg msg = ZMsg.recvMsg(socket);
                    System.out.println(msg.toString());
                    msg.destroy();
                } catch (Exception e) {
                    System.out.println("ZMQ发送数据出错：" + e.getMessage());
                    e.printStackTrace();
                }

            }
            socket.close();
            context.term();
        }
    }
}
