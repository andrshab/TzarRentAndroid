package su.tzar.borovovaleksandr.tzar.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LockInfo {
    @SerializedName("url") //this annotation maps serialized name with field name
    @Expose //this annotation to allow serialization and deserialization
    private String url;
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("lng")
    @Expose
    private Double lng;
    @SerializedName("lock_id")
    @Expose
    private int lock_id;

    public String getUrl() {
        return url;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public int getLockId() {
        return lock_id;
    }
}
