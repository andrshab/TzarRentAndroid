package su.tzar.borovovaleksandr.tzar.lock;

import android.location.Location;

import su.tzar.borovovaleksandr.tzar.ble.HexString;
import su.tzar.borovovaleksandr.tzar.helper.Codes;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Lock {
    private static final int STATE_LEN = 1;
    private static final int ID_LEN = 4;
    private static final int MSG_LEN = 16;
    private static final int VOLTAGE_LEN = 4;
    private static final int TYPE_LEN = 1;
    private byte realState;
    private byte[] id;
    private byte[] message;
    private byte[] packet;
    private byte[] openPacket;
    private byte[] voltage;
    private Location location;
    private byte[] closePacket = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private byte type;
    private byte serverState;

    public Lock(){
        byte[] empty = new byte[STATE_LEN + ID_LEN + MSG_LEN + VOLTAGE_LEN + TYPE_LEN];
        Arrays.fill(empty, (byte) 0xFF);
        this.packet = empty;
        this.realState = Array.getByte(Arrays.copyOfRange(empty, 0, STATE_LEN), 0);
        this.id = Arrays.copyOfRange(packet, STATE_LEN, STATE_LEN + ID_LEN);
        this.message = Arrays.copyOfRange(packet, STATE_LEN + ID_LEN, STATE_LEN + ID_LEN + MSG_LEN);
        this.voltage = Arrays.copyOfRange(packet, STATE_LEN + ID_LEN + MSG_LEN, STATE_LEN + ID_LEN + MSG_LEN + VOLTAGE_LEN);
        this.type = Array.getByte(Arrays.copyOfRange(packet, STATE_LEN + ID_LEN + MSG_LEN + VOLTAGE_LEN, STATE_LEN + ID_LEN + MSG_LEN + VOLTAGE_LEN + TYPE_LEN), 0);
    }
    public Lock(byte[] bytes) {
        this.packet = bytes;
        this.realState = Array.getByte(Arrays.copyOfRange(bytes, 0, STATE_LEN), 0);
        this.id = Arrays.copyOfRange(packet, STATE_LEN, STATE_LEN + ID_LEN);
        this.message = Arrays.copyOfRange(packet, STATE_LEN + ID_LEN, STATE_LEN + ID_LEN + MSG_LEN);
        this.voltage = Arrays.copyOfRange(packet, STATE_LEN + ID_LEN + MSG_LEN, STATE_LEN + ID_LEN + MSG_LEN + VOLTAGE_LEN);
        this.type = Array.getByte(Arrays.copyOfRange(packet, STATE_LEN + ID_LEN + MSG_LEN + VOLTAGE_LEN, STATE_LEN + ID_LEN + MSG_LEN + VOLTAGE_LEN + TYPE_LEN), 0);

        for (int i = STATE_LEN; i < STATE_LEN + ID_LEN; i++) {
            this.closePacket[i] = packet[i];
        }
    }

    public byte getRealState() {
        return realState;
    }

    public void setOpenPacket(byte[] bytes) {
        this.openPacket = bytes;
    }

    public byte[] getOpenPacket() {
        return this.openPacket;
    }

    public byte[] getClosePacket() {
        return this.closePacket;
    }

    public void clearOpenPacket() {
        this.openPacket = null;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getID() {
        return Integer.toString(HexString.bytesToInt(this.id));
    }

    public void setID(String id) {
        this.id = HexString.intToBytes(Integer.parseInt(id));
    }

    public byte[] getVoltageBytes() {
        return this.voltage;
    }

    public int getChargeLevel() {
        double voltage = ((double) ByteBuffer.wrap(this.voltage).getInt()) * 3 / 1000000;
        double percent = 114.121 * voltage - 374.987;
        int p = (int) percent;
        return p;
    }
    public Location getLocation() {
        return this.location;

    }

    public byte getType() {
        return this.type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public void setType(String type) {
        if (type.equals("SCOOTER")) {
            this.type = Codes.SCOOTER;
        } else if (type.equals("BICYCLE")) {
            this.type = Codes.BICYCLE;
        }
    }

    public byte getServerState() {
        return this.serverState;
    }

    public void setServerState(byte serverState) {
        this.serverState = serverState;
    }

    public void setServerState(String serverState) {
        this.serverState = (byte) 0xFF;
        if(serverState.equals("close")) {
            this.serverState = Codes.CLOSED;
        } else if (serverState.equals("open")) {
            this.serverState = Codes.OPENED;
        } else if (serverState.equals("ride_close")) {
            this.serverState = Codes.RIDE_CLOSED;
        }
    }

    public void clear() {
        this.realState = (byte) 0xFF;
        this.location = null;
        this.id = new byte[] {0x00, 0x00, 0x00, 0x00};
        this.voltage = new byte[] {0x00, 0x00, 0x00, 0x00};
        this.packet = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        this.openPacket = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        this.closePacket = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        this.type = (byte) 0xFF;
    }
}
