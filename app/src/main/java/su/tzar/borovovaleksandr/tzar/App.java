package su.tzar.borovovaleksandr.tzar;


import androidx.multidex.MultiDexApplication;

import su.tzar.borovovaleksandr.tzar.ble.RxBleModule;
import com.polidea.rxandroidble2.exceptions.BleException;

import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;


public class App extends MultiDexApplication {
    private static AppComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
//        //https://github.com/Polidea/RxAndroidBle/wiki/FAQ:-UndeliverableException
        RxJavaPlugins.setErrorHandler(throwable -> {
            if (throwable instanceof UndeliverableException && throwable.getCause() instanceof BleException) {
                return; // ignore BleExceptions as they were surely delivered at least once
            }
            // add other custom handlers if needed
            throw new RuntimeException("Unexpected Throwable in RxJavaPlugins error handler", throwable);
        });
        component = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .rxBleModule(new RxBleModule()).build();
    }
    public static AppComponent getComponent() {
        return component;
    }
}
