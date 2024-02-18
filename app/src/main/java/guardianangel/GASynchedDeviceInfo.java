package guardianangel;

import android.annotation.SuppressLint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GASynchedDeviceInfo {
    private String deviceId;
    private String deviceName;
    private Date lastUpdated;
    private Date createdAt;

    public GASynchedDeviceInfo(String deviceId, String deviceName, Date lastUpdated, Date createdAt) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.lastUpdated = lastUpdated;
        this.createdAt = createdAt;
    }

    public static GASynchedDeviceInfo fromJsonString(String jsonString) throws ParseException {
        Date lastUpdated = null;
        Date createdAt = null;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
        if(!obj.get("last_updated").isJsonNull()){
            lastUpdated = dateFormat.parse(obj.get("last_updated").getAsString());
        }
        createdAt = dateFormat.parse(obj.get("created_at").getAsString());
        return new GASynchedDeviceInfo(obj.get("device_id").getAsString(), obj.get("device_name").getAsString(), lastUpdated, createdAt);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "GASynchedDeviceInfo{" +
                "device_id='" + deviceId + '\'' +
                ", device_name='" + deviceName + '\'' +
                ", last_updated=" + lastUpdated +
                ", created_at=" + createdAt +
                '}';
    }
}
