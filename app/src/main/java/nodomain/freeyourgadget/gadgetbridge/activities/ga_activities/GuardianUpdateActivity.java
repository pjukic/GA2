package nodomain.freeyourgadget.gadgetbridge.activities.ga_activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import guardianangel.GAServices;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

public class GuardianUpdateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardian_update);
        Button updateButton = findViewById(R.id.button_update);
        final Prefs prefs = new Prefs(PreferenceManager.getDefaultSharedPreferences(this));
        final String tokenPrefKey = getString(R.string.guardian_angel_token_preference_key);
        final String shouldUpdateKey = getString(R.string.guardian_should_update_flag);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs.putString(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit(), shouldUpdateKey, "false");
                GAServices.installLatestApk(prefs.getPreferences().getString(tokenPrefKey, ""), GAServerSyncActivity.createCallbackForUpdating(getApplicationContext()));
                finish();
            }
        });
    }
}