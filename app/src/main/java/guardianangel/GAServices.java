package guardianangel;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.devices.miband.AbstractMiBandFWInstallHandler;
import nodomain.freeyourgadget.gadgetbridge.devices.pebble.PBWReader;
import nodomain.freeyourgadget.gadgetbridge.entities.MiBandActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.User;
import nodomain.freeyourgadget.gadgetbridge.util.DateTimeUtils;

import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEOperation;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;

public class GAServices {
    private static final Logger LOG = LoggerFactory.getLogger(GAServices.class);
    //static final String baseUrl = "https://guardian-angel-server.herokuapp.com/";
    static final String baseUrl = "https://angel.limes.tech/";
    static final String base64PW = "Basic YWRtaW46a2lhcmFqZWpha29kb2JhcnBhcw=="; // This is the password for the production environment
//    static final String baseUrl = "http://192.168.0.19:8080/";
//    static final String baseUrl = "http://10.0.2.2:8080/"; // emulator
    //static final String baseUrl = "https://b113-87-116-166-147.ngrok.io/";
    //static final String base64PW = "Basic YWRtaW46YWRtaW4="; // This is the password you need if your server is running in debug with password admin

    static final OkHttpClient client = new OkHttpClient();

    static GregorianCalendar sendReqCal = null;

    private static void sendRequestAsync(Request request, Callback callback) {
        sendRequestAsync(request, callback, null);
    }

    private static void sendRequestAsync(Request request, Callback callback, GregorianCalendar cal) {
        sendReqCal = cal;
        if (callback == null) {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful())
                            throw new IOException("Unexpected code " + response);

                        Headers responseHeaders = response.headers();
                        for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                            System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                        }
                        assert responseBody != null;
                        System.out.println(responseBody.string());

                        // Response positiv means database was reached
                        // therefore the DBTimestamp is going to be set.

                        // PJ. Save lastdbSyncTimestamp
                        if (sendReqCal != null && sendReqCal.getTimeInMillis() > 0) {
                            GBApplication.saveLastDBSyncTimestamp("lastDBSyncTimeMillis", sendReqCal);
                        }
                    }
                }
            });
        } else {
            client.newCall(request).enqueue(callback);
        }

    }

    public static void postSampleToServer(MiBandActivitySample sample, String token) {
        postSampleToServer(sample, token, null);
    }

    public static void postSampleToServer(MiBandActivitySample sample, String token, GregorianCalendar timestamp) {
        Request request = new Request.Builder()
                .url(baseUrl + "data_store/save_sample/?token=" + token).
                        addHeader("Authorization", base64PW).
                        addHeader("Content-Type", "application/json").
                        post(GAServerUtil.getRequestBodyFromMiBandActivitySample(sample))
                .build();
        sendRequestAsync(request, null, timestamp);
    }

    public static void postMultipleSamplesToServer(MiBandActivitySample[] samples, String token) {
        postMultipleSamplesToServer(samples, token, null);
    }

    public static void postMultipleSamplesToServer(MiBandActivitySample[] samples, String token, GregorianCalendar timestamp) {
        if (samples != null) {
            Request request = new Request.Builder()
                    .url(baseUrl + "data_store/save_multiple_samples/?token=" + token).addHeader("Authorization", base64PW).
                            addHeader("Content-Type", "application/json").
                            post(GAServerUtil.getRequestBodyFromMultipleMiBandActivitySamples(samples))
                    .build();
            sendRequestAsync(request, null, timestamp);
        }

    }

    public static void synchronizeDevice(String deviceName, int userId, Callback callback) {

        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("device_name", deviceName);
        jsonObj.addProperty("user", userId);
        String jsonString = jsonObj.toString();
        Request request = new Request.Builder()
                .url(baseUrl + "device_handling/synchronize_device/").
                        addHeader("Authorization", base64PW).
                        addHeader("Content-Type", "application/json").
                        post(GAServerUtil.getRequestBodyFromJsonString(jsonString)).
                        build();
        // Callback needs to take the response uuid and store it into the preferences
        sendRequestAsync(request, callback);
    }

    public static void getInformationAboutSynchedDevice(String token, Callback callback) {
        Request request = new Request.Builder().
                url(baseUrl + "device_handling/synchronize_device/?token=" + token).
                addHeader("Authorization", base64PW).
                get().
                build();
        sendRequestAsync(request, callback);
    }

    public static void removeSync(String token, Callback callback) {
        Request request = new Request.Builder().
                url(baseUrl + "device_handling/synchronize_device/?token=" + token).
                addHeader("Authorization", base64PW).
                delete().
                build();
        sendRequestAsync(request, callback);
    }

    public static void installLatestApk(String token, Callback callback) {
        Request request = new Request.Builder().url(baseUrl + "device_handling/apk_download/?token=" + token).
                addHeader("Authorization", base64PW).
                get().
                build();
        sendRequestAsync(request, callback);
    }

    public static void flagForUpdate(String token, String version, Callback callbackForUpdating) {
        Request request = new Request.Builder().
                url(baseUrl + "device_handling/device_should_update/?token=" + token + "&version=" + version).
                addHeader("Authorization", base64PW).
                get().
                build();
        sendRequestAsync(request, callbackForUpdating);
    }

    public static void postAudioFileToServer(File audioFile, String token) {
        Request request = new Request.Builder().url(baseUrl + "event_handling/post_recorded_event/?token=" + token).
                addHeader("Authorization", base64PW).
                addHeader("Content-Type", "multipart/form-data").
                post(GAServerUtil.getRequestBodyForAudioFile(audioFile)).build();
        sendRequestAsync(request, null);
    }

    public static void createNewUser(String username, String password, Callback callback) {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("username", username);
        jsonObj.addProperty("password", password);
        String jsonString = jsonObj.toString();
        Request request = new Request.Builder().url(baseUrl + "create-user/").
                addHeader("Authorization", base64PW).
                addHeader("Content-Type", "application/json").
                post(GAServerUtil.getRequestBodyFromJsonString(jsonString)).
                build();
        sendRequestAsync(request, callback);
    }

    public static void validateUser(String username, String password, Callback callback) {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("username", username);
        jsonObj.addProperty("password", password);
        String jsonString = jsonObj.toString();
        Request request = new Request.Builder().url(baseUrl + "guardian-angel/login/").
                addHeader("Content-Type", "application/json").
                post(GAServerUtil.getRequestBodyFromJsonString(jsonString)).
                build();
        sendRequestAsync(request, callback);
    }

    public static void refreshToken(String username, String password, Callback callback) {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("username", username);
        jsonObj.addProperty("password", password);
        String jsonString = jsonObj.toString();
        Request request = new Request.Builder().url(baseUrl + "fetch_huami_token/").
                addHeader("Authorization", base64PW).
                addHeader("Content-Type", "application/json").
                post(GAServerUtil.getRequestBodyFromJsonString(jsonString)).
                build();
        sendRequestAsync(request, callback);
    }

}
   