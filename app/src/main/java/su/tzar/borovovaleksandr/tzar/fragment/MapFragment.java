package su.tzar.borovovaleksandr.tzar.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import su.tzar.borovovaleksandr.tzar.App;
import su.tzar.borovovaleksandr.tzar.R;
import su.tzar.borovovaleksandr.tzar.activity.BikePhotoActivity;
import su.tzar.borovovaleksandr.tzar.activity.MainActivity;
import su.tzar.borovovaleksandr.tzar.activity.QRScannerActivity;
import su.tzar.borovovaleksandr.tzar.ble.Ble;
import su.tzar.borovovaleksandr.tzar.ble.BleConnection;
import su.tzar.borovovaleksandr.tzar.ble.BleDevice;
import su.tzar.borovovaleksandr.tzar.ble.BleScanner;
import su.tzar.borovovaleksandr.tzar.ble.HexString;
import su.tzar.borovovaleksandr.tzar.helper.Codes;
import su.tzar.borovovaleksandr.tzar.helper.GPS;
import su.tzar.borovovaleksandr.tzar.helper.InfoMessage;
import su.tzar.borovovaleksandr.tzar.helper.Permissions;
import su.tzar.borovovaleksandr.tzar.helper.RideHandler;
import su.tzar.borovovaleksandr.tzar.lock.Lock;
import su.tzar.borovovaleksandr.tzar.lock.LockHandler;
import su.tzar.borovovaleksandr.tzar.network.Auth;
import su.tzar.borovovaleksandr.tzar.network.InternetCheck;
import su.tzar.borovovaleksandr.tzar.network.LockInfo;
import su.tzar.borovovaleksandr.tzar.network.NetworkService;
import su.tzar.borovovaleksandr.tzar.network.Point;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.polidea.rxandroidble2.RxBleConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

import static su.tzar.borovovaleksandr.tzar.ble.BleConnection.KEY_UUID;


