package nodomain.freeyourgadget.gadgetbridge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Optional;

import guardianangel.GuardianSessionManager;
import nodomain.freeyourgadget.gadgetbridge.activities.ControlCenterv2;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GuardianCreateAccountActivity extends AppCompatActivity {
    Activity activity = this;
    String username;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardian_create_account);

        EditText editTextUsername = findViewById(R.id.edit_text_username);
        EditText editTextPassword = findViewById(R.id.edit_text_password);
        Button buttonCreateUser = findViewById(R.id.button_create_user);

        buttonCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = editTextUsername.getText().toString();
                password = editTextPassword.getText().toString();
                GuardianSessionManager.createAccount(getApplicationContext(), username, password, createCallbackForCreateUser());
            }
        });

    }

    private void showLongToastMessageOnUiThread(String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private Callback createCallbackForCreateUser() {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                showLongToastMessageOnUiThread("Could not create account: "+call.toString()+"/"+ e.getMessage()+"/");
                //.Info("Could not create account: "+call.toString()+"/"+ e.getMessage()+"/");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (response.isSuccessful()) {
                        assert responseBody != null;
                        // It is valid to store only the username and not the id since the server forces us to have unique usernames
                        Prefs.putString(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit(), GuardianSessionManager.userNamePreferenceKey, username);
                        Prefs.putString(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit(),GuardianSessionManager.passwordPreferenceKey, password);
                        String responseString = responseBody.string();
                        JsonElement root = new JsonParser().parse(responseString);
                        String key = root.getAsJsonObject().get("key").toString();
                        int userId = Integer.parseInt(root.getAsJsonObject().get("user_id").toString());
                        // Replace the added " characters
                        key = key.substring(1, key.length() - 1);
                        Prefs.putString(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit(), GuardianSessionManager.authKeyPreferenceKey, key);
                        Prefs.putInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit(), GuardianSessionManager.userIdPreferenceKey, userId);
                        showLongToastMessageOnUiThread("User successfully created");
                        finish();
                    } else {
                        showLongToastMessageOnUiThread("Could not create user: "+call.toString()+"/==>/" +response.message() + "/" + response.toString()+"/");
                        //LOG.Info("Could not create user: "+call.toString()+"/==>/" +response.message() + "/" + response.toString()+"/");
                    }
                }
            }
        };
    }
}