package su.tzar.borovovaleksandr.tzar.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FirebaseToken {
    @SerializedName("android_token")
    @Expose
    private String firebaseToken;

    public FirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }
}
