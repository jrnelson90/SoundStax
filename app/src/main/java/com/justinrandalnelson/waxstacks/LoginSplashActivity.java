package com.justinrandalnelson.waxstacks;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * Login Page for WaxStacks
 * Created by jrnel on 4/14/2017.
 */

public class LoginSplashActivity extends Activity {
    static final int LOGIN_REQUEST = 40;
    private RequestQueue queue;
    private BroadcastReceiver broadcast_reciever;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(getApplicationContext());
        Preferences.setPreferenceContext(PreferenceManager.getDefaultSharedPreferences(this));
        if (Preferences.get(Preferences.OAUTH_ACCESS_KEY, "").length() > 0) {
            Intent i = new Intent(this, DashboardActivity.class);
            startActivity(i);
            finish();
        } else {
            setContentView(R.layout.activity_login_splash);
            Button signInButton = (Button) findViewById(R.id.sign_in_button);
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectivityManager connectivityManager = (ConnectivityManager)
                            getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (connectivityManager.getActiveNetworkInfo() != null
                            && connectivityManager.getActiveNetworkInfo().isAvailable()
                            && connectivityManager.getActiveNetworkInfo().isConnected()) {
                        FetchRequestToken();
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        broadcast_reciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals("finish_activity")) {
                    finish();
                }
            }
        };
        registerReceiver(broadcast_reciever, new IntentFilter("finish_activity"));
    }

    private void FetchRequestToken() {
        // GET https://api.discogs.com/oauth/request_token
        StringRequest stringRequest = new StringRequest
                (Request.Method.GET, HttpConst.REQUEST_TOKEN_ENDPOINT_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // OnResponse
                        String[] tokenArray = response.split("&");
                        if (tokenArray.length == 3 && tokenArray[0] != null) {
                            OauthVerifyTokens.setOauthRequestTokenSecret(tokenArray[0].split("=")[1]);
                            OauthVerifyTokens.setOauthRequestToken(tokenArray[1].split("=")[1]);

                            String authUrl = null;
                            if (OauthVerifyTokens.getOauthRequestToken() != null) {
                                authUrl = HttpConst.AUTHORIZATION_WEBSITE_URL + "?oauth_token=" +
                                        OauthVerifyTokens.getOauthRequestToken();
                                Log.i("Auth URL", authUrl);
                            } else {
                                Log.i("Auth Dialog", "No oauth request token values populated");
                            }

                            if (authUrl != null) {
                                Uri authUri = Uri.parse(authUrl);
                                Intent i = AuthPageActivity.newIntent(getApplicationContext(), authUri);
                                startActivity(i);
                            }
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        // Read POST response code
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                Long tsLong = System.currentTimeMillis() / 1000;
                String ts = tsLong.toString();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Authorization", "OAuth" +
                        "  oauth_consumer_key=" + HttpConst.CONSUMER_KEY +
                        ", oauth_nonce=" + ts +
                        ", oauth_signature=" + HttpConst.CONSUMER_SECRET + "&" +
                        ", oauth_signature_method=PLAINTEXT" +
                        ", oauth_timestamp=" + ts +
                        ", oauth_callback=" + HttpConst.CALLBACK_URL);
                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        queue.add(stringRequest);
    }
}
