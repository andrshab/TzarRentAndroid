package su.tzar.borovovaleksandr.tzar.ble;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import su.tzar.borovovaleksandr.tzar.App;

/**
 * This class handles BLE base states, such as
 * turning on/off bluetooth adapter and changing
 * bonding states. For this it has BroadcastReceiver,
 * which it's necessary to register after Ble instance creation.
 * After it you can call run() method. BleScanner will start scanning
 * for advertising ble devices
 * Usage example from MainActivity:
 * <pre>
 * public class MainActivity extends AppCompatActivity {
 *     &#64;Inject
 *     public Ble ble;
 *     ...
 *     &#64;Override
 *     protected void onCreate(Bundle savedInstanceState) {
 *     ...
 *         App.getComponent().injectMainActivity(this);
 *         ble.registerBleStateReciever();
 *         ble.run(this);
 *     }
 * }
 * </pre>

 */
public class Ble {
    private final BroadcastReceiver mBleStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        log("BLE Off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        log("BLE turning Off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        log("BLE On");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        log("BLE turning on");
                        break;
                }
            }
        }
    };

    public Ble(){
        App.getComponent().injectBle(this);
        log(Integer.toString(this.hashCode()));
    }


    public void registerBleStateReciever(Context context){
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(mBleStateReceiver, intentFilter);
    }

    public void unregisterBleStateReciever(Context context){
        context.unregisterReceiver(mBleStateReceiver);
    }

    public static Boolean isEnabled(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        } else if (!mBluetoothAdapter.isEnabled()) {
            return false;
        } else {
            return true;
        }
    }

    private void log(String message) {
        Log.i(getClass().getSimpleName(), message);
    }
}
