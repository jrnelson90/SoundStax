package com.justinrandalnelson.waxstacks;

import android.support.v4.app.Fragment;

/**
 * Created by jrnel on 2/18/2017.
 */

public class ReleaseListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new CollectionListviewFragment();
    }
}
