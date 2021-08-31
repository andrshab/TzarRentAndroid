package su.tzar.borovovaleksandr.tzar.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import su.tzar.borovovaleksandr.tzar.App;
import su.tzar.borovovaleksandr.tzar.R;
import com.vk.api.sdk.VK;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

public class TokenAuthenticator implements Authenticator {
    private Context context;
    public TokenAuthenticator(Context context) {
        this.context = context;
    }
    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        if(response.request().url().equals(HttpUrl.parse("https://sharing.tzar.su/api/refresh/")) && response.code() == 401) {
            //TODO: what to if refresh token has expired?
            Auth.deleteJWT(context);
            VK.logout();
            Log.i("TokenAuthenticator", "Logged out. Refresh Token not work");
            return null;
        }
        if (responseCount(response) >= 3) {
            return null; // If we've failed 3 times, give up. - in real life, never give up!!
        }
        // Refresh your access_token using a synchronous api request
        SharedPreferences sPrefs = context.getSharedPreferences(context.getString(R.string.tag_prefernces_name), Context.MODE_PRIVATE);
        String refreshToken = sPrefs.getString(context.getString(R.string.tag_refresh_token), "null");

        if(!refreshToken.equals("null")){
            Call<Access> call = NetworkService
                    .getInstance(context)
                    .getJSONApi()
                    .refreshRequest(new RefreshRequest(refreshToken));
            Access access = call.execute().body();
            if(access!=null && access.getNewAccessToken() != null){
                String newAccessToken = access.getNewAccessToken();
                sPrefs.edit().putString(context.getString(R.string.tag_token), newAccessToken).apply();
                // Add new header to rejected request and retry it
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + newAccessToken)
                        .build();
            } else {
                Log.i("TokenAuthenticator", "Refresh Token not work");
                return null;
            }
        }
        return null;
    }
    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
