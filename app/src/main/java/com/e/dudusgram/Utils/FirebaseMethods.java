package com.e.dudusgram.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.e.dudusgram.R;
import com.e.dudusgram.models.User;
import com.e.dudusgram.models.UserAccountSettings;
import com.e.dudusgram.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;
    private String userID;

    private Context mContext;
    private double mPhotoUploadProgress = 0;

    public FirebaseMethods(Context context){
        mContext = context;
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        if(mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public void uploadNewPhoto (String photoType, String caption, int count, String imgUrl){

        Log.d(TAG, "uploadNewPhoto: attempting to upload new photo.");

        FilePaths filePaths = new FilePaths();

        if (photoType.equals(mContext.getString(R.string.new_photo))){

            Log.d(TAG, "uploadNewPhoto: uploading new photo.");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count + 1));

            //convert image url to bitmap
            Bitmap bm = ImageManager.getBitmap(imgUrl);
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> firebaseUrl = mStorageReference.getDownloadUrl();

                    Toast.makeText(mContext, "Photo upload success", Toast.LENGTH_SHORT).show();

                    //add the new photo to 'photo' and 'user_photos' node

                    //navigate to the main feed
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Log.d(TAG, "onFailure: Photo upload failed.");
                    Toast.makeText(mContext, "Photo upload failed.", Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 10 > mPhotoUploadProgress){
                        Toast.makeText(mContext, String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });

        } else if (photoType.equals(mContext.getString(R.string.profile_photo))){

            Log.d(TAG, "uploadNewPhoto: uploading new profile photo");
        }
    }

    public int getImageCount (DataSnapshot dataSnapshot){

        int count = 0;

        for (DataSnapshot ds: dataSnapshot
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()){
            count++;
        }

        return count;
    }

    /**
     * Update 'user_account_settings' node for the current user
     * @param displayName
     * @param website
     * @param description
     * @param phoneNumber
     */

    public void updateUserAccountSettings(String displayName, String website, String description, long phoneNumber){

        Log.d(TAG, "updateUserAccountSettings: updating user account settings.");

        if (displayName != null) {

            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);

        }

        if (website != null) {

            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);

        }

        if (description != null) {

            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);

        }

        if (phoneNumber != 0) {

            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(userID)
                    .child(mContext.getString(R.string.field_phone_number))
                    .setValue(phoneNumber);

        }
    }

    public void updateUsername (String username){

        Log.d(TAG, "updateUsername: updating username to: " + username);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    public void updateEmail (String email){

        Log.d(TAG, "updateEmail: updating username to: " + email);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);

    }

    /*
    public boolean checkIfUsernameExists(String username, DataSnapshot datasnapshot){

        Log.d(TAG, "checkIfUsernameExists: checking if username already exsists.");
        Log.d(TAG, "checkIfUsernameExists: userID value: " + userID);

        User user = new User();

        for (DataSnapshot ds: datasnapshot.child(userID).getChildren()){
            Log.d(TAG, "checkIfUsernameExists: datasnapshot" + ds);

            user.setUsername(ds.getValue(User.class).getUsername());
            Log.d(TAG, "checkIfUsernameExists: username: " + user.getUsername());

            if(StringManipulation.expandUsername(user.getUsername()).equals(username)){
                Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH!" + user.getUsername());
                return true;
            }
        }
        return false;
    }
    */

    /**
     * Register a new email and password to firebase Authentication
     * @param email
     * @param password
     * @param username
     */

    public void registerNewEmail(final String email, String password, final String username){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            userID = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "createUserWithEmail:success" + userID);
                            //send verification email
                            sendVerificationEmail();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    public void sendVerificationEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                            }else {
                                Toast.makeText(mContext, "couldnt send verification email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * Add information to the users and users_account_settings node
     * @param email
     * @param username
     * @param description
     * @param webiste
     * @param profile_photo
     */

    public void addNewUser(String email, String username, String description, String webiste, String profile_photo){

        User user = new User(userID, 1, email, StringManipulation.condenseUsername(username));

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);

        UserAccountSettings settings = new UserAccountSettings(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                StringManipulation.condenseUsername(username),
                webiste
        );


        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings);
    }

    /**
     * Retrieves the account settings for the currently logged in user
     * @param dataSnapshot
     * @return
     */

    public UserSettings getUserSettings(DataSnapshot dataSnapshot) {

        Log.d(TAG, "getUserSettings: retrieveing user account setting from Firebase");

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        for (DataSnapshot ds: dataSnapshot.getChildren()) {

            //user_account_settings node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))) {
                Log.d(TAG, "getUserSettings: datasnapshot: " + ds);

                try {

                    settings.setDisplay_name(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name());

                    settings.setUsername(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername());

                    settings.setWebsite(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getWebsite());

                    settings.setDescription(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription());

                    settings.setProfile_photo(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo());

                    settings.setPosts(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getPosts());

                    settings.setFollowers(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowers());

                    settings.setFollowing(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowing());

                    Log.d(TAG, "getUserSettings: retrieved user_account_settings information" + settings.toString());

                } catch (NullPointerException e) {
                    Log.d(TAG, "getUserSettings: NullPointerException: " + e.getMessage());
                }
            }

            //users node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_users))){

                Log.d(TAG, "getUserSettings: datasnapshot: " + ds);

                try {

                    user.setUsername(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getUsername());

                    user.setEmail(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getEmail());

                    user.setPhone_number(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getPhone_number());

                    user.setUser_id(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getUser_id());

                    Log.d(TAG, "getUserSettings: retrieved users information" + user.toString());

                } catch (NullPointerException e) {
                    Log.d(TAG, "getUserSettings: NullPointerException: " + e.getMessage());
                }

            }
        }

        return new UserSettings(user, settings);

    }
}
