package su.tzar.borovovaleksandr.tzar.helper;

import com.google.zxing.integration.android.IntentIntegrator;

public class Codes {
    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_TAKE_SCOOTER_PHOTO = 2;
    public static final int REQUEST_CODE_TOKENIZE = 3;
    public static final int REQUEST_3DS = 4;
    public static final int REQUEST_QR = IntentIntegrator.REQUEST_CODE;
    public static final byte CLOSED = 0x00;
    public static final byte OPENED = 0x01;
    public static final byte UNS_CLOSED = 0x02;
    public static final byte UNS_OPENED = 0x03;
    public static final float MAX_TIME_DELTA = 1;
    public static final byte BICYCLE = 0x00;
    public static final byte SCOOTER = 0x01;
    public static final byte UNASSIGNED_TYPE = (byte) 0xFF;
    public static final byte RIDE_CLOSED = 0x02;
}
