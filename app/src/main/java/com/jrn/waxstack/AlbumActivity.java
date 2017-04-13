package com.jrn.waxstack;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.UUID;

public class AlbumActivity extends SingleFragmentActivity {

    private static final String EXTRA_ALBUM_ID =
            "com.jrn.vinylmanager.album_id";

    public static Intent newIntent(Context packageContext, UUID albumID) {
        Intent intent = new Intent(packageContext, AlbumActivity.class);
        intent.putExtra(EXTRA_ALBUM_ID, albumID);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        UUID albumID = (UUID) getIntent()
                .getSerializableExtra(EXTRA_ALBUM_ID);
        return AlbumFragment.newInstance(albumID);
    }
}
