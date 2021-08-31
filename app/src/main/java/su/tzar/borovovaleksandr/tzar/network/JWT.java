package su.tzar.borovovaleksandr.tzar.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JWT {
    @SerializedName("refresh")
    @Expose
    private String refreshToken;

    @SerializedName("token")
    @Expose
    private String token;

    @SerializedName("error")
    @Expose
    private String error;

    public String getRefreshToken() {
        return refreshToken;
    }
    public String getToken() {
        return token;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {this.error = error;}
}
