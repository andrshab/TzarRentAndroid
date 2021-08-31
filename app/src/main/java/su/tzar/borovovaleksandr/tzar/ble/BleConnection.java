package su.tzar.borovovaleksandr.tzar.ble;

import android.util.Log;

import com.jakewharton.rx.ReplayingShare;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

/**
 * This class delivers through itself connection to device.
 * After creating instance of this class via
 * <pre>
 * BleConnection connection = new BleConnection(device)
 * </pre>
 * you can call:
 * <pre>
 * connection.establish()
 * </pre>
 * If device is not bonded it will try to bond to device first.
 * Else it will try to connect.
 * Before calling connection.establish() you can check whether connection
 * between phone and device already exists via connection.isEstablished
 * or connection.isEstablishing
 */
public class BleConnection {
    public interface BleConnectionStateInterface {
        void connectionStateCallback(RxBleConnection.RxBleConnectionState connectionState);
    }
    public interface BleConnectionReadInterface {
        void connectionReadCallback(byte[] bytes, UUID uuid);
    }
    BleConnectionReadInterface bleConnectionReadInterface;
    BleConnectionStateInterface bleConnectionStateInterface;
    public static UUID KEY_UUID = UUID.fromString("c0291100-77cd-40c0-bcf2-ba3dda1a6093");
    public static UUID LOCK_UUID = UUID.fromString("c0291200-77cd-40c0-bcf2-ba3dda1a6093");
    public static UUID NOTIFY_UUID = UUID.fromString("c0291300-77cd-40c0-bcf2-ba3dda1a6093");
    public String connectionStatus;
    private Disposable disposableConnectionState;
    private static final int  KEY_ID = 0;
    private static final int LOCK_ID = 1;
    private static final int  READY_FOR_READ = 0;
    private static final int  READY_FOR_WRITE = 1;
    private RxBleDevice device;
    private PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Observable<RxBleConnection> connectionObservable;

    public void registerConnectionReadCallBack(BleConnectionReadInterface callback){
        this.bleConnectionReadInterface = callback;
    }
    public void registerConnectionStateCallBack(BleConnectionStateInterface callback){
        this.bleConnectionStateInterface = callback;
    }
    public BleConnection(RxBleDevice device){
        this.device = device;
    }
    private Observable<RxBleConnection> prepareConnectionObservable() {
        return device
                .establishConnection(false)
                .takeUntil(disconnectTriggerSubject)
                .compose(ReplayingShare.instance());
    }
    public RxBleDevice getDevice(){
        return  this.device;
    }
    public void establish(){
        if(isEstablished()||isEstablishing()){
            close();
        }
        connectionObservable = prepareConnectionObservable();
        prepareCharacteristic(KEY_UUID, false);
        prepareCharacteristic(LOCK_UUID, false);
        prepareCharacteristic(NOTIFY_UUID, true);
        subscribeToConnectionStateChange();

    }
    private void subscribeToConnectionStateChange(){
        if(disposableConnectionState==null){
            disposableConnectionState = device.observeConnectionStateChanges()
                    .subscribe(
                            connectionState -> {
                                connectionStatus = device.getName() + ": " + connectionState.name();
                                log(connectionStatus);
                                bleConnectionStateInterface.connectionStateCallback(connectionState);
                                if(connectionState.equals(RxBleConnection.RxBleConnectionState.CONNECTED)){
                                    readFromCharacteristic(LOCK_UUID);
                                }
                            },
                            throwable -> {
                                // Handle an error here.
                            }
                    );
        }
    }
    private void prepareCharacteristic(UUID uuid, boolean isNotificationRequired){
        final Disposable connectionDisposable = connectionObservable
                .flatMapSingle(RxBleConnection::discoverServices)
                .flatMapSingle(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(uuid))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        characteristic -> {
                            if(isNotificationRequired){
                                setNotification(uuid);
                            }
                            log("Characteristic " + uuid.toString() + " is preapred");

                        },
                        this::onConnectionFailure,
                        this::onConnectionFinished
                );
        compositeDisposable.add(connectionDisposable);
    }
    private void onConnectionFailure(Throwable throwable) {
        log("Connection error: " + throwable);
    }
    private void onConnectionFinished() {
        log("Connection finished");
    }
    public void close(){
        if(isEstablished()||isEstablishing()){
            disconnectTriggerSubject.onNext(true);
            compositeDisposable.clear();
        }
    }
    public boolean isEstablishing(){
        return device.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTING;
    }
    public boolean isEstablished(){
        return device.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    private void setNotification(UUID uuid){
        if (isEstablished()) {
            final Disposable disposable = connectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.setupNotification(uuid))
                    .doOnNext(notificationObservable -> notificationHasBeenSetUp())
                    .flatMap(notificationObservable -> notificationObservable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNotificationReceived, this::onNotificationSetupFailure);

            compositeDisposable.add(disposable);
        }
    }

    private void notificationHasBeenSetUp() {
        log("Notification has been set up");
    }

    private void onNotificationReceived(byte[] bytes) {
        log("Status characteristic notification: " + HexString.bytesToHex(bytes));
        int char_id = bytes[0];
        switch (char_id){
            case LOCK_ID:
                readFromCharacteristic(LOCK_UUID);
                break;
            case KEY_ID:
                //do nothing
                break;
            default:
                break;
        }
    }

    private void onNotificationSetupFailure(Throwable throwable) {
        log("Notifications error: " + throwable);
    }

    public void writeInCharacteristic(UUID uuid, byte[] bytesToWriteInDevice){
        if(isEstablished()){
            final Disposable disposable = connectionObservable
                    .firstOrError()
                    .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(uuid, bytesToWriteInDevice))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            bytes -> onWriteSuccess(),
                            this::onWriteFailure
                    );

            compositeDisposable.add(disposable);
        }

    }
    private void onWriteSuccess() {
        log("Write success");
    }
    private void onWriteFailure(Throwable throwable) {
        log("Write error: " + throwable);
    }


    public void readFromCharacteristic(UUID uuid){
        if(isEstablished()){
            final Disposable disposable = connectionObservable
                    .flatMapSingle(rxBleConnection -> rxBleConnection.readCharacteristic(uuid))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            bytes -> onReadSuccess(bytes, uuid),
                            this::onReadFailure
                    );
            compositeDisposable.add(disposable);
        }
        else{
            log("Connection is not established");
        }
    }

    private void onReadSuccess(byte[] bytes, UUID uuid) {
        log("Read value: " + HexString.bytesToHex(bytes) + ", length: " + bytes.length);
        bleConnectionReadInterface.connectionReadCallback(bytes, uuid);
    }

    private void onReadFailure(Throwable throwable) {
        log("Read error: " + throwable);
    }

    private void log(String message) {
        Log.i(getClass().getSimpleName(), message);
    }
}
