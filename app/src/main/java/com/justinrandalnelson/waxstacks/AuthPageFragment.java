package com.justinrandalnelson.waxstacks;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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

/**
 * Created by jrnel on 4/14/2017.
 */

public class AuthPageFragment extends VisibleFragment {
    private static final String ARG_URI = "auth_page_url";

    private Uri mUri;
    private WebView mWebView;
    private ProgressBar mProgressBar;


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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_auth_page, container, false);

        mWebView = (WebView) v.findViewById(R.id.fragment_auth_page_web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);

        mProgressBar = (ProgressBar) v.findViewById(R.id.auth_webview_progress_bar);
        mProgressBar.setMax(100);

        mWebView.setWebChromeClient(new WebChromeClient() {
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
                } catch (Exception e) {

                }
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(HttpConst.CALLBACK_URL)) {
                    Uri OauthResult = Uri.parse(url);
                    String verifyString = OauthResult.toString().split("oauth_verifier=")[1];
                    OauthTokens.setOauthUserVerifier(verifyString);
                    Log.i("Oauth Success", "Verifier received");

                    new FetchOauthAccessToken().execute(new String[]{
                            OauthTokens.getOauthRequestToken(),
                            OauthTokens.getOauthUserVerifier()
                    });
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
        mWebView.loadUrl(mUri.toString());

        return v;
    }

    private void navigateBackToList() {
        Intent upIntent = NavUtils.getParentActivityIntent(getActivity());
        NavUtils.navigateUpTo(getActivity(), upIntent);
    }

    private class FetchOauthAccessToken extends AsyncTask<String[], Void, String[]> {
        @Override
        protected String[] doInBackground(String[]... params) {
            String[] _passedOauth = params[0];
            return new OauthTokenFetcher().fetchOauthAccessToken(_passedOauth);
        }

        @Override
        protected void onPostExecute(String[] tokenArray) {
            if (tokenArray.length == 2) {
                String parsedAccessKey = tokenArray[1].split("=")[1].replace("\n", "");
                String parsedAccessSecret = tokenArray[0].split("=")[1];
                Preferences.set(Preferences.OAUTH_ACCESS_KEY, parsedAccessKey);
                Preferences.set(Preferences.OAUTH_ACCESS_SECRET, parsedAccessSecret);
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookies(null);
                navigateBackToList();
            }
        }
    }
}
