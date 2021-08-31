package su.tzar.borovovaleksandr.tzar.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LockState {
    @SerializedName("state")
    @Expose
    private String state;
    @SerializedName("command")
    @Expose
    private String command;
    @SerializedName("error")
    @Expose
    private String error;

    public String getState() {
        return state;
    }
    public String getCoomand() {
        return command;
    }
    public String getError(){
        return error;
    }
    public void setError(String error) {this.error = error;}

}
