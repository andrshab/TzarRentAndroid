package su.tzar.borovovaleksandr.tzar.helper;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.provider.MediaStore;
import android.util.Log;

import su.tzar.borovovaleksandr.tzar.App;
import su.tzar.borovovaleksandr.tzar.R;
import su.tzar.borovovaleksandr.tzar.ble.BleConnection;
import su.tzar.borovovaleksandr.tzar.ble.BleScanner;
import su.tzar.borovovaleksandr.tzar.ble.HexString;
import su.tzar.borovovaleksandr.tzar.fragment.MapFragment;
import su.tzar.borovovaleksandr.tzar.network.NetworkService;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import javax.inject.Inject;

public class RideHandler {
    public static final String UNASSIGNED_LOCK_ID = "null";
    private Context context;
    @Inject
    BleScanner bleScanner;
    private String serverState;
    private String IDFromQR = UNASSIGNED_LOCK_ID;

    public RideHandler(Context context) {
        App.getComponent().injectRideHandler(this);
        this.context = context;
        this.serverState = "";
    }

    public void handleOnActivityResult(Context context, int requestCode, int resultCode, Intent data, BleConnection connection, MapFragment mapFragment) {
        switch (requestCode) {
            case Codes.REQUEST_TAKE_SCOOTER_PHOTO:
                if (data != null) {
                    String photoPath = data.getExtras().getString(MediaStore.EXTRA_OUTPUT);
                    String lockID = data.getStringExtra("lockID");
                    Location location = new Location("");
                    location.setLatitude(data.getDoubleExtra("lat", 0));
                    location.setLongitude(data.getDoubleExtra("lng", 0));
                    mapFragment.showWait();
                    NetworkService
                            .closeScooterRequest(context, lockID, location, photoPath, response -> {
                                mapFragment.hideWait();
                                if (response.getState() != null) {
                                    mapFragment.hideTimer();
                                    new InfoMessage(context, () -> mapFragment.hideTimer()).show(context.getString(R.string.tip_thanks_for_ride));
                                    setServerState(response.getState());
                                    mapFragment.setLockServerState(response.getState());
                                }
                            });
                }
                break;
            case Codes.REQUEST_TAKE_PHOTO:
                if (data != null) {
                    //send to server photo and packet from lock
                    String photoPath = data.getExtras().getString(MediaStore.EXTRA_OUTPUT);

                    byte[] packetFromLock = data.getByteArrayExtra("packetFromLock");
                    byte realState = data.getByteExtra("realState", (byte) 0xFF);
                    Location location = new Location("");
                    location.setLatitude(data.getDoubleExtra("lat", 0));
                    location.setLongitude(data.getDoubleExtra("lng", 0));
                    mapFragment.showWait();
                    NetworkService
                            .closeRequest(context, packetFromLock, location, photoPath, response -> {
                                mapFragment.hideWait();
                                if (response.getCommand() != null) {
                                    byte[] packetFromServer = HexString.hexToBytes(response.getCommand());
                                    connection.writeInCharacteristic(BleConnection.LOCK_UUID, packetFromServer);
                                } else if (response.getState() != null) {
                                    mapFragment.hideTimer();
                                    new InfoMessage(context, () -> mapFragment.hideTimer()).show(context.getString(R.string.tip_thanks_for_ride));
                                    setServerState(response.getState());
                                    mapFragment.handleStates(realState, getServerState(), packetFromLock);
                                } else if (response.getError() != null) {
                                    new InfoMessage(mapFragment.getContext()).show(response.getError());
                                }
                            });
                }
                break;
            case Codes.REQUEST_QR:
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    String barcode = result.getContents();
                    if (barcode != null) {
                        if(barcode.length() < 9) {
                            new InfoMessage(context).show(context.getString(R.string.warn_incorrect_lock_id));
                            return;
                        }
                        IDFromQR = barcode.substring(8, barcode.length() - 1);
                        if (IDFromQR.length() < 11) {
                            bleScanner.setDesiredDeviceName(IDFromQR);
                            bleScanner.startScan(context);
                        } else {
                            new InfoMessage(context).show(context.getString(R.string.warn_incorrect_lock_id));
                            log("Incorrect lock ID");
                        }

                    } else {
                        log("Barcode is NULL");
                    }
                }
                break;
        }
    }


    public byte getServerState() {
        if (serverState.equals("close")) {
            return Codes.CLOSED;
        } else if (serverState.equals("open")) {
            return Codes.OPENED;
        }
        return (byte) 0xff;
    }

    public void setServerState(String state){
        this.serverState = state;
    }

    public String getIDFromQR() {
        return this.IDFromQR;
    }

    public void clearIDFromQR(){
        this.IDFromQR = UNASSIGNED_LOCK_ID;
    }

    private void log(String message) {
        Log.i(getClass().getSimpleName(), message);
    }
}
