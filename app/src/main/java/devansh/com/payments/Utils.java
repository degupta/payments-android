package devansh.com.payments;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by devansh on 9/27/15.
 */
public class Utils {

    public static Map<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        if (UserManager.getInstance().getAuthToken() != null) {
            headers.put("Authorization", "Token " + UserManager.getInstance().getAuthToken());
        }
        return headers;
    }
}
