package guardianangel;

import android.content.Context;

import androidx.preference.PreferenceManager;

import nodomain.freeyourgadget.gadgetbridge.util.Prefs;
import okhttp3.Callback;

public class GuardianSessionManager {

    public static String userNamePreferenceKey = "username";
    public static String passwordPreferenceKey = "password";
    public static String authKeyPreferenceKey = "user_authkey";
    public static String userIdPreferenceKey = "user_id";

    public static void tryLogin(Context context, Callback loginCallback) {
        Prefs prefs = getPrefsFromContext(context);
        String currentUsername = prefs.getString(userNamePreferenceKey, "");
        String password = prefs.getString(passwordPreferenceKey, "");
        if(!currentUsername.isEmpty()){
            GAServices.validateUser(currentUsername, password, loginCallback);
        }
    }

    public static void login(String username, String password, Callback callback) {
        GAServices.validateUser(username, password, callback);
    }

    public static void createAccount(Context context, String email, String password, Callback callback) {
        GAServices.createNewUser(email, password,callback);

    }

    public static Prefs getPrefsFromContext(Context context) {
        return new Prefs(PreferenceManager.getDefaultSharedPreferences(context));
    }


}
