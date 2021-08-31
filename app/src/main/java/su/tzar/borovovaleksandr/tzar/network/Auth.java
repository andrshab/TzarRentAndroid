package su.tzar.borovovaleksandr.tzar.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import su.tzar.borovovaleksandr.tzar.App;
import su.tzar.borovovaleksandr.tzar.R;
import com.vk.api.sdk.VK;

import static android.content.Context.MODE_PRIVATE;


public class Auth {
    public static boolean isAuthorized(Context context) {
        SharedPreferences sPrefs = context.getSharedPreferences(context.getString(R.string.tag_prefernces_name), Context.MODE_PRIVATE);
        log(sPrefs.getString(context.getString(R.string.tag_token), "null"));
        Boolean isJWT = sPrefs.getString(context.getString(R.string.tag_token), "null") != "null";
        return VK.isLoggedIn() && isJWT;
    }

    public static void deleteJWT(Context context){
        SharedPreferences sPrefs = context.getSharedPreferences(context.getString(R.string.tag_prefernces_name), Context.MODE_PRIVATE);
        sPrefs.edit().putString(context.getString(R.string.tag_token), "null").apply();
        sPrefs.edit().putString(context.getString(R.string.tag_refresh_token), "null").apply();
    }

    public static String getToken(Context context){
        SharedPreferences sPrefs = context.getSharedPreferences(context.getString(R.string.tag_prefernces_name), MODE_PRIVATE);
        return sPrefs.getString(context.getString(R.string.tag_token), "null");
    }

    private static void log(String message) {
        Log.i(Auth.class.getSimpleName(), message);
    }
}
