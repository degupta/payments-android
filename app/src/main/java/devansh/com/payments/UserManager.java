package devansh.com.payments;

import org.json.JSONObject;

/**
 * Created by devansh on 9/27/15.
 */
public class UserManager {

    public static final UserManager sInstance = new UserManager();

    public static final UserManager getInstance() {
        return sInstance;
    }

    public static class User {
        public long id;
        public String name;
        public String email;

        public User(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }

            id = jsonObject.optLong("id");
            name = jsonObject.optString("name");
            email = jsonObject.optString("email");
        }
    }

    public static class AuthResult {
        public String authToken;
        public long expires;
        public User user;

        public AuthResult(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }

            JSONObject authTokenObj = jsonObject.optJSONObject("auth_token");

            if (authTokenObj != null) {
                authToken = jsonObject.optString("token");
                expires = jsonObject.optLong("expires");
            }

            user = new User(jsonObject.optJSONObject("user"));
        }
    }

    private AuthResult authResult;

    public void setAuthResult(JSONObject jsonObject) {
        this.authResult = new AuthResult(jsonObject);
    }

    public String getAuthToken() {
        if (authResult == null || authResult.authToken == null) {
            return "";
        } else {
            return authResult.authToken;
        }
    }
}
