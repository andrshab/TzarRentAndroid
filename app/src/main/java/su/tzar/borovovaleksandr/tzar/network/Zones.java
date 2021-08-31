package su.tzar.borovovaleksandr.tzar.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Zones {
    @SerializedName("zones")
    @Expose
    private List<List<Point>> zones;

    public List<List<Point>> getZones() {
        return zones;
    }
}
