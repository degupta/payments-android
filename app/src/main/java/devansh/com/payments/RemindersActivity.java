package devansh.com.payments;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by devansh on 9/27/15.
 */
public class RemindersActivity extends Activity {

    public static final String COMPANY_ID = "company_id";
    public static final String COMPANY_NAME = "company_name";

    private RequestQueue mRequestQueue;
    private JsonArrayRequest mCurrentRequest;
    private long mCompanyId;
    private String mCompanyName;
    private ListView mListView;
    private RemindersAdapter mAdapter;
    private Reminder mCurrentReminder = null;
    private boolean mSentParty = false;

    public class Reminder {
        public long id;
        public String party;
        public String partyNumber;
        public String broker;
        public String brokerNumber;
        public String billDate;
        public String billNo;
        public String amount;
        public String dueDate;
        public int repeat;
        public String lastMessage;

        public boolean messageSent;
        public boolean messageError;

        public Reminder(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }

            id = jsonObject.optLong("id");
            party = jsonObject.optString("party");
            partyNumber = jsonObject.optString("party_number");
            broker = jsonObject.optString("broker");
            brokerNumber = jsonObject.optString("broker_number");
            billDate = jsonObject.optString("bill_date");
            billNo = jsonObject.optString("bill_no");
            amount = jsonObject.optString("amount");
            dueDate = jsonObject.optString("due_date");
            repeat = jsonObject.optInt("repeat");
            lastMessage = jsonObject.optString("last_message");
        }


        public String toPartySmsString() {
            return "This is reminder from " + mCompanyName + ". Bill no: " + billNo +
                    " for amount: " + amount + " is due on: " + dueDate;
        }

        public String toBrokerSmsString() {
            return "This is reminder from " + mCompanyName + ". Bill no: " + billNo +
                    " for amount: " + amount + " for party: " + party + " is due on: " + dueDate;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() == null || !getIntent().hasExtra(COMPANY_ID) ||
                !getIntent().hasExtra(COMPANY_NAME)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_reminders);

        mRequestQueue = Volley.newRequestQueue(this);
        mListView = (ListView) findViewById(R.id.reminders);
        mAdapter = new RemindersAdapter();
        mListView.setAdapter(mAdapter);

        mCompanyId = getIntent().getLongExtra(COMPANY_ID, 0);
        if (mCompanyId == 0) {
            finish();
            return;
        }
        mCompanyName = getIntent().getStringExtra(COMPANY_NAME);

        ((TextView) findViewById(R.id.company_name)).setText(mCompanyName);

        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchReminders();
            }
        });

        findViewById(R.id.sendMessages).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAndSendMessages(false);
            }
        });
        fetchReminders();

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        if (mSentParty) {
                            onMessageSent();
                        } else {
                            mSentParty = true;
                            sendBroker();
                        }
                        break;

                    default:
                        resetAndSendMessages(true);
                        break;
                }
            }
        }, new IntentFilter("SMS_DELIVERED"));
    }

    private void resetAndSendMessages(boolean error) {
        if (error && mCurrentReminder != null) {
            mCurrentReminder.messageError = true;
        }
        mCurrentReminder = null;
        mSentParty = false;
        sendMessage(mAdapter.getNextReminder());
        mAdapter.notifyDataSetChanged();
    }

    private void sendMessage(Reminder r) {
        if (r == null || mCurrentReminder != null) {
            return;
        }

        mCurrentReminder = r;
        mSentParty = false;

        PendingIntent deliveredPI =
                PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> parts = smsManager.divideMessage(r.toPartySmsString());
        smsManager.sendMultipartTextMessage(r.partyNumber, null, parts, null,
                new ArrayList<>(Arrays.asList(deliveredPI)));
    }

    private void sendBroker() {
        if (mCurrentReminder == null) {
            return;
        }

        PendingIntent deliveredPI =
                PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> parts = smsManager.divideMessage(mCurrentReminder.toBrokerSmsString());
        smsManager.sendMultipartTextMessage(mCurrentReminder.brokerNumber, null, parts, null,
                new ArrayList<>(Arrays.asList(deliveredPI)));
    }

    private void onMessageSent() {
        if (mCurrentReminder == null) {
            resetAndSendMessages(false);
            return;
        }

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        String date = day + "-" + month + "-" + year;

        JSONObject object = new JSONObject();
        try {
            object.put("date", date);
        } catch (JSONException e) {
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,
                Constants.COMPANIES + "/" + mCompanyId + Constants.REMINDERS + "/" +
                        mCurrentReminder.id + Constants.SENT_MESSAGE, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mCurrentReminder.messageSent = true;
                        resetAndSendMessages(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RemindersActivity.this, error.getMessage(), Toast.LENGTH_SHORT)
                        .show();
                resetAndSendMessages(true);
            }
        });

        mRequestQueue.add(req);
    }

    private void fetchReminders() {
        if (mCurrentRequest != null) {
            mCurrentRequest.cancel();
            mCurrentRequest = null;
        }

        mCurrentRequest =
                new JsonArrayRequest(
                        Constants.COMPANIES + "/" + mCompanyId + Constants.PENDING_MESSAGES,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                mCurrentRequest = null;
                                if (response == null || response.length() == 0) {
                                    return;
                                }
                                int size = response.length();
                                ArrayList<Reminder> res = new ArrayList<>();
                                for (int i = 0; i < size; i++) {
                                    try {
                                        res.add(new Reminder(response.getJSONObject(i)));
                                    } catch (JSONException e) {
                                    }
                                }

                                mAdapter.updateCompanies(res);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mCurrentRequest = null;
                        Toast.makeText(RemindersActivity.this, error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return Utils.getHeaders();
                    }

                };

        mRequestQueue.add(mCurrentRequest);
    }


    private class RemindersAdapter extends BaseAdapter {

        private ArrayList<Reminder> mReminders = new ArrayList<>();

        public void updateCompanies(List<Reminder> reminders) {
            mReminders.clear();
            mReminders.addAll(reminders);
            notifyDataSetChanged();
        }

        public Reminder getNextReminder() {
            for (Reminder r : mReminders) {
                if (!r.messageSent && !r.messageError) {
                    return r;
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return mReminders.size();
        }

        @Override
        public Reminder getItem(int position) {
            return mReminders.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.reminders_item, parent, false);
            }

            Reminder r = getItem(position);
            ((TextView) convertView.findViewById(R.id.party)).setText(r.party);
            ((TextView) convertView.findViewById(R.id.broker)).setText(r.broker);
            ((TextView) convertView.findViewById(R.id.amount)).setText(r.amount);
            ((TextView) convertView.findViewById(R.id.dueDate)).setText(r.dueDate);

            TextView status = ((TextView) convertView.findViewById(R.id.status));
            if (r.messageError) {
                status.setText(R.string.sent);
                status.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            } else if (r.messageSent) {
                status.setText(R.string.sent);
                status.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            } else {
                status.setText(R.string.not_sent);
                status.setTextColor(getResources().getColor(android.R.color.holo_orange_light));

            }
            return convertView;
        }
    }
}
