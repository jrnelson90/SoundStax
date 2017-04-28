package com.justinrandalnelson.waxstacks;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.UUID;

public class ReleaseActivity extends SingleFragmentActivity {
    private static final String EXTRA_RELEASE_ID =
            "com.justinrandalnelson.waxstacks.release_id";
    private static final String EXTRA_RELEASE =
            "com.justinrandalnelson.waxstacks.release";
    private static String parentList;

    public static Intent newIntent(Context packageContext, UUID releaseID, String _parentList) {
        Intent intent = new Intent(packageContext, ReleaseActivity.class);
        intent.putExtra(EXTRA_RELEASE_ID, releaseID);
        parentList = _parentList;
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        UUID releaseID = (UUID) getIntent()
                .getSerializableExtra(EXTRA_RELEASE_ID);
        return ReleaseFragment.newInstance(releaseID, parentList);
    }
}
