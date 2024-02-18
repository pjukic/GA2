package nodomain.freeyourgadget.gadgetbridge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;

import guardianangel.GuardianSessionManager;
import nodomain.freeyourgadget.gadgetbridge.activities.ControlCenterv2;
import nodomain.freeyourgadget.gadgetbridge.activities.ga_activities.GuardianUpdateActivity;
import nodomain.freeyourgadget.gadgetbridge.util.GBPrefs;
import nodomain.freeyourgadget.gadgetbridge.util.LimitedQueue;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GuardianLoginActivity extends AppCompatActivity {
    Activity activity = this;
    String textUserName;
    String textPassword;

    private static SharedPreferences sharedPrefs;
    //private static Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Before we do anything we try to log the user in with existng user data.
        // If it works we send him over to controlActivity
        GuardianSessionManager.tryLogin(getApplicationContext(), createValidateUserCallback());

        setContentView(R.layout.activity_guardian_login);

        EditText editTextUserName = (EditText) findViewById(R.id.edit_text_username);
        EditText editTextPassword = (EditText) findViewById(R.id.edit_text_password);

        Button buttonLogin = (Button) findViewById(R.id.button_login);
        TextView textViewCreateAccount = (TextView) findViewById(R.id.text_view_create_account);

        sharedPrefs = android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        editTextPassword.setText(sharedPrefs.getString(GuardianSessionManager.passwordPreferenceKey, ""));
        editTextUserName.setText(sharedPrefs.getString(GuardianSessionManager.userNamePreferenceKey, ""));

        textUserName = editTextUserName.getText().toString();
        textPassword = editTextPassword.getText().toString();
        if(!(textUserName.isEmpty()) && !(textPassword.isEmpty())){
            GuardianSessionManager.login(textUserName, textPassword, createValidateUserCallback());
        } else {
            //only if textUserName and textPassword are not saved, show dialog
            buttonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textUserName = editTextUserName.getText().toString();
                    textPassword = editTextPassword.getText().toString();
                    /*
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                    editor.putString(GuardianSessionManager.passwordPreferenceKey, textPassword);
                    editor.putString(GuardianSessionManager.userNamePreferenceKey, textUserName);
                    editor.commit();
                    */
                    GuardianSessionManager.login(textUserName, textPassword, createValidateUserCallback());
                }
            });

            textViewCreateAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, GuardianCreateAccountActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void showLongToastMessageOnUiThread(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }



    private Callback createValidateUserCallback() {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful() || responseBody == null) {
                        showLongToastMessageOnUiThread("Login not successful" + response.toString());
                        throw new IOException("Unexpected code " + response);
                    } else {
                        String responseString = responseBody.string();
                        JsonElement root = new JsonParser().parse(responseString);
                        int userId = Integer.parseInt(root.getAsJsonObject().get("user_id").toString());
                        if(textUserName != null && textPassword != null) {
                            if (!(textUserName.isEmpty()) && !(textPassword.isEmpty())) {
                                Prefs.putString(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit(), GuardianSessionManager.passwordPreferenceKey, textPassword);
                                Prefs.putString(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit(), GuardianSessionManager.userNamePreferenceKey, textUserName);
                                Prefs.putInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit(), GuardianSessionManager.userIdPreferenceKey, userId);

                            }
                        }
                        activity.finish();
                        Intent intent = new Intent(activity, ControlCenterv2.class);
                        intent.addFlags(PendingIntent.FLAG_IMMUTABLE);
                        startActivity(intent);
                    }
                }
            }
        };
    }
}
