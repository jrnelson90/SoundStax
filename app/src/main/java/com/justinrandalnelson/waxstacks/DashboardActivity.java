package com.justinrandalnelson.waxstacks;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;

import java.io.File;

/**
 * Dashboard Activity
 * Created by jrnel on 4/14/2017.
 */

public class DashboardActivity extends SingleFragmentActivity {
    private DrawerLayout mDrawerLayout;
    private NavigationView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    @Override
    protected Fragment createFragment() {
        return new DashboardFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (NavigationView) findViewById(R.id.navView);
        mDrawerList.setNavigationItemSelectedListener(new DrawerItemClickListener());


        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectItem(MenuItem menuItem) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        Class fragmentClass;
        switch (menuItem.getItemId()) {
            case R.id.dashboard_fragment_nav:
                fragmentClass = DashboardFragment.class;
                break;
            case R.id.collection_fragment_nav:
                fragmentClass = CollectionListviewFragment.class;
                break;
            case R.id.wantlist_fragment_nav:
                fragmentClass = WantlistListviewFragment.class;
                break;
            case R.id.lists_fragment_nav:
                fragmentClass = CollectionListviewFragment.class;
                break;
            case R.id.profile_fragment_nav:
                fragmentClass = CollectionListviewFragment.class;
                break;
            case R.id.logout_nav:
                clearAllUserInfo();
                fragmentClass = DashboardFragment.class;
                break;
            default:
                fragmentClass = DashboardFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void clearAllUserInfo() {
        Preferences.set(Preferences.OAUTH_ACCESS_KEY, "");
        Preferences.set(Preferences.OAUTH_ACCESS_SECRET, "");
        Preferences.set(Preferences.USERNAME, "");
        Preferences.set(Preferences.USER_ID, "");
        Preferences.set(Preferences.USER_PROFILE, "");

        File collectionImageDir =
                new File("/data/user/0/com.justinrandalnelson.waxstacks/app_CollectionCovers");
        if (collectionImageDir.isDirectory()) {
            String[] children = collectionImageDir.list();
            for (int i = 0; i < children.length; i++) {
                new File(collectionImageDir, children[i]).delete();
            }
        }
        File wantlistImageDir =
                new File("/data/user/0/com.justinrandalnelson.waxstacks/app_WantlistCovers");
        if (wantlistImageDir.isDirectory()) {
            String[] children = wantlistImageDir.list();
            for (int i = 0; i < children.length; i++) {
                new File(wantlistImageDir, children[i]).delete();
            }
        }
        UserWantlistDB.get(getApplicationContext()).deleteAllReleases();
        UserCollectionDB.get(getApplicationContext()).deleteAllReleases();
    }

    @Override
    public void setTitle(CharSequence title) {

        if (!title.equals("Log out")) {
            mTitle = title;
        } else {
            mTitle = "Wax Stacks";
        }
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            selectItem(menuItem);
            return true;
        }
    }
}
