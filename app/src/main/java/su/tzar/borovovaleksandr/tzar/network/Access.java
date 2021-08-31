package su.tzar.borovovaleksandr.tzar.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Access {
    @SerializedName("access")
    @Expose
    private String access;

    public String getNewAccessToken() {
        return access;
    }
}
