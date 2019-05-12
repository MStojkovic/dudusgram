package com.e.dudusgram.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;

import com.e.dudusgram.R;
import com.e.dudusgram.Utils.BottomNavigationViewHelper;
import com.e.dudusgram.Utils.Permissions;
import com.e.dudusgram.Utils.SectionsPagerAdapter;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class ShareActivity extends AppCompatActivity {
    private static final String TAG = "ShareActivity";

    private static final int ACTIVITY_NUM = 2;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    private ViewPager mViewPager;

    private Context mContext = ShareActivity.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Log.d(TAG, "onCreate: started.");

        if(checkPermissionsArray(Permissions.PERMISSIONS)){

            setupViewPager();

        }else{
            verifyPermissions(Permissions.PERMISSIONS);
        }

        //setupBottomNavigationView();
    }

    /**
     * Returns the current tab number
     * 0 = GalleryFragment
     * 1 = PhotoFragment
     * @return
     */

    public int getCurrentTabNumber(){

        return mViewPager.getCurrentItem();
    }

    /**
     * Setup viewpager to manage the tabs
     */

    private void setupViewPager(){

        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new PhotoFragment());

        mViewPager = findViewById(R.id.viewpager_container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText(R.string.gallery);
        tabLayout.getTabAt(1).setText(R.string.photo);
    }

    public int getTask(){

        Log.d(TAG, "getTask: TASK: " + getIntent().getFlags());
        return getIntent().getFlags();
    }

    /**
     * Verify all the passed permissions
     * @param permissions
     */

    public void verifyPermissions(String[] permissions){

        Log.d(TAG, "verifyPermissions: verifying permissions.");

        ActivityCompat.requestPermissions(
                ShareActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    /**
     * Check if an array of permission have been granted
     * @param permissions
     * @return
     */

    public boolean checkPermissionsArray(String [] permissions){

        Log.d(TAG, "checkPermissionsArray: checking permissions array");

        for (int i = 0; i < permissions.length; i++){

            String check = permissions[i];
            if (!checkPermissions(check)){
                return false;
            }
        }

        return true;
    }

    /**
     * Check if a single permission has been granted
     * @param permission
     * @return
     */

    public boolean checkPermissions (String permission) {

        Log.d(TAG, "checkPermissions: checking permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED){

            Log.d(TAG, "checkPermissions: \n Permission was not granted for: " + permission);
            return false;

        } else {

            Log.d(TAG, "checkPermissions: \n Permission was granted for: " + permission);
            return true;

        }
    }
    

    /**
     * BottomNavigationView setup
     */

    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
