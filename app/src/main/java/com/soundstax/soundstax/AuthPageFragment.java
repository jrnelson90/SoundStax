package com.soundstax.soundstax;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment for Discogs Login WebView for User Authentication
 * Created by jrnel on 4/14/2017.
 */

public class AuthPageFragment extends VisibleFragment {
    private static final String ARG_URI = "auth_page_url";
    private Uri mUri;
    private ProgressBar mProgressBar;
    private RequestQueue queue;

    public static AuthPageFragment newInstance(Uri uri) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);

        AuthPageFragment fragment = new AuthPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUri = getArguments().getParcelable(ARG_URI);
        queue = VolleyRequestQueue.getInstance(getActivity().getApplicationContext()).getRequestQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_auth_page, container, false);

        WebView webView = (WebView) v.findViewById(R.id.fragment_auth_page_web_view);
        webView.getSettings().setJavaScriptEnabled(true);

        mProgressBar = (ProgressBar) v.findViewById(R.id.auth_webview_progress_bar);
        mProgressBar.setMax(100);

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView webView, int newProgress) {
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
            }

            public void onReceivedTitle(WebView webView, String title) {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                try {
                    activity.getSupportActionBar().setSubtitle(title);
                } catch (NullPointerException npe) {
                    npe.printStackTrace();
                    Log.e("No Subtitle Found", "Actionbar missing subtitle object");
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(HttpConst.CALLBACK_URL)) {
                    Uri OauthResult = Uri.parse(url);
                    String verifyString = OauthResult.toString().split("oauth_verifier=")[1];
                    OauthVerifyTokens.setOauthUserVerifier(verifyString);
                    Log.i("Oauth Success", "Verifier received");

                    FetchOauthAccessToken(new String[]{
                            OauthVerifyTokens.getOauthRequestToken(),
                            OauthVerifyTokens.getOauthUserVerifier()});
                } else {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.clearCache(true);
            }
        });

        // Load Auth Page
        webView.loadUrl(mUri.toString());
        return v;
    }

    private void openLoadingActivity() {
        Intent i = new Intent(getActivity(), LoadingSplashActivity.class);
        startActivity(i);
        Intent responseIntent = new Intent();

        // Throw in some identifier
//        i.putExtra(EXTRA_CAR_ID, car.getId());

        // Set the result with this data, and finish the activity
        getActivity().setResult(Activity.RESULT_OK, responseIntent);

        getActivity().finish();
    }

    private void FetchOauthAccessToken(final String[] _passedOauth) {
        StringRequest stringRequest = new StringRequest
                (Request.Method.GET, HttpConst.DISCOGS_ACCESS_TOKEN_ENDPOINT_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String[] tokenArray = response.split("&");
                        // OnResponse
                        if (tokenArray.length == 2) {
                            String parsedAccessKey = tokenArray[1].split("=")[1].replace("\n", "");
                            String parsedAccessSecret = tokenArray[0].split("=")[1];
                            Preferences.set(Preferences.OAUTH_ACCESS_KEY, parsedAccessKey);
                            Preferences.set(Preferences.OAUTH_ACCESS_SECRET, parsedAccessSecret);
                            CookieManager cookieManager = CookieManager.getInstance();
                            cookieManager.removeAllCookies(null);
                            openLoadingActivity();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                })
                // Request Headers
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                Long tsLong = System.currentTimeMillis() / 1000;
                String ts = tsLong.toString();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Authorization", "OAuth" +
                        "  oauth_consumer_key=" + HttpConst.DISCOGS_CONSUMER_KEY +
                        ", oauth_nonce=" + ts +
                        ", oauth_token=" + _passedOauth[0] +
                        ", oauth_signature=" + HttpConst.DISCOGS_CONSUMER_SECRET + "&" +
                        OauthVerifyTokens.getOauthRequestTokenSecret() +
                        ", oauth_signature_method=PLAINTEXT" +
                        ", oauth_timestamp=" + ts +
                        ", oauth_verifier=" + _passedOauth[1]);
                params.put("User-Agent", HttpConst.USER_AGENT);
                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        queue.add(stringRequest);
    }
}
