package com.justinrandalnelson.waxstacks;

import android.support.v4.app.Fragment;

/**
 * Created by jrnel on 4/14/2017.
 */

public class LaunchpadActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new LaunchpadFragment();
    }
}
