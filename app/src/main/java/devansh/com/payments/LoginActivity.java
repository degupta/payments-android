package devansh.com.payments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Map;

public class LoginActivity extends Activity {

    private RequestQueue mRequestQueue;
    private JsonObjectRequest mCurrentReq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (UserManager.getInstance().isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, CompaniesActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        final TextView email = (TextView) findViewById(R.id.email);
        final TextView password = (TextView) findViewById(R.id.password);

        mRequestQueue = Volley.newRequestQueue(this);

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    login(email.getText().toString(), password.getText().toString());
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void login(final String email, final String password) throws Exception {
        if (mCurrentReq != null) {
            mCurrentReq.cancel();
            mCurrentReq = null;
        }

        JSONObject obj = new JSONObject();
        obj.put("email", email);
        obj.put("password", password);

        mCurrentReq = new JsonObjectRequest(Constants.LOGIN, obj,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        mCurrentReq = null;
                        if (response == null) {
                            Toast.makeText(LoginActivity.this, "Internal error, please try again",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            UserManager.getInstance().setAuthResult(response);
                            startActivity(new Intent(LoginActivity.this, CompaniesActivity.class));
                            finish();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                mCurrentReq = null;
                Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return Utils.getHeaders();
            }

        };

        mRequestQueue.add(mCurrentReq);
    }
}