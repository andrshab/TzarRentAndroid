package su.tzar.borovovaleksandr.tzar.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateFirebaseTokenResponse {

    @SerializedName("result")
    @Expose
    private String result;

    @SerializedName("error")
    @Expose
    private String error;

    public String getResult() {
        return result;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {this.error = error;}
}
