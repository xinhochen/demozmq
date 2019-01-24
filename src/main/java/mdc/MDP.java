package mdc;

import java.util.Arrays;

import org.zeromq.ZFrame;
import org.zeromq.ZMQ;

/**
 * @author Chen Feng
 * @version 1.0 2018/3/26
 */
public enum MDP {

    /**
     * This is the version of mdc.MDP/Client we implement
     */
    C_CLIENT("MDPC01"),

    /**
     * This is the version of mdc.MDP/Worker we implement
     */
    W_WORKER("MDPW01"),

    // mdc.MDP/Server commands, as byte values
    W_READY(1), W_REQUEST(2), W_REPLY(3), W_HEARTBEAT(4), W_DISCONNECT(5);

    private final byte[] data;

    MDP(String value) {
        this.data = value.getBytes(ZMQ.CHARSET);
    }

    MDP(int value) { //watch for ints>255, will be truncated
        byte b = (byte) (value & 0xFF);
        this.data = new byte[] { b };
    }

    public byte[] getValue() {
        return data;
    }

    public ZFrame newFrame() {
        return new ZFrame(data);
    }

    public boolean frameEquals(ZFrame frame) {
        return Arrays.equals(data, frame.getData());
    }
}
