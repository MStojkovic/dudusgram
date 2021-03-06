package com.e.dudusgram.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.e.dudusgram.R;
import com.e.dudusgram.Share.ShareActivity;
import com.e.dudusgram.Utils.CustomText;
import com.e.dudusgram.Utils.FirebaseMethods;
import com.e.dudusgram.Utils.UniversalImageLoader;
import com.e.dudusgram.dialogs.ConfirmPasswordDialog;
import com.e.dudusgram.models.User;
import com.e.dudusgram.models.UserAccountSettings;
import com.e.dudusgram.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;
import info.hoang8f.android.segmented.SegmentedGroup;

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListener{

    @Override
    public void onConfirmPassword(String password) {

        Log.d(TAG, "onConfirmPassword: got the password.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), password);

        ////////////////////// Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            Log.d(TAG, "User re-authenticated.");

                            ////////////////////Check to see if the email is already in use
                            mAuth.fetchSignInMethodsForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    
                                    if(task.isSuccessful()){

                                        try {

                                            if(task.getResult().getSignInMethods().size() == 1){

                                                Log.d(TAG, "onComplete: That email is already in use!");
                                                Toast.makeText(getActivity(), getString(R.string.email_already_in_use), Toast.LENGTH_SHORT).show();

                                            } else {

                                                //////////Email is available for use
                                                mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "User email address updated.");
                                                                    Toast.makeText(getActivity(), getString(R.string.email_updated), Toast.LENGTH_SHORT).show();
                                                                    mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                                }
                                                            }
                                                        });

                                            }

                                        } catch (NullPointerException e){

                                            Log.e(TAG, "onComplete: NullPointerException" + e.getMessage());

                                        }
                                        
                                    }
                                    
                                }
                            });

                        } else {

                            Log.d(TAG, "Re-authentication failed! ");

                        }

                    }
                });

    }

    private static final String TAG = "EditProfileFragment";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods mFirebaseMethods;

    //Widgets
    private CustomText mDisplayName, mUsername, mWebsite, mDescription, mEmail, mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;
    private Switch mNotifications;
    private SegmentedGroup mProfileType;
    private RadioButton mPublicProfile, mPrivateProfile;

    private UserSettings mUserSettings;
    private Boolean success = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);
        mProfilePhoto = view.findViewById(R.id.profile_photo);
        mDisplayName = view.findViewById(R.id.display_name);
        mUsername = view.findViewById(R.id.username);
        mWebsite = view.findViewById(R.id.website);
        mDescription = view.findViewById(R.id.description);
        mEmail = view.findViewById(R.id.email);
        mPhoneNumber = view.findViewById(R.id.phoneNumber);
        mChangeProfilePhoto = view.findViewById(R.id.changeProfilePhoto);
        mNotifications = view.findViewById(R.id.notificationsSwitch);
        mProfileType = view.findViewById(R.id.segmentedGroupProfileType);
        mPublicProfile = view.findViewById(R.id.buttonPublic);
        mPrivateProfile = view.findViewById(R.id.buttonPrivate);
        mFirebaseMethods = new FirebaseMethods(getActivity());

        //setProfileImage();
        setupFirebaseAuth();

        //back arrow for navigating back to "Profile Activity"
        ImageView backArrow = view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Navigating back to ProfileActivity");
                getActivity().finish();
            }
        });

        ImageView checkmark = view.findViewById(R.id.saveChanges);
        checkmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to save changes.");
                if (!saveProfileSettings()) {
                    Log.d(TAG, "onClick: Navigating back to ProfileActivity");
                    getActivity().finish();
                }

            }
        });

        return view;
    }

    /**
     * Retrieves the data contained in the widgets and submits it to the database
     * 1st checks if the username is unique
     */
    private boolean saveProfileSettings() {
        final String displayName = mDisplayName.getText().toString();
        final String username = mUsername.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final String phoneNumber =mPhoneNumber.getText().toString();
        String notifications;
        String profileType;
        Boolean hadErrors = false;

        if (mNotifications.isChecked()) {
            notifications = "ON";
        } else {
            notifications ="OFF";
        }

        if (mProfileType.getCheckedRadioButtonId() == R.id.buttonPublic) {
            profileType = "PUBLIC";
        } else {
            profileType = "PRIVATE";
        }

        //case1: the user made a change to their username
        if (!mUserSettings.getUser().getUsername().equals(username)) {

            if (username.length() < 5 ){
                Toast.makeText(getActivity(), getString(R.string.short_username), Toast.LENGTH_SHORT).show();
                hadErrors = true;
            } else {
                 if (checkIfUsernameExists(username)){
                     hadErrors = true;
                 }
            }
        }
        //case2: user changed their email
        if (!mUserSettings.getUser().getEmail().equals(email)) {

            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
                dialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));
                dialog.setTargetFragment(EditProfileFragment.this, 1);

            } else {
                Toast.makeText(getActivity(), getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
                hadErrors = true;
            }

        }

        if (!mUserSettings.getSettings().getDisplay_name().equals(displayName)){

            if (displayName.length() < 5 ){
                Toast.makeText(getActivity(), getString(R.string.short_displayName), Toast.LENGTH_SHORT).show();
                hadErrors = true;
            } else {
                //update the display name
                mFirebaseMethods.updateUserAccountSettings(displayName, null, null, null, null, null);
            }

        }

        if (!mUserSettings.getSettings().getWebsite().equals(website)){

            //update the website
            mFirebaseMethods.updateUserAccountSettings(null, website, null, null, null, null);

        }

        if (!mUserSettings.getSettings().getDescription().equals(description)){

            //update the description
            mFirebaseMethods.updateUserAccountSettings(null, null, description, null, null, null);

        }

        if (!mUserSettings.getUser().getPhone_number().equals(phoneNumber)){

            //update the phone number
            mFirebaseMethods.updateUserAccountSettings(null, null, null, phoneNumber, null, null);

        }

        if (!mUserSettings.getUser().getNotifications().equals(notifications)) {

            //update notifications preferences
            mFirebaseMethods.updateUserAccountSettings(null, null, null, null, notifications, null);
        }

        if (!mUserSettings.getUser().getProfile_type().equals(profileType)) {

            //update account type preferences
            mFirebaseMethods.updateUserAccountSettings(null, null, null, null, null, profileType);
        }

        return hadErrors;
    }

    /**
     * Check if @param already exists in the database
     * @param username
     */
    private boolean checkIfUsernameExists(final String username) {

        Log.d(TAG, "checkIfUsernameExists: checking if username: " + username + " already exists.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()){
                    //add the username
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(), getString(R.string.username_saved), Toast.LENGTH_SHORT).show();
                    success = false;
                }
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    if (singleSnapshot.exists()){
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH!");
                        Toast.makeText(getActivity(), getString(R.string.username_exists), Toast.LENGTH_SHORT).show();
                        success = true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return success;

    }

    private void setProfileWidgets(UserSettings userSettings){

        Log.d(TAG, "setProfileWidgets: populating widgets with data retrieved from firebase");

        mUserSettings = userSettings;

        User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        Log.d(TAG, "setProfileWidgets: " + settings.getProfile_photo() + mProfilePhoto);

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(user.getEmail());
        mPhoneNumber.setText(user.getPhone_number());
        if (user.getNotifications().equals("ON")) {
            mNotifications.setChecked(true);
        } else {
            mNotifications.setChecked(false);
        }

        if (user.getProfile_type().equals("PUBLIC")) {
            mPublicProfile.setChecked(true);
            mPrivateProfile.setChecked(false);
        } else {
            mPublicProfile.setChecked(false);
            mPrivateProfile.setChecked(true);
        }

        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: changing profile photo.");

                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });
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
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference myRef = mFirebaseDatabase.getReference();

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
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

}
