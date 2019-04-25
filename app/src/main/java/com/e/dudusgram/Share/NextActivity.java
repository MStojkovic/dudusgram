package com.e.dudusgram.Share;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.e.dudusgram.Login.LoginActivity;
import com.e.dudusgram.R;
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

    private String mAppend = "file:/";
    private int imageCount = 0;
    private String imgURL;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    private EditText mCaption;

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

        TextView share = findViewById(R.id.tvShare);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               //upload the image to firebase
                Log.d(TAG, "onClick: Attempting to upload new photo.");

                String caption = mCaption.getText().toString();

                Toast.makeText(NextActivity.this, "Attempting to upload new photo", Toast.LENGTH_SHORT).show();
                mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, imgURL);
            }
        });

        setImage();

    }

    private void someMethod(){

        //create a data model for Photos

        //Add properties to the Photo Objects (caption, date, imageURL, photo_id, tags, user_id)

        //Count the number of photos the user already has

        //Upload the Photo to FireBase Storage and insert two new nodes in the FireBase Database:
            //Photo node
            //user_photo node
    }

    /**
     * Gets the image URL from the incoming intent and displays the chosen image
     */

    private void setImage(){

        Intent intent = getIntent();
        ImageView image = findViewById(R.id.imageShare);
        imgURL = intent.getStringExtra(getString(R.string.selected_image));

        UniversalImageLoader.setImage(intent.getStringExtra(getString(R.string.selected_image)), image, null, mAppend);
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
        Log.d(TAG, "setupFirebaseAuth: image count: " + imageCount);

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

                imageCount = mFirebaseMethods.getImageCount(dataSnapshot);
                Log.d(TAG, "onDataChange: image count: " + imageCount);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
