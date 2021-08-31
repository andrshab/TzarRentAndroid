package su.tzar.borovovaleksandr.tzar.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RefreshRequest {
    @SerializedName("refresh")
    @Expose
    private String refreshToken;

    public RefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
