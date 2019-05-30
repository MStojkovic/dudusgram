package com.e.dudusgram.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.e.dudusgram.Home.HomeActivity;
import com.e.dudusgram.Profile.AccountSettingsActivity;
import com.e.dudusgram.R;
import com.e.dudusgram.models.Photo;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    private FirebaseAuth mAuth;
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

    public void uploadNewPhoto (String photoType, final String caption, final String imgUrl, Bitmap bm){

        Log.d(TAG, "uploadNewPhoto: attempting to upload new photo.");

        FilePaths filePaths = new FilePaths();

        //in case of a new photo
        if (photoType.equals(mContext.getString(R.string.new_photo))){

            Log.d(TAG, "uploadNewPhoto: uploading new photo.");

            final String dateCreated = getTimestamp();

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + dateCreated);

            //convert image url to bitmap
            if (bm == null){
                bm = ImageManager.getBitmap(imgUrl);
            }

            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            String firebaseUrl = uri.toString();
                            Log.d(TAG, "onSuccess: SUCCESS" + firebaseUrl);
                            //add the new photo to 'photo' and 'user_photos' node
                            addPhotoToDatabase(caption, firebaseUrl, dateCreated);

                        }
                    });

                    Toast.makeText(mContext, mContext.getString(R.string.photo_upload_success), Toast.LENGTH_SHORT).show();



                    //navigate to the main feed
                    Intent intent = new Intent(mContext, HomeActivity.class);
                    mContext.startActivity(intent);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Log.d(TAG, "onFailure: Photo upload failed.");
                    Toast.makeText(mContext, mContext.getString(R.string.error_text), Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 7 > mPhotoUploadProgress){
                        Toast.makeText(mContext, String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });

        }
        //in case of new profile photo
        else if (photoType.equals(mContext.getString(R.string.profile_photo))){

            Log.d(TAG, "uploadNewPhoto: uploading new profile photo");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

            //convert image url to bitmap
            if (bm == null){
                bm = ImageManager.getBitmap(imgUrl);
            }

            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            String firebaseUrl = uri.toString();
                            Log.d(TAG, "onSuccess: url: " + firebaseUrl);
                            //insert into 'user_account_settings'
                            setProfilePhoto(firebaseUrl);

                        }
                    });

                    Toast.makeText(mContext, mContext.getString(R.string.photo_upload_success), Toast.LENGTH_SHORT).show();

                    ((AccountSettingsActivity)mContext).setViewPager(
                            ((AccountSettingsActivity)mContext).pagerAdapter
                                    .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment))
                    );


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Log.d(TAG, "onFailure: Photo upload failed.");
                    Toast.makeText(mContext, mContext.getString(R.string.error_text), Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 7 > mPhotoUploadProgress){
                        Toast.makeText(mContext, String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });
        }
    }

    private void setProfilePhoto(String url){

        Log.d(TAG, "setProfilePhoto: setting a new profile photo: " + url);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);

    }

    private String getTimestamp(){

        SimpleDateFormat sdf = new SimpleDateFormat(mContext.getString(R.string.date_pattern), Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone(mContext.getString(R.string.timezone)));

        return sdf.format(new Date());
    }

    private void addPhotoToDatabase(String caption, String url, String dateCreated){

        Log.d(TAG, "addPhotoToDatabase: adding photo to database.");

        String tags = StringManipulation.getTags(caption);
        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(dateCreated);
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        myRef.child(mContext.getString(R.string.dbname_user_photos)).child(FirebaseAuth.getInstance().getCurrentUser()
                .getUid()).child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);

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


    /**
     * Register a new email and password to firebase Authentication
     * @param email
     * @param password
     */

    public void registerNewEmail(final String email, String password){
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
                            Toast.makeText(mContext, mContext.getString(R.string.authentication_fail),
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void sendVerificationEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(mContext, mContext.getString(R.string.verification_email_send_fail), Toast.LENGTH_SHORT).show();
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
     * @param website
     * @param profile_photo
     */

    public void addNewUser(String email, String username, String description, String website, String profile_photo){

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
                website,
                userID
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

        Log.d(TAG, "getUserSettings: retrieving user account settings from Firebase");

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        for (DataSnapshot ds: dataSnapshot.getChildren()) {

            //user_account_settings node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))) {
                Log.d(TAG, "getUserSettings: user account settings node datasnapshot: " + ds);

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

    public void deletePhoto (String photo_id, String date_created){

        FilePaths filePaths = new FilePaths();
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //delete from storage

        StorageReference storageReference = mStorageReference
                .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + date_created);

        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(mContext, mContext.getString(R.string.deleted_photo_success), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(mContext, mContext.getString(R.string.error_text), Toast.LENGTH_SHORT).show();
            }
        });

        //delete from photos node

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query deletePhoto = databaseReference
                .child(mContext.getString(R.string.dbname_photos))
                .orderByChild(mContext.getString(R.string.field_photo_id))
                .equalTo(photo_id);


        deletePhoto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                    singleSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: error", databaseError.toException() );
            }
        });

        //delete from user_photos node

        Query deleteUserPhoto = databaseReference
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(user_id)
                .orderByChild(mContext.getString(R.string.field_photo_id))
                .equalTo(photo_id);


        deleteUserPhoto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                    singleSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: error", databaseError.toException() );
            }
        });
    }

    public void deleteComment (String photoOwnerID, String photoID, String commentID){

        //delete from photos node

        Log.d(TAG, "Test2: " + mContext.getString(R.string.dbname_photos));
        Log.d(TAG, "Test2: " + photoID);
        Log.d(TAG, "Test2: " + mContext.getString(R.string.field_comments));
        Log.d(TAG, "Test2: " + commentID);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query deleteCommentPhoto = databaseReference
                .child(mContext.getString(R.string.dbname_photos))
                .child(photoID)
                .child(mContext.getString(R.string.field_comments))
                .equalTo(commentID);

        deleteCommentPhoto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                    singleSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: error", databaseError.toException() );
            }
        });

        //delete from user_photos node

        Query deleteCommentUserPhoto = databaseReference
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(photoOwnerID)
                .child(photoID)
                .child(mContext.getString(R.string.field_comments))
                .equalTo(commentID);

        deleteCommentUserPhoto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                    singleSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: error", databaseError.toException() );
            }
        });

    }
}
