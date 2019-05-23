package com.e.dudusgram.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.e.dudusgram.R;
import com.e.dudusgram.Utils.CustomText;
import com.e.dudusgram.Utils.FirebaseMethods;
import com.e.dudusgram.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";
    
    private String imgURL;
    private Bitmap bitmap;
    private Intent intent;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    private CustomText mCaption;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        mFirebaseMethods = new FirebaseMethods(NextActivity.this);

        mCaption = findViewById(R.id.caption);

        setupFirebaseAuth();

        ImageView backArrow = findViewById(R.id.ivBackArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: closing the activity.");
                finish();
            }
        });

        final TextView share = findViewById(R.id.tvShare);
        share.setClickable(true);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                share.setClickable(false);

               //upload the image to firebase
                Log.d(TAG, "onClick: Attempting to upload new photo.");

                String caption = mCaption.getText().toString();

                if(intent.hasExtra(getString(R.string.selected_image))){

                    imgURL = intent.getStringExtra(getString(R.string.selected_image));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imgURL, null);

                }else if(intent.hasExtra(getString(R.string.selected_bitmap))){

                    bitmap = intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, null, bitmap);
                }

            }
        });

        setImage();

    }

    /**
     * Gets the image URL from the incoming intent and displays the chosen image
     */

    private void setImage(){

        intent = getIntent();
        ImageView image = findViewById(R.id.imageShare);

        if(intent.hasExtra(getString(R.string.selected_image))){

            imgURL = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "setImage: got new image url " + imgURL);
            String mAppend = "file:/";
            UniversalImageLoader.setImage(intent.getStringExtra(getString(R.string.selected_image)), image, null, mAppend);

        }else if(intent.hasExtra(getString(R.string.selected_bitmap))){

            bitmap = intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Log.d(TAG, "setImage: got new bitmap");
            image.setImageBitmap(bitmap);

        }


    }

    //<-----------------------------------Firebase---------------------------------------------->
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

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

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: data changed.");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
