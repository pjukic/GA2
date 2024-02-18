package nodomain.freeyourgadget.gadgetbridge.activities.ga_activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

import guardianangel.GAServices;
import guardianangel.GASynchedDeviceInfo;
import guardianangel.GuardianSessionManager;
import nodomain.freeyourgadget.gadgetbridge.BuildConfig;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.AbstractGBActivity;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

public class GAServerSyncActivity extends AbstractGBActivity {
    private static final Logger LOG = LoggerFactory.getLogger(GAServerSyncActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardian_server_sync);
        Context context = this;

        EditText editText = (EditText) findViewById(R.id.edit_text_guardian_device_name);
        TextView serverSyncHeadline = (TextView) findViewById(R.id.textview_server_sync_headline);
        Button syncButton = (Button) findViewById(R.id.button_sync_with_server);
        TextView deviceName = (TextView) findViewById(R.id.textview_device_name);
        TextView lastUpdated = (TextView) findViewById(R.id.textview_last_updated);
        TextView syncedOn = (TextView) findViewById(R.id.textview_synced_on);
        Button updateManuallyButton = (Button) findViewById(R.id.button_update_manually);
        Prefs prefs = new Prefs(PreferenceManager.getDefaultSharedPreferences(context));
        if (prefs.getPreferences().contains(getString(R.string.guardian_angel_token_preference_key))) {
            final String token = prefs.getPreferences().getString(getString(R.string.guardian_angel_token_preference_key), "");
            serverSyncHeadline.setText(R.string.text_device_synced);
            editText.setVisibility(View.INVISIBLE);
            editText.setHeight(0);
            syncButton.setText(R.string.button_text_remove_sync);
            GAServices.getInformationAboutSynchedDevice(token, createCallbackForGettingDeviceSyncInfo(deviceName, lastUpdated, syncedOn));

            syncButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GAServices.removeSync(token, createCallbackForRemovingSync(token));
                    Prefs.removeEntry(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit(),
                            getString(R.string.guardian_angel_token_preference_key));
                    recreate();
                }
            });
            updateManuallyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GAServices.installLatestApk(token, createCallbackForUpdating(getApplicationContext()));
                }
            });
        } else {
            final Callback callback = createCallbackForSyncingDevice();
            syncButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Prefs prefs = GuardianSessionManager.getPrefsFromContext(getApplicationContext());
                    int userId = prefs.getInt(GuardianSessionManager.userIdPreferenceKey, 0);
                    EditText editText = (EditText) findViewById(R.id.edit_text_guardian_device_name);
                    String deviceName = editText.getText().toString();
                    GAServices.synchronizeDevice(deviceName, userId, callback);
                    recreate();
                }
            });
        }
    }

    public static void handleGuardianUpdateEvent(Context context) {
        String shouldUpdateKey = context.getString(R.string.guardian_should_update_flag);
        Prefs prefs = new Prefs(PreferenceManager.getDefaultSharedPreferences(context));
        if (prefs.getPreferences().contains(shouldUpdateKey)) {
            if (prefs.getPreferences().getString(shouldUpdateKey, "").equals("true")) {
                Intent intent = new Intent(context, GuardianUpdateActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }

    }

    public static void handleFlagGuardianForUpdateEvent(Context context) {
        Prefs prefs = new Prefs(PreferenceManager.getDefaultSharedPreferences(context));
        String tokenPrefKey = context.getString(R.string.guardian_angel_token_preference_key);
        String version = "0.0.0";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (prefs.getPreferences().contains(tokenPrefKey)) {
            GAServices.flagForUpdate(prefs.getPreferences().getString(tokenPrefKey, ""), version, GAServerSyncActivity.createCallbacktoCheckWhetherDeviceShouldUpdate(context));
        } else {
            Toast.makeText(context.getApplicationContext(), "Need to synchronize device first", Toast.LENGTH_LONG).show();
        }
    }

    private static Callback createCallbacktoCheckWhetherDeviceShouldUpdate(final Context context) {
        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String shouldUpdatePrefKey = context.getString(R.string.guardian_should_update_flag);
                String shouldUpdate = response.header("should_update");
                if (shouldUpdate != null && shouldUpdate.equals("y")) {
                    LOG.info("guardian should update");
                    Prefs.putString(PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit(), shouldUpdatePrefKey, "true");
                } else if (shouldUpdate != null && shouldUpdate.equals("n")) {
                    LOG.info("guardian is up to date");
                    Prefs.putString(PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit(), shouldUpdatePrefKey, "false");
                }
                GAServerSyncActivity.handleGuardianUpdateEvent(context);
            }
        };
        return callback;
    }

    private Callback createCallbackForSyncingDevice() {
        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(getApplicationContext(), String.format("Error Code: %s Message: %s", response.code(), response.message()), Toast.LENGTH_LONG);
                                toast.show();
                                recreate();
                            }
                        });

                        throw new IOException("Unexpected code " + response);
                    }
                    assert responseBody != null;
                    final String responseString = responseBody.string();
                    Prefs.putString(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit(), getString(R.string.guardian_angel_token_preference_key), responseString);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(getApplicationContext(), "Synced successfully. Token: " + responseString, Toast.LENGTH_LONG);
                            toast.show();
                            recreate();
                        }
                    });

                }
            }
        };
        return callback;
    }

    private Callback createCallbackForGettingDeviceSyncInfo(final TextView deviceName, final TextView lastUpdated, final TextView syncedOn) {
        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deviceName.setText(String.format(getString(R.string.guardian_device_name), "not found - sync again"));
                        lastUpdated.setText(String.format(getString(R.string.guardian_last_updated), "not found - sync again"));
                        syncedOn.setText(String.format(getString(R.string.guardian_synced_on), "not found - sync again"));
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    assert responseBody != null;

                    if (!response.isSuccessful()) {
                        String responseString = responseBody.string();
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
                            }
                        });
                        throw new IOException("Unexpected code " + response);
                    }

                    String jsonStr = responseBody.string();
                    final GASynchedDeviceInfo syncedDeviceInfo = GASynchedDeviceInfo.fromJsonString(jsonStr);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            deviceName.setText(String.format(getString(R.string.guardian_device_name), syncedDeviceInfo.getDeviceName()));
                            lastUpdated.setText(String.format(getString(R.string.guardian_last_updated), Optional.ofNullable(syncedDeviceInfo.getLastUpdated()).map(Date::toString).orElse("never")));
                            syncedOn.setText(String.format(getString(R.string.guardian_synced_on), syncedDeviceInfo.getCreatedAt().toString()));
                        }
                    });

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };
        return callback;
    }

    private Callback createCallbackForRemovingSync(String token) {
        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    assert responseBody != null;

                    if (!response.isSuccessful()) {
                        String responseString = responseBody.string();
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
                            }
                        });
                        throw new IOException("Unexpected code " + response);
                    }

                    String responseString = responseBody.string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG);
                            toast.show();
                            startActivity(getIntent());
                            finish();
                        }
                    });
                }
            }
        };
        return callback;
    }

    public static Callback createCallbackForUpdating(Context context) {
        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    assert responseBody != null;

                    if (!response.isSuccessful()) {
                        String responseString = responseBody.string();
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(context, responseString, Toast.LENGTH_LONG).show();
                            }
                        });
                        throw new IOException("Unexpected code " + response);
                    }

                    File downloadedFile = new File(context.getApplicationContext().getExternalFilesDir(null), "update.apk");
                    BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
                    sink.writeAll(responseBody.source());
                    sink.close();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                    intent.setDataAndType(FileProvider.getUriForFile(context.getApplicationContext(),
                            BuildConfig.APPLICATION_ID + ".screenshot_provider",
                            downloadedFile), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    context.startActivity(intent);
                }
            }
        };
        return callback;
    }

}

