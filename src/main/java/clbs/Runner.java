package clbs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Chen Feng
 * @version 1.0 2018/3/27
 */
public class Runner {
    public static void main(String[] args) throws Exception {
        ExecutorService service = Executors.newFixedThreadPool(5);

        Broker broker = new Broker();
        LoadBalancer loadBalancer = new LoadBalancer();
        service.submit(broker);
        service.submit(new Receiver("adas-receiver", "tcp://127.0.0.1:5210"));
        //service.submit(new Receiver("web-receiver", "tcp://127.0.0.1:5210"));
        service.submit(loadBalancer);

        Sender protocolSender = new Sender("protocol-sender", "tcp://192.168.24.127:5211");
        Sender webSender = new Sender("web-sender", "tcp://192.168.24.127:5211");
        Sender adasSender = new Sender("adas-sender", "tcp://192.168.24.127:5211");

        for (int i = 0; i < 10; i++) {
            String message = "data#" + Integer.toString(i);
            //protocolSender.sendMsg(message, "adas-receiver");
            //webSender.sendMsg(message, "adas-receiver");
            adasSender.sendMsg(message, "web-receiver");
        }
        Thread.sleep(2000);
        webSender.sendMsg("exit", "adas-receiver");
        adasSender.sendMsg("exit", "web-receiver");
        service.shutdown();
        protocolSender.close();
        webSender.close();
        adasSender.close();
        broker.close();
        loadBalancer.close();
    }
}
