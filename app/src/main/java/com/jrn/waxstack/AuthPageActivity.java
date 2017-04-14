package com.jrn.waxstack;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

/**
 * Created by jrnel on 4/14/2017.
 */

public class AuthPageActivity extends SingleFragmentActivity {
    public static Intent newIntent(Context context, Uri authPageUri) {
        Intent i = new Intent(context, AuthPageActivity.class);
        i.setData(authPageUri);
        return i;
    }

    @Override
    public Fragment createFragment() {
        return AuthPageFragment.newInstance(getIntent().getData());
    }
}
