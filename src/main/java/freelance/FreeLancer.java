package freelance;

import org.zeromq.ZMsg;

/**
 * @author Chen Feng
 * @version 1.0 2018/6/19
 */
public class FreeLancer {
    public static void main(String[] args) {
        //String[] binds = new String[] { "tcp://*:5555", "tcp://*:5556", "tcp://*:5557" };
        //String[] binds = new String[] { "inproc://server1", "inproc://server2" };
        //String[] hosts = new String[] { "tcp://127.0.0.1:5210", "tcp://127.0.0.1:5211" };
        //String[] addresses = new String[] { "proc://server1", "proc://server2" };
        //Server[] servers = new Server[3];

        //ExecutorService executorService = Executors.newCachedThreadPool();

        //for (int i = 0; i < binds.length; i++) {
        //    servers[i] = new Server("tcp://127.0.0.1:" + (5555 + i), binds[i]);
        //    executorService.submit(servers[i]);
        //}

        //  Create new freelance client object
        Flcliapi client = new Flcliapi();

        //  Connect to several endpoints
        client.connect("tcp://127.0.0.1:5555");
        client.connect("tcp://127.0.0.1:5556");
        client.connect("tcp://127.0.0.1:5557");

        //  Send a bunch of name resolution 'requests', measure time
        int requests = 10000;
        long start = System.currentTimeMillis();
        while (requests-- > 0) {
            ZMsg request = new ZMsg();
            request.add("random name");
            ZMsg reply = client.request(request);
            if (reply == null) {
                System.out.println("E: name service not available, aborting");
                break;
            }
            reply.destroy();
        }
        System.out.printf("Average round trip cost: %d ms\n", System.currentTimeMillis() - start);

        client.destroy();

        //for (Server server : servers) {
        //    server.destroy();
        //}
    }
}
