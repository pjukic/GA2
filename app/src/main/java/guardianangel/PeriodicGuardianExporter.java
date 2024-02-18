package guardianangel;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceManager;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceService;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceService;
import nodomain.freeyourgadget.gadgetbridge.service.DeviceCommunicationService;
import nodomain.freeyourgadget.gadgetbridge.util.GBPrefs;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

import static nodomain.freeyourgadget.gadgetbridge.model.DeviceService.ACTION_FETCH_RECORDED_DATA;
import static nodomain.freeyourgadget.gadgetbridge.model.DeviceService.EXTRA_RECORDED_DATA_TYPES;

public class PeriodicGuardianExporter extends BroadcastReceiver {
    private static final Logger LOG = LoggerFactory.getLogger(PeriodicGuardianExporter.class);

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void enablePeriodicExport(Context context) {
        Prefs prefs = GBApplication.getPrefs();
        Integer autoExportInterval = prefs.getInt(GBPrefs.AUTO_GUARDIAN_EXPORT_INTERVAL, 1);


        sheduleAlarm(context,autoExportInterval , true);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void sheduleAlarm(Context context, Integer autoExportInterval, boolean autoExportEnabled) {
        Intent i = new Intent(context, PeriodicGuardianExporter.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0 , i, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        if (!autoExportEnabled) {
            return;
        }
        int exportPeriod = autoExportInterval *  60 * 60 * 1000;
        if (autoExportInterval == 0) {
            exportPeriod = 10000;
        }
        LOG.info("Enabling periodic guardian export");
        am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + exportPeriod,
                pi
        );
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        LOG.debug("Guardian export...");
        GBApplication app  = (GBApplication) context.getApplicationContext();
        DeviceManager deviceManager = app.getDeviceManager();
        List<GBDevice> devices = deviceManager.getDevices();
        if(devices != null && devices.size() == 1){
            GBDevice relevantDevice = devices.get(0);
            if(!(relevantDevice.isConnected() || relevantDevice.isInitialized())){
                DeviceService deviceService = new GBDeviceService(context.getApplicationContext());
                deviceService.connect(relevantDevice);
                PeriodicGuardianExporter.sheduleAlarm(context, 0,true);
            }
            else{
                Intent fetch_intent = new Intent(context, DeviceCommunicationService.class);
                fetch_intent.putExtra(EXTRA_RECORDED_DATA_TYPES, 1);
                fetch_intent.setAction(ACTION_FETCH_RECORDED_DATA);
                app.startService(fetch_intent);
            }
        }
        enablePeriodicExport(context);
        return;
    }
}
