package su.tzar.borovovaleksandr.tzar.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserStatus {
    @SerializedName("balance")
    @Expose
    private String balance;
    @SerializedName("delta")
    @Expose
    private String delta;
    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("lock_id")
    @Expose
    private String lockId;
    @SerializedName("lock_state")
    @Expose
    private String lockState;
    @SerializedName("lock_type")
    @Expose
    private String lockType;

    public String getError() {
        return error;
    }
    public String getDelta() {
        return delta;
    }
    public String getBalance() {
        return balance;
    }
    public String getLockId() {return lockId;}
    public String getLockState() {return lockState;}
    public String getLockType() {return lockType;}
    public void setError(String error) {this.error = error;}
}
