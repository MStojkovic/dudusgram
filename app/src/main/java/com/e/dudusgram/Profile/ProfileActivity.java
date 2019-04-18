package com.e.dudusgram.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.e.dudusgram.Login.LoginActivity;
import com.e.dudusgram.R;
import com.e.dudusgram.Utils.BottomNavigationViewHelper;
import com.e.dudusgram.Utils.GridImageAdapter;
import com.e.dudusgram.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;

    private Context mContext = ProfileActivity.this;

    private ProgressBar mProgressBar;
    private ImageView profilePhoto;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: started.");

        init();

        /*
        mProgressBar = findViewById(R.id.profileProgressBar);
        mProgressBar.setVisibility(View.GONE);


        setupFirebaseAuth();

        setupBottomNavigationView();
        setupToolBar();
        setupActivityWidget();
        setProfileImage();

        tempGridSetup();
        */
    }

    private void init(){
        Log.d(TAG, "init: inflating." + getString(R.string.profile_fragment));

        ProfileFragment fragment = new ProfileFragment();
        FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.profile_fragment));
        transaction.commit();
    }
    /*
    private void tempGridSetup(){
        ArrayList<String> imgURLs = new ArrayList<>();
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=6COT3TOpW0YSEM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=8OMT8aPmmhjb7M:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=bqTJL6ymCq4AgM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=qeURZMg52aWoHM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=1xQbcdcH6trwuM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=YEf-yiDuXoPh4M:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=wMIB21i8Ir66GM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=4_MAtHmXz09UnM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=dU-R6qPQC7RNwM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=eXfmBMoYJXdxHM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=8MGimE6KBIAZUM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=bK16nXzxq9fAFM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=rHniKhLfU-oOZM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=rHniKhLfU-oOZM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=rHniKhLfU-oOZM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=rHniKhLfU-oOZM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=rHniKhLfU-oOZM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=rHniKhLfU-oOZM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=rHniKhLfU-oOZM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=rHniKhLfU-oOZM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=rHniKhLfU-oOZM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=rHniKhLfU-oOZM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=rHniKhLfU-oOZM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=rHniKhLfU-oOZM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=rHniKhLfU-oOZM:");
        imgURLs.add("https://www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiDje66uMPhAhWto4sKHe8fCWoQ_AUIDigB&biw=958&bih=927#imgrc=rHniKhLfU-oOZM:");


        setupImageGrid(imgURLs);
    }

    private void setupImageGrid(ArrayList<String> imgURLs){
        GridView gridView = findViewById(R.id.gridView);

        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        GridImageAdapter adapter = new GridImageAdapter(mContext, R.layout.layout_grid_imageview, "",imgURLs);
        gridView.setAdapter(adapter);
    }

    private void setProfileImage(){
        Log.d(TAG, "setProfileImage: setting profile photo.");
        String imgURL = "www.google.com/search?q=conrad+von+h%C3%B6tzendorf&source=lnms&tbm=isch&sa=X&ved=0ahUKEwje-qbTo8HhAhUHVhoKHTv5C8wQ_AUIDigB&biw=958&bih=927#imgrc=YEf-yiDuXoPh4M:";
        UniversalImageLoader.setImage(imgURL, profilePhoto, mProgressBar, "https://");

    }

    private void setupActivityWidget(){
        mProgressBar = findViewById(R.id.profileProgressBar);
        mProgressBar.setVisibility(View.GONE);
        profilePhoto = findViewById(R.id.profile_photo);
    }

    /**
     * Responsible for setting up the profile toolbar
     */
    /*
    private void setupToolBar(){
        Toolbar toolbar = findViewById(R.id.profileToolBar);
        setSupportActionBar(toolbar);

        ImageView profileMenu = findViewById(R.id.profileMenu);
        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Navigating to account settings.");
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * BottomNavigationView setup
     */
    /*
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    //<-----------------------------------Firebase---------------------------------------------->
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Checks if the @param "user" is logged in.
     * @param user
     */
    /*
    private void checkCurrentUser(FirebaseUser user){
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if (user == null){
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }

    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in
                checkCurrentUser(user);

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    } */
}
