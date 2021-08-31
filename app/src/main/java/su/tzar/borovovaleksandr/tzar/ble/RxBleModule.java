package su.tzar.borovovaleksandr.tzar.ble;

import android.content.Context;

import com.polidea.rxandroidble2.RxBleClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RxBleModule {
    @Provides
    @Singleton
    RxBleClient provideRxBleClient(Context context){
        return RxBleClient.create(context);
    }
    @Provides
    @Singleton
    BleScanner provideBleScanner(){return new BleScanner();}
    @Provides
    @Singleton
    Ble provideBle(){return new Ble();}
    @Provides
    @Singleton
    BleDevice provideBleDevice(){return new BleDevice();}
}
