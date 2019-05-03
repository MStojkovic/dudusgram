package com.e.dudusgram.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.e.dudusgram.Home.HomeActivity;
import com.e.dudusgram.Likes.LikesActivity;
import com.e.dudusgram.Profile.ProfileActivity;
import com.e.dudusgram.R;
import com.e.dudusgram.Search.SearchActivity;
import com.e.dudusgram.Share.ShareActivity;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class BottomNavigationViewHelper {

    private static final String TAG = "BottomNavigationViewHel";

    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx){
        Log.d(TAG, "setupBottomNavigationView: Setting up BottomNavigationView");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }

    public static void enableNavigation(final Context context, final Activity callingActivity, BottomNavigationViewEx view){
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){

                    case R.id.ic_house:
                        if (!(callingActivity.toString().split("@")[0].equals(HomeActivity.class.toString().split(" ")[1]))) {
                            Log.d(TAG, "onNavigationItemSelected: test");
                            Intent intent1 = new Intent(context, HomeActivity.class); //ACTIVITY_NUM = 0
                            context.startActivity(intent1);
                            callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                        break;

                    case R.id.ic_search:
                        if (!(callingActivity.toString().split("@")[0].equals(SearchActivity.class.toString().split(" ")[1]))) {
                            Intent intent2 = new Intent(context, SearchActivity.class); //ACTIVITY_NUM = 1
                            context.startActivity(intent2);
                            callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                        break;

                    case R.id.ic_circle:
                        if (!(callingActivity.toString().split("@")[0].equals(ShareActivity.class.toString().split(" ")[1]))) {
                            Intent intent3 = new Intent(context, ShareActivity.class); //ACTIVITY_NUM = 2
                            context.startActivity(intent3);
                            callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                        break;

                    case R.id.ic_alert:
                        if (!(callingActivity.toString().split("@")[0].equals(LikesActivity.class.toString().split(" ")[1]))) {
                            Intent intent4 = new Intent(context, LikesActivity.class); //ACTIVITY_NUM = 3
                            context.startActivity(intent4);
                            callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                        break;

                    case R.id.ic_android:
                        if (!(callingActivity.toString().split("@")[0].equals(ProfileActivity.class.toString().split(" ")[1]))) {
                            Intent intent5 = new Intent(context, ProfileActivity.class); //ACTIVITY_NUM = 4
                            context.startActivity(intent5);
                            callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                        break;

                }

                return false;
            }
        });
    }
}
