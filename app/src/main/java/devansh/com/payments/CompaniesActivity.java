package devansh.com.payments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by devansh on 9/6/15.
 */
public class CompaniesActivity extends Activity {

    public static class Company {
        public long id;
        public String name;

        public Company(JSONObject jsonObject) throws JSONException {
            this.id = jsonObject.getLong("id");
            this.name = jsonObject.getString("name");
        }
    }

    private RequestQueue mRequestQueue;
    private JsonArrayRequest mCurrentRequest;
    private ListView mListView;
    private CompaniesAdapter mAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_companies);

        mRequestQueue = Volley.newRequestQueue(this);
        mListView = (ListView) findViewById(R.id.companies);
        mAdapter = new CompaniesAdapter();
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Company c = mAdapter.getItem(position);
                Intent i = new Intent(CompaniesActivity.this, RemindersActivity.class);
                i.putExtra(RemindersActivity.COMPANY_ID, c.id);
                i.putExtra(RemindersActivity.COMPANY_NAME, c.name);
                startActivity(i);
            }
        });

        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchCompanies();
            }
        });

        fetchCompanies();
    }


    private void fetchCompanies() {
        if (mCurrentRequest != null) {
            mCurrentRequest.cancel();
            mCurrentRequest = null;
        }

        mCurrentRequest =
                new JsonArrayRequest(Constants.COMPANIES, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        mCurrentRequest = null;
                        if (response == null || response.length() == 0) {
                            return;
                        }
                        int size = response.length();
                        ArrayList<Company> res = new ArrayList<>();
                        for (int i = 0; i < size; i++) {
                            try {
                                res.add(new Company(response.getJSONObject(i)));
                            } catch (JSONException e) {
                            }
                        }

                        mAdapter.updateCompanies(res);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mCurrentRequest = null;
                        Toast.makeText(CompaniesActivity.this, error.getMessage(),
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

    private class CompaniesAdapter extends BaseAdapter {

        private ArrayList<Company> mCompanies = new ArrayList<>();

        public void updateCompanies(List<Company> companies) {
            mCompanies.clear();
            mCompanies.addAll(companies);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mCompanies.size();
        }

        @Override
        public Company getItem(int position) {
            return mCompanies.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.companies_item, parent, false);
            }

            ((TextView) convertView).setText(mCompanies.get(position).name);

            return convertView;
        }
    }
}
