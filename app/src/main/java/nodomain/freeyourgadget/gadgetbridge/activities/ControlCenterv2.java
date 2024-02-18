/*  Copyright (C) 2016-2020 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, Johannes Tysiak, Taavi Eomäe, vanous

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import de.cketti.library.changelog.ChangeLog;
import guardianangel.GAServices;
import guardianangel.GuardianSessionManager;
import guardianangel.GuardianStopWatch;
import guardianangel.MainFabButtonOnTouchListener;
import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.GuardianLoginActivity;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.ga_activities.GAServerSyncActivity;
import nodomain.freeyourgadget.gadgetbridge.adapter.GBDeviceAdapterv2;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceManager;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceService;
import nodomain.freeyourgadget.gadgetbridge.util.AndroidUtils;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

//TODO: extend AbstractGBActivity, but it requires actionbar that is not available
public class ControlCenterv2 extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GBActivity {

    public static final int MENU_REFRESH_CODE = 1;
    private static PhoneStateListener fakeStateListener;

    //needed for KK compatibility
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private DeviceManager deviceManager;
    private GBDeviceAdapterv2 mGBDeviceAdapter;
    private RecyclerView deviceListView;
    private FloatingActionButton mainFabButton;
    private FloatingActionButton recordFabButton;
    private GuardianSessionManager sessionManager;
    private boolean isLanguageInvalid = false;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (Objects.requireNonNull(action)) {
                case GBApplication.ACTION_LANGUAGE_CHANGE:
                    setLanguage(GBApplication.getLanguage(), true);
                    break;
                case GBApplication.ACTION_QUIT:
                    finish();
                    break;
                case DeviceManager.ACTION_DEVICES_CHANGED:
                    setFabListenerAndIcon();
                    refreshPairedDevices();
                    break;
                case DeviceService.ACTION_REALTIME_SAMPLES:
                    handleRealtimeSample(intent.getSerializableExtra(DeviceService.EXTRA_REALTIME_SAMPLE));
                    break;

            }
        }
    };
    private boolean pesterWithPermissions = true;
    private ActivitySample currentHRSample;

    public ActivitySample getCurrentHRSample() {
        return currentHRSample;
    }

    private void setCurrentHRSample(ActivitySample sample) {
        if (HeartRateUtils.getInstance().isValidHeartRateValue(sample.getHeartRate())) {
            currentHRSample = sample;
            refreshPairedDevices();
        }
    }

    private void handleRealtimeSample(Serializable extra) {
        if (extra instanceof ActivitySample) {
            ActivitySample sample = (ActivitySample) extra;
            setCurrentHRSample(sample);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AbstractGBActivity.init(this, AbstractGBActivity.NO_ACTIONBAR);

        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_controlcenterv2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.controlcenter_navigation_drawer_open, R.string.controlcenter_navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //end of material design boilerplate
        deviceManager = ((GBApplication) getApplication()).getDeviceManager();

        deviceListView = findViewById(R.id.deviceListView);
        deviceListView.setHasFixedSize(true);
        deviceListView.setLayoutManager(new LinearLayoutManager(this));

        List<GBDevice> deviceList = deviceManager.getDevices();
        mGBDeviceAdapter = new GBDeviceAdapterv2(this, deviceList);

        deviceListView.setAdapter(this.mGBDeviceAdapter);

        /*
        defina guardian specific stuff
         */
        TextView recordTextView = findViewById(R.id.textview_record);
        Animation recordTextViewAlphaAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animate_visibility);

        mainFabButton = findViewById(R.id.fab);
        mainFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchDiscoveryActivity();
            }
        });
        mainFabButton.show();
        recordFabButton = findViewById(R.id.record_fab);
        recordFabButton.setOnTouchListener(new MainFabButtonOnTouchListener(
                getApplicationContext(),
                recordFabButton,
                recordTextView,
                recordTextViewAlphaAnimation));
        recordFabButton.setRippleColor(Color.RED);
        recordFabButton.hide();


        registerForContextMenu(deviceListView);

        IntentFilter filterLocal = new IntentFilter();
        filterLocal.addAction(GBApplication.ACTION_LANGUAGE_CHANGE);
        filterLocal.addAction(GBApplication.ACTION_QUIT);
        filterLocal.addAction(DeviceManager.ACTION_DEVICES_CHANGED);
        filterLocal.addAction(DeviceService.ACTION_REALTIME_SAMPLES);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filterLocal);

        refreshPairedDevices();

        /*
         * Ask for permission to intercept notifications on first run.
                 * This is commented out for now, maybe this will break something Filip - 20.8.2021


        Prefs prefs = GBApplication.getPrefs();
        pesterWithPermissions = prefs.getBoolean("permission_pestering", true);

        Set<String> set = NotificationManagerCompat.getEnabledListenerPackages(this);
        if (pesterWithPermissions) {
            if (!set.contains(this.getPackageName())) { // If notification listener access hasn't been granted
                Intent enableIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(enableIntent);
            }
        }

         */


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }
        setFabListenerAndIcon();

        ChangeLog cl = createChangeLog();
        if (cl.isFirstRun()) {
            try {
               // cl.getLogDialog().show();
            } catch (Exception ignored) {
                GB.toast(getBaseContext(), "Error showing Changelog", Toast.LENGTH_SHORT, GB.ERROR);

            }
        }

        GBApplication.deviceService().start();

        if (GB.isBluetoothEnabled() && deviceList.isEmpty() && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startActivity(new Intent(this, DiscoveryActivity.class));
        } else {
            GBApplication.deviceService().requestDeviceInfo();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLanguageInvalid) {
            isLanguageInvalid = false;
            recreate();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterForContextMenu(deviceListView);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MENU_REFRESH_CODE) {
            setFabListenerAndIcon();
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, MENU_REFRESH_CODE);
                return true;
            /*case R.id.action_debug:
                Intent debugIntent = new Intent(this, DebugActivity.class);
                startActivity(debugIntent);
                return true;*/
            /*
            case R.id.action_data_management:
                Intent dbIntent = new Intent(this, DataManagementActivity.class);
                startActivity(dbIntent);
                return true;*/
            /*case R.id.action_blacklist:
                Intent blIntent = new Intent(this, AppBlacklistActivity.class);
                startActivity(blIntent);
                return true;*/
            case R.id.device_action_discover:
                launchDiscoveryActivity();
                return true;
            case R.id.action_quit:
                GBApplication.quit();
                return true;
            /*case R.id.donation_link:
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://liberapay.com/Gadgetbridge")); //TODO: centralize if ever used somewhere else
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return true;
            case R.id.about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;*/
            case R.id.external_changelog:
                ChangeLog cl = createChangeLog();
                try {
                    cl.getLogDialog().show();
                } catch (Exception ignored) {
                    GB.toast(getBaseContext(), "Error showing Changelog", Toast.LENGTH_SHORT, GB.ERROR);
                }
                return true;

            case R.id.synchronize_with_server:
                Intent serverSyncIntent = new Intent(this, GAServerSyncActivity.class);
                startActivity(serverSyncIntent);
                return true;
            case R.id.logout:
                Intent loginIntent = new Intent(this, GuardianLoginActivity.class);
                Prefs.removeEntry(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit(), GuardianSessionManager.userNamePreferenceKey);
                Prefs.removeEntry(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit(), GuardianSessionManager.passwordPreferenceKey);
                finish();
                startActivity(loginIntent);
                return true;
            case R.id.refresh_token:
                Prefs prefs = GuardianSessionManager.getPrefsFromContext(getApplicationContext());
                String username = prefs.getString(GuardianSessionManager.userNamePreferenceKey, "");
                String password = prefs.getString(GuardianSessionManager.passwordPreferenceKey, "");
                GAServices.refreshToken(username, password, createRefreshTokenCallback());
        }

        return true;
    }

    private ChangeLog createChangeLog() {
        String css = ChangeLog.DEFAULT_CSS;
        css += "body { "
                + "color: " + AndroidUtils.getTextColorHex(getBaseContext()) + "; "
                + "background-color: " + AndroidUtils.getBackgroundColorHex(getBaseContext()) + ";" +
                "}";
        return new ChangeLog(this, css);
    }

    private void launchDiscoveryActivity() {
        startActivity(new Intent(this, DiscoveryActivity.class));
    }

    private void refreshPairedDevices() {
        mGBDeviceAdapter.notifyDataSetChanged();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setFabListenerAndIcon() {
        if (deviceManager.getDevices().size() < 1) {
            mainFabButton.show();
            recordFabButton.hide();
        } else {
            mainFabButton.hide();
            recordFabButton.show();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermissions() {
        List<String> wantedPermissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.BLUETOOTH);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.SCHEDULE_EXACT_ALARM);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_PHONE_STATE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            wantedPermissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            wantedPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        wantedPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        wantedPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
        //PJ - remove
/*
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.RECEIVE_SMS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_SMS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.SEND_SMS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_CALENDAR);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_CONTACTS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.CALL_PHONE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_CALL_LOG);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (pesterWithPermissions) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_DENIED) {
                    wantedPermissions.add(Manifest.permission.ANSWER_PHONE_CALLS);
                }
            }
        }


        */

        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.MEDIA_CONTENT_CONTROL) == PackageManager.PERMISSION_DENIED)
                wantedPermissions.add(Manifest.permission.MEDIA_CONTENT_CONTROL);
        } catch (Exception ignored) {
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED) {
                wantedPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        }

        if (!wantedPermissions.isEmpty()) {
            Prefs prefs = GBApplication.getPrefs();
            // If this is not the first run, we can rely on
            // shouldShowRequestPermissionRationale(String permission)
            // and ignore permissions that shouldn't or can't be requested again
            if (prefs.getBoolean("permissions_asked", true)) {
                // Don't request permissions that we shouldn't show a prompt for
                // e.g. permissions that are "Never" granted by the user or never granted by the system
                Set<String> shouldNotAsk = new HashSet<>();
                for (String wantedPermission : wantedPermissions) {
                    if ( !shouldShowRequestPermissionRationale(wantedPermission)) {
                        shouldNotAsk.add(wantedPermission);
                    }
                }
                wantedPermissions.removeAll(shouldNotAsk);
            } else {
                // Permissions have not been asked yet, but now will be
                prefs.getPreferences().edit().putBoolean("permissions_asked", true).apply();
            }
            wantedPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            wantedPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
            if (!wantedPermissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, wantedPermissions.toArray(new String[0]), 0);
                GB.toast(this, "wanted: " + getString(R.string.permission_granting_mandatory), Toast.LENGTH_SHORT, GB.ERROR);

                //GB.toast(this, getString(R.string.permission_granting_mandatory), Toast.LENGTH_LONG, GB.ERROR);
            }
        }

        /* In order to be able to set ringer mode to silent in GB's PhoneCallReceiver
           the permission to access notifications is needed above Android M
           ACCESS_NOTIFICATION_POLICY is also needed in the manifest */

        if (pesterWithPermissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).isNotificationPolicyAccessGranted()) {
                    //GB.toast(this, "pester: " + getString(R.string.permission_granting_mandatory), Toast.LENGTH_LONG, GB.ERROR);
                    //PJ try to remove. Test OK!
                    startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
                }
            }
        }

        // HACK: On Lineage we have to do this so that the permission dialog pops up
        //PJ - remove
/*
        if (fakeStateListener == null) {
            fakeStateListener = new PhoneStateListener();
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            telephonyManager.listen(fakeStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            telephonyManager.listen(fakeStateListener, PhoneStateListener.LISTEN_NONE);
        }
*/

    }

    public void setLanguage(Locale language, boolean invalidateLanguage) {
        if (invalidateLanguage) {
            isLanguageInvalid = true;
        }
        AndroidUtils.setLanguage(this, language);
    }

    private Callback createRefreshTokenCallback() {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Could not fetch token", Toast.LENGTH_LONG).show();
                            }
                        });
                        throw new IOException("Unexpected code " + response);
                    } else {
                        assert responseBody != null;
                        String responseString = responseBody.string();
                        JsonElement root = new JsonParser().parse(responseString);
                        String key = root.getAsJsonObject().get("key").toString();
                        // Replace the added " characters
                        key = key.substring(1, key.length() - 1);
                        Prefs.putString(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit(), GuardianSessionManager.authKeyPreferenceKey, key);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Token fetched successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        };
    }
}
