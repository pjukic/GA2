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

import nodomain.freeyourgadget.gadgetbridge.activities.ga_activities.GAServerSyncActivity;

public class PeriodicGuardianUpdater extends BroadcastReceiver {
    private static final Logger LOG = LoggerFactory.getLogger(PeriodicGuardianUpdater.class);

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void enablePeriodicUpdate(Context context) {
        Integer autoExportInterval = 1; // 24 hours
        sheduleAlarm(context, autoExportInterval);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void sheduleAlarm(Context context, Integer autoExportInterval) {
        Intent i = new Intent(context, PeriodicGuardianUpdater.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);

        int updatePeriod = autoExportInterval * 24 * 60 * 60 * 1000;
        LOG.info("Enabling periodic guardian update");
        am.set(
                AlarmManager.RTC,
                System.currentTimeMillis() + updatePeriod,
                pi
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        LOG.info("handling guardian update event");
        GAServerSyncActivity.handleFlagGuardianForUpdateEvent(context);
        enablePeriodicUpdate(context);
    }
}
