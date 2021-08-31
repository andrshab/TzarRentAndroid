package su.tzar.borovovaleksandr.tzar.ble;

import android.content.Context;
import android.util.Log;

import su.tzar.borovovaleksandr.tzar.App;
import su.tzar.borovovaleksandr.tzar.R;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;


import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * This class can scan for advertising devices,
 * add them to BleDevices and notify about it some view(for example DevicesFragment).<br>
 * To notify view you should implement BleScannerDeviceFoundInterface in view class,
 * register callback in this view and override bleScannerDeviceFoundCallback.<br>
 * Also after finding new device this class calls adapter.notifyDataSetChanged()
 * to refresh list of devices, which via this adapter is displayed in DevicesFragment.
 */
public class BleScanner {
    public interface BleScannerDeviceFoundInterface {
        void bleScannerDeviceFoundCallback();
    }

    BleScannerDeviceFoundInterface bleScannerDeviceFoundInterface;
    @Inject
    public RxBleClient rxBleClient;
    @Inject
    public BleDevice bleDevice;
    public Disposable scanDisposable;
    private String desiredDeviceName;
    private String lockID;
    public Observable<ScanResult> scanner;
    public Subject<String> mObservable = PublishSubject.create();

    public BleScanner() {
        App.getComponent().injectBleScanner(this);
        scanner = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build(),
                new ScanFilter.Builder()
                        .build()
        )
                .observeOn(AndroidSchedulers.mainThread());

    }

    public void registerDeviceFoundCallback(BleScannerDeviceFoundInterface callback) {
        this.bleScannerDeviceFoundInterface = callback;
    }

    public void setDesiredDeviceName(String id) {
        String desiredName = lockIDToName(id);
        this.lockID = id;
        this.desiredDeviceName = desiredName;
    }

    public void startScan(Context context) {
        bleDevice.reset();
        scanDisposable = scanner.subscribe(this::handleScanResult, (t) -> onScanFailure(t, context));
        log("SCANNING...");
        mObservable.onNext(context.getString(R.string.tip_press_lock_btn) + getLockID());
    }


    public void stopScan() {
        if(scanDisposable!=null) {
            scanDisposable.dispose();
        }
        desiredDeviceName = null;
        lockID = null;
        log("Scan STOPPED");
//        mObservable.onNext("BLUETOOTH SCAN STOPPED");
    }


    private void handleScanResult(ScanResult scanResult) {
        if (desiredDeviceName != null) {
            if (desiredDeviceName.equals(scanResult.getBleDevice().getName()) && !bleDevice.isSet()) {
                log("Desired device " + desiredDeviceName + " was found");
                bleDevice.set(scanResult.getBleDevice());
                stopScan();
                bleScannerDeviceFoundInterface.bleScannerDeviceFoundCallback();
            }
        }
    }

    private void onScanFailure(Throwable throwable, Context context) {
        mObservable.onNext(context.getString(R.string.warn_scan_failed));
        if (throwable instanceof BleScanException) {
            ScanExceptionHandler.handleException((BleScanException) throwable, context);
        }
    }

    public boolean isScanning() {
        return scanDisposable != null;
    }

    private String getLockID() {
        if (this.lockID != null) {
            return this.lockID;
        }
        return "";
    }

    public static String lockIDToName(String lockID) {
        String name = "tzarLock 0000000000";
        name = name.substring(0, name.length() - lockID.length()) + lockID;
        return name;
    }

    private void log(String message) {
        Log.i(getClass().getSimpleName(), message);
    }
}
