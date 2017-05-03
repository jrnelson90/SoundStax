package com.soundstax.soundstax;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Login Page for SoundStax
 * Created by jrnel on 4/14/2017.
 */

public class LoginSplashActivity extends Activity {
    private static final int REQUEST_CODE_LOGIN_SUCCESS = 101;
    private RequestQueue queue;
    private BroadcastReceiver broadcast_reciever;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = VolleyRequestQueue.getInstance(getApplicationContext()).getRequestQueue();
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
    }

    private void FetchRequestToken() {
        // GET https://api.discogs.com/oauth/request_token
        StringRequest stringRequest = new StringRequest
                (Request.Method.GET, HttpConst.DISCOGS_REQUEST_TOKEN_ENDPOINT_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // OnResponse
                        String[] tokenArray = response.split("&");
                        if (tokenArray.length == 3 && tokenArray[0] != null) {
                            OauthVerifyTokens.setOauthRequestTokenSecret(tokenArray[0].split("=")[1]);
                            OauthVerifyTokens.setOauthRequestToken(tokenArray[1].split("=")[1]);

                            String authUrl = null;
                            if (OauthVerifyTokens.getOauthRequestToken() != null) {
                                authUrl = HttpConst.DISCOGS_AUTHORIZATION_WEBSITE_URL + "?oauth_token=" +
                                        OauthVerifyTokens.getOauthRequestToken();
                            } else {
                                Log.i("Auth Dialog", "No oauth request token values populated");
                            }

                            if (authUrl != null) {
                                Uri authUri = Uri.parse(authUrl);
                                Intent i = AuthPageActivity.newIntent(getApplicationContext(), authUri);
                                startActivityForResult(i, REQUEST_CODE_LOGIN_SUCCESS);
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
                        "  oauth_consumer_key=" + HttpConst.DISCOGS_CONSUMER_KEY +
                        ", oauth_nonce=" + ts +
                        ", oauth_signature=" + HttpConst.DISCOGS_CONSUMER_SECRET + "&" +
                        ", oauth_signature_method=PLAINTEXT" +
                        ", oauth_timestamp=" + ts +
                        ", oauth_callback=" + HttpConst.CALLBACK_URL);
                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        queue.add(stringRequest);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_CODE_LOGIN_SUCCESS == requestCode) {
            // If the activity confirmed a succesful login
            if (Activity.RESULT_OK == resultCode) {
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
