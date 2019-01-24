package mdc;

import org.zeromq.ZMsg;

/**
 * @author Chen Feng
 * @version 1.0 2018/3/26
 */
public class Mdclient {

    public static void main(String[] args) {
        boolean verbose = (args.length > 0 && "-v".equals(args[0]));
        Mdcliapi clientSession = new Mdcliapi("tcp://localhost:55555", verbose);

        int count;
        for (count = 0; count < 100000; count++) {
            ZMsg request = new ZMsg();
            request.addString("Hello world");
            ZMsg reply = clientSession.send("echo", request);
            if (reply != null)
                reply.destroy();
            else
                break; // Interrupt or failure
        }

        System.out.printf("%d requests/replies processed\n", count);
        clientSession.destroy();
    }
}
