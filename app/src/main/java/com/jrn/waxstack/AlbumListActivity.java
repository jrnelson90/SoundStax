package com.jrn.waxstack;

import android.support.v4.app.Fragment;

/**
 * Created by jrnel on 2/18/2017.
 */

public class AlbumListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new AlbumListFragment();
    }
}
