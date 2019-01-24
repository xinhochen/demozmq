package mdc;

import org.zeromq.ZMsg;

/**
 * @author Chen Feng
 * @version 1.0 2018/3/26
 */
public class MdWorker {

    /**
     * @param args
     */
    public static void main(String[] args) {
        boolean verbose = (args.length > 0 && "-v".equals(args[0]));
        Mdwrkapi workerSession = new Mdwrkapi("tcp://localhost:55555", "echo", verbose);

        ZMsg reply = null;
        while (!Thread.currentThread().isInterrupted()) {
            ZMsg request = workerSession.receive(reply);
            if (request == null)
                break; //Interrupted
            reply = request; //  Echo is complex :-)
        }
        workerSession.destroy();
    }
}