public class MapFragment extends Fragment implements
        GoogleMap.OnCameraIdleListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        BleScanner.BleScannerDeviceFoundInterface,
        BleConnection.BleConnectionStateInterface,
        BleConnection.BleConnectionReadInterface {
    private GoogleMap mMap;
    private MapView mMapView;
    private ArrayList<Marker> markers = new ArrayList<>();
    private BleConnection connection;
    private Lock lock = new Lock();
    private GPS gps;
    private FloatingActionButton scanQRBtn;
    private FloatingActionButton userAccountBtn;
    private FloatingActionButton helpBtn;
    private Snackbar snackbar;
    //    private TextView timer;
    private Chronometer timer;
    private ExtendedFloatingActionButton lockIndicator;
    private ProgressBar wait;
    private LinearLayout bottomSheetMenu;
    private Button endScooterRideBtn;
    private Button pulloutCableBtn;
    private BottomSheetBehavior bottomSheetBehavior;
    private int strokeColorArgb = Color.HSVToColor(10, new float[]{100, 1, 1});
    @Inject
    BleScanner bleScanner;
    @Inject
    public BleDevice bleDevice;
    @Inject
    public RideHandler rideHandler;


    private View.OnClickListener onScanQRClickListener = v -> onScanQRClick(getContext());
    private View.OnClickListener onUserAccountClickListener = v -> onUserAccountClick();
    private View.OnClickListener onEndScooterRideClickListener = v -> onEndScooterRideClick();
    private View.OnClickListener onPulloutCableClickListener = v -> onPulloutCableClick();
    private View.OnClickListener onHelpClickListener = v -> onHelpClick();

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getComponent().injectMapFragment(this);
        bleScanner.registerDeviceFoundCallback(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = mView.findViewById(R.id.mapView);
        scanQRBtn = mView.findViewById(R.id.scan_QR_btn);
        userAccountBtn = mView.findViewById(R.id.user_account_btn);
        helpBtn = mView.findViewById(R.id.help_btn);
        gps = new GPS(getActivity());
        snackbar = Snackbar.make(mView, "", Snackbar.LENGTH_INDEFINITE);
        timer = mView.findViewById(R.id.chronometer);
        lockIndicator = mView.findViewById(R.id.lock_indicator);
        wait = mView.findViewById(R.id.map_wait);
        bottomSheetMenu =  mView.findViewById(R.id.menu_bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetMenu);
        endScooterRideBtn = mView.findViewById(R.id.end_ride_btn);
        pulloutCableBtn = mView.findViewById(R.id.pullout_cable_btn);

        clearLockIndicator();
        hideWait();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        scanQRBtn.setOnClickListener(onScanQRClickListener);
        userAccountBtn.setOnClickListener(onUserAccountClickListener);
        endScooterRideBtn.setOnClickListener(onEndScooterRideClickListener);
        pulloutCableBtn.setOnClickListener(onPulloutCableClickListener);
        helpBtn.setOnClickListener(onHelpClickListener);
        bleScanner.mObservable.subscribe(result -> handleScannerState(result));

        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        NetworkService.getUserState(getContext(), response -> {
            if (response.getDelta() != null && response.getLockId() != null && getActivity() != null && response.getLockType() != null && response.getLockState() != null) {
                //записать rideType
                lock.setType(response.getLockType());
                lock.setServerState(response.getLockState());
                lock.setID(response.getLockId());
                setTimer(response.getDelta(), response.getLockId());
            } else if (response.getError() != null && getActivity() != null) {
//                new InfoMessage(getContext()).show(response.getError());
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        hideWait();
        if (connection != null) {
            connection.close();
        }
        if(bleScanner!=null) {
            bleScanner.stopScan();
        }
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        if(mMap != null && mMapView != null) {
            mMap.clear();
            mMapView.onDestroy();
            mMapView = null;
        }
        super.onDestroyView();
    }


    private void onScanQRClick(Context context) {
        //check BLE, GPS, INTERNET enabled
        if (Permissions.isPermissionsGranted(requireContext())) {
            if (Auth.isAuthorized(context)) {
                new InternetCheck(isInternetAvailable -> {
                    if (!isInternetAvailable) {
                        new InfoMessage(context).show(getString(R.string.warn_check_internet_connectio));
                        return;
                    } else {
                        if (!(checkPhoneFunctions(getContext()))) {
                            return;
                        }
                        IntentIntegrator
                                .forSupportFragment(this)
                                .setOrientationLocked(false)
                                .setCaptureActivity(QRScannerActivity.class)
                                .initiateScan();
                    }
                });
            } else {
                new InfoMessage(getActivity()).show(getString(R.string.tip_please_sign_in));
            }
        } else {
            Permissions.requestPermissions(requireActivity());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        rideHandler.handleOnActivityResult(getContext(), requestCode, resultCode, data, connection, this);
    }

    @Override
    public void bleScannerDeviceFoundCallback() {
        connection = bleDevice.getConnectionToDevice();
        connection.registerConnectionStateCallBack(this);
        connection.registerConnectionReadCallBack(this);
        connection.establish();
    }

    public void handleScannerState(String state) {
        if (getActivity() != null) {
            //В onResume могло записаться rideType
            if (lock.getType() == Codes.UNASSIGNED_TYPE || lock.getType() == Codes.BICYCLE) {
                snackbar.setText(state)
                        .setAction(getString(R.string.OK), v -> {
                            snackbar.dismiss();
                        })
                        .show();
            } else if (lock.getType() == Codes.SCOOTER) {
                if (lock.getServerState() == Codes.CLOSED || lock.getServerState() == Codes.OPENED) {
                    snackbar.setText(state)
                            .setAction(getString(R.string.OK), v -> {
                                snackbar.dismiss();
                            })
                            .show();
                } else if (lock.getServerState() == Codes.RIDE_CLOSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
            //Если rideType=0xFF - предложить нажать кнопку
            //Если rideType=SCOOTER - запросить serverScooterState на сервере. По ответу либо показать меню, либо предложить нажать кнопку
            //Если rideType=BICYCLE - предложить нажать кнопку
        }
    }

    @Override
    public void connectionReadCallback(byte[] rawBytes, UUID uuid) {
        if(uuid.equals(BleConnection.LOCK_UUID)) {
            //по последнему байту в rawBytes определеить самокат или велик

            //если велик, то логика ниже
            lock = new Lock(rawBytes);
            int chargeLevel = lock.getChargeLevel();
            byte realState = lock.getRealState();
            setLockIndicator(realState, chargeLevel);
            connection.readFromCharacteristic(KEY_UUID);
            if (lock.getType() == Codes.BICYCLE) {
                if (lock.getID().equals(rideHandler.getIDFromQR())) {
                    NetworkService
                            .getLockState(getContext(), HexString.bytesToHex(rawBytes), response -> {
                                //set server_state
                                if (response.getCoomand() != null) {
                                    if (connection != null) {
                                        byte[] packetFromServer = HexString.hexToBytes(response.getCoomand());
                                        connection.writeInCharacteristic(BleConnection.LOCK_UUID, packetFromServer);
                                    }
                                } else if (response.getState() != null) {
                                    rideHandler.setServerState(response.getState());
                                    handleStates(realState, rideHandler.getServerState(), rawBytes);
                                } else if (response.getError() != null) {
                                    new InfoMessage(getContext()).show(response.getError());
                                }
                            });
                } else {
                    new InfoMessage(getContext()).show(getString(R.string.warn_ids_not_match));
                }
            } else if (lock.getType() == Codes.SCOOTER) {
                if (lock.getID().equals(rideHandler.getIDFromQR())) {
                    NetworkService
                            .getLockState(getContext(), HexString.bytesToHex(rawBytes), response -> {
                                //set server_state
                                if (response.getState() != null) {
                                    handleScooterStates(realState, response.getState(), rawBytes);
                                } else if (response.getError() != null) {
                                    new InfoMessage(getContext()).show(response.getError());
                                }
                            });
                } else {
                    new InfoMessage(getContext()).show(getString(R.string.warn_ids_not_match));
                }
            }
            //если самокат, то
            //узнать realState
            //запросить serverScooterState
            //вызвать handleScooterStates(realState, serverScooterState, rawBytes)

        }
    }

    public void handleScooterStates(byte realState, String serverState, byte[] packetFromLock){

        if(connection != null) {
            if (lock != null && connection.isEstablished()) {
                switch (LockHandler.getScooterButtonsConfig(realState, serverState)) {
                    case LockHandler.ACTIVATE_OPEN:
                        //если serverState = STOP,CLOSED и realState = CLOSED - показать кнопку ОТКРЫТЬ
                        snackbar.setText(getString(R.string.tip_you_can_open_lock))
                                .setAction(getString(R.string.title_open), v -> {
                                    snackbar.dismiss();
                                    onOpenClick();
                                })
                                .show();
                        break;
                    case LockHandler.ACTIVATE_CLOSE:
                        //если serverState = OPENED и realState = CLOSED - отправить пакет на сервер методом CLOSE
                        closeScooterOnServer(packetFromLock);
                        break;
                    case LockHandler.ACTIVATE_REOPEN:
                        //если serverState = GO,CLOSED и realState = CLOSED - показать кнопку ОТКРЫТЬ ЕЩЕ РАЗ
                        snackbar.setText(getString(R.string.title_reopen))
                                .setAction(getString(R.string.title_open), v -> {
                                    snackbar.dismiss();
                                    onReopenClick();
                                })
                                .show();

                        break;
                    case LockHandler.ACTIVATE_LOCAL_CLOSE:
                        //если serverState = GO,UNS_CLOSED||GO,CLOSED и realState = UNS_CLOSED - отправить на замок CLOSE_CMD
                        if (connection != null && lock.getClosePacket() != null) {
                            connection.writeInCharacteristic(BleConnection.LOCK_UUID, lock.getClosePacket());
                        }
                        break;
                }
            }
        }
    }

    public void handleStates(byte realState, byte serverState, byte[] packetFromLock) {
        if(connection != null) {
            if (lock != null && connection.isEstablished()) {
                switch (LockHandler.getButtonsConfig(realState, serverState)) {
                    case LockHandler.ACTIVATE_CLOSE:
                        new InfoMessage(getContext()).show(getString(R.string.tip_lock_is_opened));
                        snackbar.setText(getString(R.string.tip_you_can_close_lock))
                                .setAction(getString(R.string.title_close), v -> {
                                    snackbar.dismiss();
                                    onCloseClick();
                                })
                                .show();
                        break;
                    case LockHandler.ACTIVATE_OPEN:
                        snackbar.setText(getString(R.string.tip_you_can_open_lock))
                                .setAction(getString(R.string.title_open), v -> {
                                    snackbar.dismiss();
                                    onOpenClick();
                                })
                                .show();
                        break;
                    case LockHandler.ACTIVATE_SYNC:
                        gps.getLastLocation(location -> {
                            if (gps.age_minutes(location) < Codes.MAX_TIME_DELTA) {
                                Intent intent = new Intent(getContext(), BikePhotoActivity.class);
                                intent.putExtra("packetFromLock", packetFromLock);
                                intent.putExtra("realState", realState);
                                intent.putExtra("lat", location.getLatitude());
                                intent.putExtra("lng", location.getLongitude());
                                startActivityForResult(intent, Codes.REQUEST_TAKE_PHOTO);
                            } else {
                                gps.updateLocation();
                                new InfoMessage(getActivity()).show(getString(R.string.tip_gps_location_too_old));
                            }
                        });
                        break;
                }

            }
        }

    }

    private void onOpenClick() {
        if (lock != null && rideHandler != null) {
            gps.getLastLocation(location -> {
                if (gps.age_minutes(location) < Codes.MAX_TIME_DELTA) {
                    NetworkService
                            .openRequest(getContext(), lock.getID(), location, response -> {
                                if (response.getCommand() != null) {
                                    byte[] packetFromServer = HexString.hexToBytes(response.getCommand());
                                    if (connection != null) {
                                        connection.writeInCharacteristic(BleConnection.LOCK_UUID, packetFromServer);
                                        NetworkService.getUserState(getContext(), userStatusResponse -> {
                                            if (userStatusResponse.getDelta() != null && userStatusResponse.getLockId() != null && userStatusResponse.getLockState() != null) {
                                                setTimer(userStatusResponse.getDelta(), userStatusResponse.getLockId());
                                                lock.setServerState(userStatusResponse.getLockState());
                                            } else if (response.getError() != null) {
                                                new InfoMessage(getContext()).show(response.getError());
                                            }
                                        });
                                    }
                                } else if (response.getError() != null) {
                                    new InfoMessage(getContext()).show(response.getError());
                                }
                            });
                } else {
                    gps.updateLocation();
                    new InfoMessage(getActivity()).show(getString(R.string.tip_gps_location_too_old));
                }

            });
        }
    }

    private void onCloseClick() {
        if (lock != null && rideHandler != null) {
            new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.tip_press_next_if_ready))
                    .setPositiveButton(getString(R.string.title_next), (paramDialogInterface, paramInt) -> {
                        if (connection != null && lock.getClosePacket() != null) {
                            connection.writeInCharacteristic(BleConnection.LOCK_UUID, lock.getClosePacket());
                        }
                    })
                    .setNegativeButton(getString(R.string.CANCEL), null)
                    .show();
        }
    }

    private void closeScooterOnServer(byte[] packetFromLock) {
        gps.getLastLocation(location -> {
            if (gps.age_minutes(location) < Codes.MAX_TIME_DELTA) {
                NetworkService
                        .closeRequest(getContext(), packetFromLock, location, null, response -> {
                            hideWait();
                            if (connection != null && response.getCommand() != null) {
                                byte[] packetFromServer = HexString.hexToBytes(response.getCommand());
                                connection.writeInCharacteristic(BleConnection.LOCK_UUID, packetFromServer);
                            } else if (response.getState() != null) {
                                lock.setServerState(response.getState());
                            } else if (response.getError() != null) {
                                new InfoMessage(getContext()).show(response.getError());
                            }
                        });
            } else {
                gps.updateLocation();
                new InfoMessage(getActivity()).show(getString(R.string.tip_gps_location_too_old));
            }
        });
    }

    private void onEndScooterRideClick(){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        gps.getLastLocation(location -> {
            if (gps.age_minutes(location) < Codes.MAX_TIME_DELTA) {
                Intent intent = new Intent(getContext(), BikePhotoActivity.class);
                intent.putExtra("lockType", lock.getType());
                intent.putExtra("lockID", lock.getID());
                intent.putExtra("lat", location.getLatitude());
                intent.putExtra("lng", location.getLongitude());
                startActivityForResult(intent, Codes.REQUEST_TAKE_SCOOTER_PHOTO);
            } else {
                gps.updateLocation();
                new InfoMessage(getActivity()).show(getString(R.string.tip_gps_location_too_old));
            }
        });
    }

    private void onPulloutCableClick() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        snackbar.setText("НАЖМИТЕ КНОПКУ НА ЗАМКЕ #" + lock.getID())
                .setAction(getString(R.string.OK), v -> {
                    snackbar.dismiss();
                })
                .show();
    }

    private void onReopenClick () {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        NetworkService
                .reopen(getContext(), lock.getID(), response -> {
                    if (response.getCommand() != null) {
                        byte[] packetFromServer = HexString.hexToBytes(response.getCommand());
                        if (connection != null) {
                            connection.writeInCharacteristic(BleConnection.LOCK_UUID, packetFromServer);
                        }
                    } else if (response.getError() != null) {
                        new InfoMessage(getContext()).show(response.getError());
                    }
                });
    }

    public void setLockServerState(String state) {
        if(lock != null) {
            lock.setServerState(state);
        }
    }

    @Override
    public void connectionStateCallback(RxBleConnection.RxBleConnectionState connectionState) {
        if (connection != null && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                //change UI
                if (connectionState.equals(RxBleConnection.RxBleConnectionState.DISCONNECTED)) {
                    clearLockIndicator();
                }
            });
        }
    }

    private void setLockIndicator(byte realState, int chargeLevel) {
        String lockIndicatorTitle;
        if (lockIndicator != null) {
            lockIndicatorTitle = getString(R.string.title_id_number) + lock.getID() + " | " + chargeLevel + "%";
            switch (realState) {
                case Codes.UNS_CLOSED:
                case Codes.OPENED:
                    lockIndicator.setText(lockIndicatorTitle);
                    if(getContext() != null) {
                        lockIndicator.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_lock_open_black_24dp));
                    }

                    break;
                case Codes.UNS_OPENED:
                case Codes.CLOSED:
                    lockIndicator.setText(lockIndicatorTitle);
                    if(getContext() != null) {
                        lockIndicator.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_lock_black_24dp));
                    }
                    break;
            }
        }
    }

    public void setTimer(String delta, String lockID) {
        if (timer != null) {
            timer.stop();
            timer.setVisibility(View.INVISIBLE);
            try {
                long ridingTime = (long) (Float.parseFloat(delta) * 1000);
                timer.setBase(SystemClock.elapsedRealtime() - ridingTime);
                timer.setFormat("ID#" + lockID + " | " + " "+"%s");
                timer.start();
                timer.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                log(e.toString());
            }

        }
    }

    public void hideTimer() {
        if(timer != null) {
            timer.setVisibility(View.INVISIBLE);
        }
    }

    private void clearLockIndicator() {
        if (lockIndicator != null) {
            lockIndicator.setText(R.string.title_disconnected);
            if(getContext() != null) {
                lockIndicator.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_lock_black_24dp));
            }
        }
    }

    public void showWait() {
        if(wait != null) {
            wait.setVisibility(View.VISIBLE);
        }
    }

    public void hideWait() {
        if(wait != null) {
            wait.setVisibility(View.INVISIBLE);
        }
    }

    private void onUserAccountClick() {
        if(getFragmentManager() != null && getActivity() != null) {
            boolean isHomeAdded = ((MainActivity) getActivity()).getmHomeFragment().isAdded();
            if(!isHomeAdded) {
                getFragmentManager().beginTransaction()
                        .add(R.id.host_fragment, ((MainActivity) getActivity()).getmHomeFragment(), "Home")
                        .addToBackStack(null)
                        .commit();
            }
        }

    }

    private void onHelpClick() {
        if(getActivity() != null && getActivity().getSupportFragmentManager() != null) {
            boolean isHelpAdded = ((MainActivity) getActivity()).getmHelpFragment().isAdded();
            if(!isHelpAdded) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.host_fragment, ((MainActivity) getActivity()).getmHelpFragment(), "Help")
                        .addToBackStack(null)
                        .commit();
            }
        }
    }

    private void showMyLocationOnMap() {
        enableMyLocation();
        gps.getLastLocation(location -> {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zoom(13)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        });
    }

    public void onLocationPermissionsGranted() {
        showMyLocationOnMap();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnMarkerClickListener(this);
        markers.clear();
        showMyLocationOnMap();

        NetworkService.getZones(getContext(), response -> {
            for(List<Point> zone: response.getZones()) {
                PolygonOptions rectOptions = new PolygonOptions()
                        .strokeColor(Color.MAGENTA)
                        .strokeWidth(1)
                        .fillColor(strokeColorArgb);
                for(Point point: zone) {
                    rectOptions.add(new LatLng(point.getLat(), point.getLng()));
                }
                mMap.addPolygon(rectOptions);
            }
        });

        NetworkService.getLocks(getContext(), response -> {
            if (response != null) {
                addMarkers(response);
            }

        });
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        return false;
    }

    private void enableMyLocation() {
        if(getActivity() != null) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setCompassEnabled(false);
            } else {
                Toast.makeText(getContext(), getString(R.string.warn_no_location_permission), Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onCameraIdle() {
    }

    private void addMarkers(List<LockInfo> locksList) {
        for (LockInfo lockInfo : locksList) {
            mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lockInfo.getLat(), lockInfo.getLng()))
                        .title("Замок #" + lockInfo.getLockId())
                        .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(getContext(), R.drawable.ic_directions_bike_black_24dp))));
        }
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private Boolean checkPhoneFunctions(Context context) {
        if (!Ble.isEnabled()) {
            new InfoMessage(context).show(getString(R.string.tip_enable_bluetooth));
            return false;
        }
        if (!gps.isEnabled()) {
            new InfoMessage(context).show(getString(R.string.tip_enable_gps));
            return false;
        }
        return true;
    }


    private void log(String message) {
        Log.i(getClass().getSimpleName(), message);
    }
}
