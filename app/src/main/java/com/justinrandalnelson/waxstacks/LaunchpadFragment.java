package com.justinrandalnelson.waxstacks;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by jrnel on 4/14/2017.
 */

public class LaunchpadFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
//        Preferences pref = new PreferenceManager()
//                (PreferenceManager.getDefaultSharedPreferences(getContext()));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        boolean oauthVerified = false;
        View view;
        if (!oauthVerified) {
            view = inflater.inflate(R.layout.fragment_login_splash, container, false);

            Button signInButton = (Button) view.findViewById(R.id.sign_in_button);
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectivityManager connectivityManager = (ConnectivityManager)
                            getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (connectivityManager.getActiveNetworkInfo() != null
                            && connectivityManager.getActiveNetworkInfo().isAvailable()
                            && connectivityManager.getActiveNetworkInfo().isConnected()) {
                        new FetchRequestToken().execute();
                    }
                }
            });
        } else {
            view = inflater.inflate(R.layout.fragment_launchpad, container, false);
            Button collectionButton = (Button) view.findViewById(R.id.collection_button);
            collectionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    ConnectivityManager connectivityManager = (ConnectivityManager)
//                            getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//                    if (connectivityManager.getActiveNetworkInfo() != null
//                            && connectivityManager.getActiveNetworkInfo().isAvailable()
//                            && connectivityManager.getActiveNetworkInfo().isConnected()) {
//                        new FetchRequestToken().execute();
//                    }
                }
            });
            Button wantlistButton = (Button) view.findViewById(R.id.wantlist_button);
            wantlistButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    ConnectivityManager connectivityManager = (ConnectivityManager)
//                            getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//                    if (connectivityManager.getActiveNetworkInfo() != null
//                            && connectivityManager.getActiveNetworkInfo().isAvailable()
//                            && connectivityManager.getActiveNetworkInfo().isConnected()) {
//                        new FetchRequestToken().execute();
//                    }
                }
            });
            Button userInfoButton = (Button) view.findViewById(R.id.user_button);
            userInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    ConnectivityManager connectivityManager = (ConnectivityManager)
//                            getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//                    if (connectivityManager.getActiveNetworkInfo() != null
//                            && connectivityManager.getActiveNetworkInfo().isAvailable()
//                            && connectivityManager.getActiveNetworkInfo().isConnected()) {
//                        new FetchRequestToken().execute();
//                    }
                }
            });
        }
        return view;
    }

    private class FetchRequestToken extends AsyncTask<Void, Void, String[]> {
        @Override
        protected String[] doInBackground(Void... params) {
            return new OauthTokenFetcher().fetchRequestToken();
        }

        @Override
        protected void onPostExecute(String[] tokenArray) {
            if (tokenArray.length == 3 && tokenArray[0] != null) {
                OauthTokens.setOauthRequestTokenSecret(tokenArray[0].split("=")[1]);
                OauthTokens.setOauthRequestToken(tokenArray[1].split("=")[1]);
                OauthTokens.setOauthCallbackConfirmed(tokenArray[2].split("=")[1]);

                String authUrl = null;
                if (OauthTokens.getOauthRequestToken() != null) {
                    authUrl = HttpConst.AUTHORIZATION_WEBSITE_URL + "?oauth_token=" +
                            OauthTokens.getOauthRequestToken();
                    Log.i("Auth URL", authUrl);
                } else {
                    Log.i("Auth Dialog", "No oauth request token values populated");
                }

                if (authUrl != null) {
                    Uri authUri = Uri.parse(authUrl);
                    Intent i = AuthPageActivity.newIntent(getActivity(), authUri);
                    startActivity(i);
                }
            }
        }
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
                OauthTokens.setOauthAccessTokenSecret(tokenArray[0].split("=")[1]);
                OauthTokens.setOauthAccessToken(tokenArray[1]
                        .split("=")[1].replace("\n", ""));
//                navigateBackToList();
            }
        }
    }
}
