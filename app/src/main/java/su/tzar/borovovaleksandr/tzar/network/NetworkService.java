package su.tzar.borovovaleksandr.tzar.network;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import su.tzar.borovovaleksandr.tzar.App;
import su.tzar.borovovaleksandr.tzar.R;
import su.tzar.borovovaleksandr.tzar.ble.HexString;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkService {
    public interface Listener<T> {
        void onResponse(T response);
    }

    private static NetworkService mInstance;
    private static final String BASE_URL = "https://sharing.tzar.su/api/";
    private Retrofit mRetrofit;
    private HttpLoggingInterceptor mLogging;
    private OkHttpClient.Builder mHttpClient;
    private TokenAuthenticator tokenAuthenticator;

    private NetworkService(Context context) {
        mLogging = new HttpLoggingInterceptor();
        mLogging.setLevel(HttpLoggingInterceptor.Level.BODY);
        mHttpClient = new OkHttpClient.Builder();
        tokenAuthenticator = new TokenAuthenticator(context);
        mHttpClient
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .authenticator(tokenAuthenticator)
                .addInterceptor(mLogging);

        Gson gson = new GsonBuilder().create();

        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(mHttpClient.build())
                .build();
    }

    public static NetworkService getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NetworkService(context);
        }
        return mInstance;
    }

    public static void getJWT(Context context, AuthRequest authRequest, Listener<JWT> listener) {
        NetworkService
                .getInstance(context)
                .getJSONApi()
                .getJWT(authRequest)
                .enqueue(new Callback<JWT>() {
                    @Override
                    public void onResponse(Call<JWT> call, Response<JWT> response) {
                        if (response.body() != null) {
                            listener.onResponse(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<JWT> call, Throwable t) {
                        log("Server authorization failed. Call to support.");
                        JWT jwt = new JWT();
                        jwt.setError(context.getString(R.string.warn_jwt));
                        listener.onResponse(jwt);
                    }
                });
    }

    public static void openRequest(Context context, String lockID, Location location, Listener<Packet> listener) {
        NetworkService
                .getInstance(context)
                .getJSONApi()
                .openRequest("Bearer " + Auth.getToken(context),
                        lockID,
                        Double.toString(location.getLatitude()),
                        Double.toString(location.getLongitude()))
                .enqueue(new Callback<Packet>() {
                    @Override
                    public void onResponse(Call<Packet> call, Response<Packet> response) {
                        //send callback to fragment to show open button
                        if (response.body() != null) {
                            listener.onResponse(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<Packet> call, Throwable t) {
                        log("Open request failure");
                        Packet packet = new Packet();
                        packet.setError(context.getString(R.string.warn_open_packet));
                        listener.onResponse(packet);
                    }
                });
    }

    public static void closeRequest(Context context, byte[] packetFromLock, Location location, String photoPath, Listener<Packet> listener) {
        RequestBody commandBody = RequestBody.create(MediaType.parse("text/plain"), HexString.bytesToHex(packetFromLock));
        RequestBody latBody = RequestBody.create(MediaType.parse("text/plain"), Double.toString(location.getLatitude()));
        RequestBody lngBody = RequestBody.create(MediaType.parse("text/plain"), Double.toString(location.getLongitude()));
        MultipartBody.Part photoPart = null;
        if(photoPath != null) {
            File photoFile = new File(photoPath);
            photoPart = MultipartBody.Part.createFormData("photo", "android.jpg", RequestBody.create(MediaType.parse("image/*"), photoFile));
        }

        NetworkService
                .getInstance(context)
                .getJSONApi()
                .closeRequest("Bearer " + Auth.getToken(context), commandBody, latBody, lngBody, photoPart)
                .enqueue(new Callback<Packet>() {
                    @Override
                    public void onResponse(Call<Packet> call, Response<Packet> response) {
                        //send callback to fragment to show open button
                        if (response.body() != null) {
                            listener.onResponse(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<Packet> call, Throwable t) {
                        log("Close request failure");
                        Packet packet = new Packet();
                        packet.setError(context.getString(R.string.warn_close_packet));
                        listener.onResponse(packet);
                    }
                });
    }

    public static void closeScooterRequest(Context context, String lockID, Location location, String photoPath, Listener<Packet> listener){
        RequestBody lockIDBody = RequestBody.create(MediaType.parse("text/plain"), lockID);
        RequestBody latBody = RequestBody.create(MediaType.parse("text/plain"), Double.toString(location.getLatitude()));
        RequestBody lngBody = RequestBody.create(MediaType.parse("text/plain"), Double.toString(location.getLongitude()));
        File photoFile = new File(photoPath);
        MultipartBody.Part photoPart = MultipartBody.Part.createFormData("photo", "android.jpg", RequestBody.create(MediaType.parse("image/*"), photoFile));
        NetworkService
                .getInstance(context)
                .getJSONApi()
                .closeScooterRequest("Bearer " + Auth.getToken(context), lockIDBody, latBody, lngBody, photoPart)
                .enqueue(new Callback<Packet>() {
                    @Override
                    public void onResponse(Call<Packet> call, Response<Packet> response) {
                        if (response.body() != null) {
                            listener.onResponse(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<Packet> call, Throwable t) {
                        log("Close scooter request failure");
                        Packet packet = new Packet();
                        packet.setError(context.getString(R.string.warn_close_packet));
                        listener.onResponse(packet);
                    }
                });
    }

    public static void reopen(Context context, String lockID, Listener<Packet> listener){
        NetworkService
                .getInstance(context)
                .getJSONApi()
                .reopen("Bearer " + Auth.getToken(context), lockID)
                .enqueue(new Callback<Packet>() {
                    @Override
                    public void onResponse(Call<Packet> call, Response<Packet> response) {
                        if (response.body() != null) {
                            listener.onResponse(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<Packet> call, Throwable t) {
                        log("Close scooter request failure");
                        Packet packet = new Packet();
                        packet.setError(context.getString(R.string.warn_close_packet));
                        listener.onResponse(packet);
                    }
                });
    }

    public static void getLockState(Context context, String lockID, Listener<LockState> listener) {
        NetworkService
                .getInstance(context)
                .getJSONApi()
                .getLockState("Bearer " + Auth.getToken(context), lockID)
                .enqueue(new Callback<LockState>() {
                    @Override
                    public void onResponse(Call<LockState> call, Response<LockState> response) {
                        if (response.body() != null) {
                            listener.onResponse(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<LockState> call, Throwable t) {
                        log("LockInfo state request failure");
                        LockState lockState = new LockState();
                        lockState.setError(context.getString(R.string.warn_lock_state));
                        listener.onResponse(lockState);
                    }
                });
    }

    public static void getLocks(Context context, Listener<List<LockInfo>> listener) {
        NetworkService
                .getInstance(context)
                .getJSONApi()
                .getLocks()
                .enqueue(new Callback<List<LockInfo>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<LockInfo>> call, @NonNull Response<List<LockInfo>> response) {
                        listener.onResponse(response.body());
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<LockInfo>> call, @NonNull Throwable t) {
                        log("Locks request failure");
                    }
                });
    }

    public static void getUserState(Context context, Listener<UserStatus> listener) {
        NetworkService
                .getInstance(context)
                .getJSONApi()
                .getUserState("Bearer " + Auth.getToken(context))
                .enqueue(new Callback<UserStatus>() {
                    @Override
                    public void onResponse(Call<UserStatus> call, Response<UserStatus> response) {
                        if (response.body() != null){
                            listener.onResponse(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<UserStatus> call, Throwable t) {
                        log("UserStatus request failure");
                        UserStatus userStatus = new UserStatus();
                        userStatus.setError(context.getString(R.string.warn_user_data));
                        listener.onResponse(userStatus);
                    }
                });
    }

    public static void getSecureURL(Context context, PaymentToken paymentToken, Listener<SecureURL> listener) {
        NetworkService
                .getInstance(context)
                .getJSONApi()
                .getSecureURL("Bearer " + Auth.getToken(context), paymentToken)
                .enqueue(new Callback<SecureURL>() {
                    @Override
                    public void onResponse(Call<SecureURL> call, Response<SecureURL> response) {
                        if(response.body() != null) {
                            listener.onResponse(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<SecureURL> call, Throwable t) {
                        log("SecureURL request failure");
                        SecureURL secureURL = new SecureURL();
                        secureURL.setError(context.getString(R.string.warn_3DS));
                        listener.onResponse(secureURL);
                    }
                });
    }

    public static void sendFirebaseToken(Context context, FirebaseToken firebaseToken, Listener<UpdateFirebaseTokenResponse> listener) {
        NetworkService
                .getInstance(context)
                .getJSONApi()
                .sendFirebaseToken("Bearer " + Auth.getToken(context), firebaseToken)
                .enqueue(new Callback<UpdateFirebaseTokenResponse>() {
                    @Override
                    public void onResponse(Call<UpdateFirebaseTokenResponse> call, Response<UpdateFirebaseTokenResponse> response) {
                        if(response.body() != null) {
                            listener.onResponse(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<UpdateFirebaseTokenResponse> call, Throwable t) {
                        log("UpdateFirebaseToken request failure");
                        UpdateFirebaseTokenResponse updateFirebaseTokenResponse = new UpdateFirebaseTokenResponse();
                        updateFirebaseTokenResponse.setError(context.getString(R.string.warn_firebasetoken));
                        listener.onResponse(updateFirebaseTokenResponse);
                    }
                });
    }

    public static void getInfoPage(Context context, Listener<ResponseBody> listener) {
        NetworkService
                .getInstance(context)
                .getJSONApi()
                .getInfoPage()
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.body() != null) {
                            listener.onResponse(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        log("getInfoPage failure");
                    }
                });
    }

    public static void getZones(Context context, Listener<Zones> listener) {
        NetworkService
                .getInstance(context)
                .getJSONApi()
                .getZones()
                .enqueue(new Callback<Zones>() {
                    @Override
                    public void onResponse(Call<Zones> call, Response<Zones> response) {
                        if(response.body() != null) {
                            listener.onResponse(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<Zones> call, Throwable t) {
                        log("getZones failure");
                    }
                });
    }

    public JSONPlaceHolderApi getJSONApi() {
        return mRetrofit.create(JSONPlaceHolderApi.class);
    }

    private static void log(String message) {
        Log.i(NetworkService.class.getSimpleName(), message);
    }
}
