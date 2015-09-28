package devansh.com.payments;

/**
 * Created by devansh on 9/6/15.
 */
public class Constants {

    public static final String HOST = "http://ec2-52-88-94-73.us-west-2.compute.amazonaws.com";
    public static final String LOGIN = HOST + "/login";
    public static final String COMPANIES = HOST + "/companies";
    public static final String PENDING_MESSAGES = "/pending_reminders";
    public static final String REMINDERS = "/reminders";
    public static final String SENT_MESSAGE = "/sent_message";

    public static final long DAY_IN_MILLIS = (24 * 60 * 60 * 1000);
}
