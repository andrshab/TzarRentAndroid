package su.tzar.borovovaleksandr.tzar.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CloseRequest {
    @SerializedName("command")
    @Expose
    private String command;

    public CloseRequest(String command){
        this.command = command;
    }
}
