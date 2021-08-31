package su.tzar.borovovaleksandr.tzar.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuthRequest {
    @SerializedName("token")
    @Expose
    private String vkToken;

    public AuthRequest(String vkToken){
        this.vkToken = vkToken;
    }
}
