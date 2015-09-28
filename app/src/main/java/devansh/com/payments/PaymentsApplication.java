package devansh.com.payments;

import android.app.Application;

/**
 * Created by devansh on 9/27/15.
 */
public class PaymentsApplication extends Application {

    private static PaymentsApplication sInstance;

    public void onCreate() {
        super.onCreate();
        sInstance = this;

        // Load
        UserManager.getInstance();
    }

    public static PaymentsApplication getInstance() {
        return sInstance;
    }
}
