package guardianangel;

import android.content.Context;

import androidx.preference.PreferenceManager;


import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.entities.MiBandActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class GAServerUtil {

    static RequestBody getRequestBodyFromMiBandActivitySample(MiBandActivitySample sample) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("timestamp", sample.getTimestamp());
            jsonObj.put("device_id", sample.getDeviceId());
            jsonObj.put("user_id", sample.getUserId());
            jsonObj.put("raw_intensity", sample.getRawIntensity());
            jsonObj.put("steps", sample.getSteps());
            jsonObj.put("raw_kind", sample.getRawKind());
            jsonObj.put("heart_rate", sample.getHeartRate());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return RequestBody.create(MediaType
                .parse("application/json"), jsonObj.toString());

        /*Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonBody = gson.toJson(ActivitySampleJsonSerializable.fromMiBandActivitySample(sample));
        return RequestBody.create(MediaType
                .parse("application/json"), jsonBody);*/
    }

    static RequestBody getRequestBodyFromMultipleMiBandActivitySamples(MiBandActivitySample[] samples) {
        JSONArray jsonArray = new JSONArray();
        Arrays.stream(samples).forEach((sample) -> {
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("timestamp", sample.getTimestamp());
                jsonObj.put("device_id", sample.getDeviceId());
                jsonObj.put("user_id", sample.getUserId());
                jsonObj.put("raw_intensity", sample.getRawIntensity());
                jsonObj.put("steps", sample.getSteps());
                jsonObj.put("raw_kind", sample.getRawKind());
                jsonObj.put("heart_rate", sample.getHeartRate());

                jsonArray.put(jsonObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        String jsonBody = jsonArray.toString();
        return RequestBody.create(MediaType.parse("application/json"), jsonBody);


        /*Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ActivitySampleJsonSerializable[] serializableSamples = new ActivitySampleJsonSerializable[samples.length];
        for (int i = 0; i < samples.length; i++) {
            serializableSamples[i] = ActivitySampleJsonSerializable.fromMiBandActivitySample(samples[i]);
        }
        String jsonBody = gson.toJson(serializableSamples);
        return RequestBody.create(MediaType
                .parse("application/json"), jsonBody);*/
    }

    static RequestBody getRequestBodyFromJsonString(String jsonString) {
        return RequestBody.create(MediaType.parse("application/json"), jsonString);
    }

    private String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }

    public static String loadToken(Context context) {
        Context appContext = context.getApplicationContext();
        Prefs prefs = new Prefs(PreferenceManager.getDefaultSharedPreferences(appContext));
        return prefs.getString(appContext.getString(R.string.guardian_angel_token_preference_key), "");
    }

    public static RequestBody getRequestBodyForAudioFile(File file) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("file", dtf.format(now) + ".mp4", RequestBody.create(MediaType.parse("audio/mp4"), file)).build();
    }

}
