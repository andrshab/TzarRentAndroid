package su.tzar.borovovaleksandr.tzar;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private Context appContext;

    public AppModule(Context context){
        appContext = context;
    }
    @Provides
    @Singleton
    Context provideContext(){
        return appContext;
    }
}
