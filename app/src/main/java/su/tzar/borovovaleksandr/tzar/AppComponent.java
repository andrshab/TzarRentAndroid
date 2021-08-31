package su.tzar.borovovaleksandr.tzar;

import su.tzar.borovovaleksandr.tzar.activity.MainActivity;
import su.tzar.borovovaleksandr.tzar.ble.Ble;
import su.tzar.borovovaleksandr.tzar.ble.BleScanner;
import su.tzar.borovovaleksandr.tzar.ble.RxBleModule;
import su.tzar.borovovaleksandr.tzar.fragment.MapFragment;
import su.tzar.borovovaleksandr.tzar.helper.HelperModule;
import su.tzar.borovovaleksandr.tzar.helper.RideHandler;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {HelperModule.class, RxBleModule.class, AppModule.class})
@Singleton
public interface AppComponent {
    void injectMainActivity(MainActivity mainActivity);
    void injectBle(Ble ble);
    void injectBleScanner(BleScanner bleScanner);
    void injectMapFragment(MapFragment mapFragment);
    void injectRideHandler(RideHandler rideHandler);
}
