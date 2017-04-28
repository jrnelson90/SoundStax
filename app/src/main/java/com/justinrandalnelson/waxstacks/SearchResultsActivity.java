package com.justinrandalnelson.waxstacks;

import android.support.v4.app.Fragment;

/**
 * Activity for displaying search results
 * Created by jrnel on 4/26/2017.
 */

public class SearchResultsActivity extends SingleFragmentActivity {
    @Override
    public Fragment createFragment() {
        return new SearchResultsFragment();
    }
}
