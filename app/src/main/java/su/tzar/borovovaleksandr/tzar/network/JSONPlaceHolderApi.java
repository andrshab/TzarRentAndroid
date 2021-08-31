package su.tzar.borovovaleksandr.tzar.network;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface JSONPlaceHolderApi {
    @GET("locks/")
    Call<List<LockInfo>> getLocks();
    @Headers("Content-Type: application/json")
    @POST("auth/")
    Call<JWT> getJWT(@Body AuthRequest body);
    @GET("status/")
    Call<LockState> getLockState(@Header("Authorization") String token, @Query("command") String command);
    @GET("open/")
    Call<Packet> openRequest(@Header("Authorization") String token, @Query("id") String id, @Query("lat") String lat, @Query("lng") String lng);
    @Multipart
    @POST("close/")
    Call<Packet> closeRequest(@Header("Authorization") String token, @Part("command") RequestBody command, @Part("lat") RequestBody lat, @Part("lng") RequestBody lng, @Part MultipartBody.Part photo);
    @POST("refresh/")
    Call<Access> refreshRequest(@Body RefreshRequest body);
    @GET("user_status/")
    Call<UserStatus> getUserState(@Header("Authorization") String token);
    @POST("pay/")
    Call<SecureURL> getSecureURL(@Header("Authorization") String token , @Body PaymentToken body);
    @POST("update_token/")
    Call<UpdateFirebaseTokenResponse> sendFirebaseToken(@Header("Authorization") String token, @Body FirebaseToken body);
    @GET("info/")
    Call<ResponseBody> getInfoPage();
    @GET("zones/")
    Call<Zones> getZones();
    @Multipart
    @POST("close-scooter/")
    Call<Packet> closeScooterRequest(@Header("Authorization") String token, @Part("id") RequestBody id, @Part("lat") RequestBody lat, @Part("lng") RequestBody lng, @Part MultipartBody.Part photo);
    @GET("reopen/")
    Call<Packet> reopen(@Header("Authorization") String token, @Query("id") String lockID);
}
