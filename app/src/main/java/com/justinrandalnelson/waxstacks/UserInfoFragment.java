package com.justinrandalnelson.waxstacks;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

        ImageView profilePicImageView = (ImageView) view.findViewById(R.id.user_pic_info_fragment);
        profilePicImageView.setImageBitmap(
                BitmapFactory.decodeFile(Preferences.get(Preferences.USER_PIC_DIR, "")));

        // Basic user info labels

        TextView usernameLabel = (TextView) view.findViewById(R.id.user_name_infoFragment_label);
        try {
            usernameLabel.setText(mUserProfileJSON.getString("username"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView inCollectionLabel = (TextView) view.findViewById(R.id.user_collection_count_label);
        try {
            inCollectionLabel.append(" " + mUserProfileJSON.getString("num_collection"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView inWantlistLabel = (TextView) view.findViewById(R.id.user_wantlist_count_label);
        try {
            inWantlistLabel.append(" " + mUserProfileJSON.getString("num_wantlist"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView contributedLabel = (TextView) view.findViewById(R.id.user_contributed_count_label);
        try {
            contributedLabel.append(" " + mUserProfileJSON.getString("releases_contributed"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView rankingLabel = (TextView) view.findViewById(R.id.user_rank_label);
        try {
            rankingLabel.append(" " + mUserProfileJSON.getString("rank"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Releases Labels

        TextView pendingNumLabel = (TextView) view.findViewById(R.id.pending_releases_group_label);
        try {
            pendingNumLabel.append(" " + mUserProfileJSON.getString("num_pending"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView ratedReleasesLabel = (TextView) view.findViewById(R.id.rated_releases_group_label);
        try {
            ratedReleasesLabel.append(" " + mUserProfileJSON.getString("releases_rated"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView ratingAverageReleasesLabel = (TextView) view.findViewById(R.id.rating_average_releases_group_label);
        try {
            ratingAverageReleasesLabel.append(" " + mUserProfileJSON.getString("rating_avg"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Marketplace Labels

        TextView sellerRatingLabel = (TextView) view.findViewById(R.id.seller_rating_marketplace_group_label);
        try {
            sellerRatingLabel.append(" " + mUserProfileJSON.getString("seller_rating"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView sellerRatingsNumLabel = (TextView) view.findViewById(R.id.seller_ratings_num_marketplace_group_label);
        try {
            sellerRatingsNumLabel.append(" " + mUserProfileJSON.getString("seller_num_ratings"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView buyerRatingLabel = (TextView) view.findViewById(R.id.buyer_rating_marketplace_group_label);
        try {
            buyerRatingLabel.append(" " + mUserProfileJSON.getString("buyer_rating"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView buyerRatingNumLabel = (TextView) view.findViewById(R.id.buyer_ratings_num_marketplace_group_label);
        try {
            buyerRatingNumLabel.append(" " + mUserProfileJSON.getString("buyer_num_ratings"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return view;
    }
}
