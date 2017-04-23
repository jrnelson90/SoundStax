package com.justinrandalnelson.waxstacks;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * User Info Fragment
 * Created by jrnel on 4/23/2017.
 */

public class UserInfoFragment extends Fragment {
    private static final String TAG = "UserInfoFragment";
    private JSONObject mUserProfileJSON = new JSONObject();
    private RequestQueue queue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
//        setHasOptionsMenu(true);
        try {
            mUserProfileJSON = new JSONObject(Preferences.get(Preferences.USER_PROFILE, ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        queue = Volley.newRequestQueue(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);
        return view;
    }
}
