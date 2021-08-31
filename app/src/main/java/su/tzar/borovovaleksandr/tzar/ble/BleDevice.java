package su.tzar.borovovaleksandr.tzar.ble;

import com.polidea.rxandroidble2.RxBleDevice;

public class BleDevice {
    public RxBleDevice device;
    public BleConnection connection;

    public void set(RxBleDevice d){
        device = d;
        connection = new BleConnection(d);
    }
    public BleConnection getConnectionToDevice(){
        return connection;
    }
    public String getName(){
        if(device!=null) {
            return device.getName();
        }
        else{
            return "null";
        }
    }
    public void reset(){
        device = null;
        connection = null;
    }
    public boolean isSet(){
        if(device!=null){
            return true;
        }
        return false;
    }
}
