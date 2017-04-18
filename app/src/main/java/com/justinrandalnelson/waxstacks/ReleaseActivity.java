package com.justinrandalnelson.waxstacks;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.UUID;

public class ReleaseActivity extends SingleFragmentActivity {

    private static final String EXTRA_RELEASE_ID =
            "com.justinrandalnelson.waxstacks.release_id";

    public static Intent newIntent(Context packageContext, UUID releaseID) {
        Intent intent = new Intent(packageContext, ReleaseActivity.class);
        intent.putExtra(EXTRA_RELEASE_ID, releaseID);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        UUID releaseID = (UUID) getIntent()
                .getSerializableExtra(EXTRA_RELEASE_ID);
        return ReleaseFragment.newInstance(releaseID);
    }
}
