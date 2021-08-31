package su.tzar.borovovaleksandr.tzar.helper;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class HelperModule {
    @Provides
    @Singleton
    RideHandler provideRideHander(Context context){return new RideHandler(context);}
}