package su.tzar.borovovaleksandr.tzar.lock;

public class LockHandler {
    public static final byte CLOSED = 0x00;
    public static final byte OPENED = 0x01;
    public static final byte RIDE_CLOSED = 0x02;
    public static final byte UNS_CLOSED = 0x02;
    public static final byte UNS_OPENED = 0x03;
    public static final byte CLOSE_CMD = 0x00;
    public static final byte OPEN_CMD = 0x01;

    public static final int ACTIVATE_OPEN = 1;
    public static final int ACTIVATE_CLOSE = 2;
    public static final int ACTIVATE_SYNC = 3;
    public static final int ACTIVATE_REOPEN = 4;
    public static final int ACTIVATE_LOCAL_CLOSE = 5;

    public static int getButtonsConfig(byte lockState, byte serverState){
        int config;
        config = 0;
        switch (serverState) {
            case CLOSED:
                switch (lockState) {
                    case CLOSED:
                        config = ACTIVATE_OPEN;
                        break;
                    case OPENED:
                        // do nothing
                        break;
                    case UNS_CLOSED:
                        // do nothing
                        break;
                    case UNS_OPENED:
                        config = ACTIVATE_OPEN;
                        break;
                }
                break;
            case OPENED:
                switch (lockState) {
                    case CLOSED:
                        config = ACTIVATE_SYNC;
                        break;
                    case OPENED:
                        config = ACTIVATE_CLOSE;
                        break;
                    case UNS_CLOSED:
                        config = ACTIVATE_CLOSE;
                        break;
                    case UNS_OPENED:
                        config = ACTIVATE_SYNC;
                        break;
                }
                break;
        }
        return config;
    }

    public static int getScooterButtonsConfig(byte lockState, String serverStateString) {
        byte serverState = (byte) 0xFF;
        if(serverStateString.equals("close")) {
            serverState = CLOSED;
        } else if (serverStateString.equals("open")) {
            serverState = OPENED;
        } else if (serverStateString.equals("ride_close")) {
            serverState = RIDE_CLOSED;
        }
        int config;
        config = 0;
        switch (serverState) {
            case CLOSED:
                switch (lockState) {
                    case CLOSED:
                        config = ACTIVATE_OPEN;
                        break;
                    case UNS_CLOSED:
                        //do nothing
                        break;
                }
                break;
            case OPENED:
                switch (lockState) {
                    case CLOSED:
                        config = ACTIVATE_CLOSE;
                        break;
                    case UNS_CLOSED:
                        config = ACTIVATE_LOCAL_CLOSE;
                        break;
                }
                break;
            case RIDE_CLOSED:
                switch (lockState) {
                    case CLOSED:
                        config = ACTIVATE_REOPEN;
                        break;
                    case UNS_CLOSED:
                        config = ACTIVATE_LOCAL_CLOSE;
                        break;
                }
                break;
        }
        return config;
    }

}

