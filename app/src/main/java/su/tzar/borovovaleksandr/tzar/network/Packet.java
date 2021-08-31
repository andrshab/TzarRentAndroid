package su.tzar.borovovaleksandr.tzar.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Packet {
    @SerializedName("command")
    @Expose
    private String packet;

    @SerializedName("state")
    @Expose
    private String state;

    @SerializedName("error")
    @Expose
    private String error;

    public String getCommand(){
        return packet;
    }

    public String getState(){
        return state;
    }

    public String getError(){
        return error;
    }
    public void setError(String error) {this.error = error;}

}
