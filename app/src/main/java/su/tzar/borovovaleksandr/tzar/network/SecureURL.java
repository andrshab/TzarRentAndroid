package su.tzar.borovovaleksandr.tzar.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SecureURL {
    @SerializedName("url") //this annotation maps serialized name with field name
    @Expose //this annotation to allow serialization and deserialization
    private String url;

    @SerializedName("error")
    @Expose
    private String error;

    public String getUrl() {
        return url;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {this.error = error;}
}
