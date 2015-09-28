package devansh.com.payments;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

/**
 * Created by devansh on 9/27/15.
 */
public class UserManager {

    private static final String SHARED_PREFS_NAME = "user_manager";

    private static final String TOKEN = "token";
    private static final String EXPIRES = "expires";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String EMAIL = "email";

    public static final UserManager sInstance = new UserManager();

    public static final UserManager getInstance() {
        return sInstance;
    }

    private UserManager() {
        load();
    }

    public static class User {
        public long id;
        public String name;
        public String email;

        public User(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }

            id = jsonObject.optLong(ID);
            name = jsonObject.optString(NAME);
            email = jsonObject.optString(EMAIL);
        }

        public User() {
        }
    }

    public static class AuthResult {
        public String authToken;
        public long expires;
        public User user;

        public AuthResult() {
        }

        public AuthResult(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }

            JSONObject authTokenObj = jsonObject.optJSONObject("auth_token");

            if (authTokenObj != null) {
                authToken = authTokenObj.optString(TOKEN);
                expires = authTokenObj.optLong(EXPIRES);
            }

            user = new User(jsonObject.optJSONObject("user"));
        }
    }

    private AuthResult authResult;

    public void setAuthResult(JSONObject jsonObject) {
        this.authResult = new AuthResult(jsonObject);
        save();
    }

    public String getAuthToken() {
        if (authResult == null || authResult.authToken == null) {
            return "";
        } else {
            return authResult.authToken;
        }
    }

    private SharedPreferences getSharedPrefs() {
        return PaymentsApplication.getInstance()
                .getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void save() {
        SharedPreferences.Editor editor = getSharedPrefs().edit();
        editor.putString(TOKEN, authResult.authToken);
        editor.putLong(EXPIRES, authResult.expires);
        editor.putLong(ID, authResult.user.id);
        editor.putString(NAME, authResult.user.name);
        editor.putString(EMAIL, authResult.user.email);
        editor.commit();
    }

    public void load() {
        SharedPreferences prefs = getSharedPrefs();
        if (prefs.getString(TOKEN, null) != null) {
            AuthResult authResult = new AuthResult();
            authResult.authToken = prefs.getString(TOKEN, null);
            authResult.expires = prefs.getLong(EXPIRES, 0);
            if (authResult.expires < System.currentTimeMillis() + Constants.DAY_IN_MILLIS) {
                return;
            }
            authResult.user = new User();
            authResult.user.id = prefs.getLong(ID, 0);
            authResult.user.name = prefs.getString(NAME, null);
            authResult.user.email = prefs.getString(EMAIL, null);

            this.authResult = authResult;
        }
    }

    public boolean isLoggedIn() {
        return authResult != null;
    }
}
