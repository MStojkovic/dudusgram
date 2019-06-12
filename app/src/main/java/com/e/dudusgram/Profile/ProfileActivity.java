package com.e.dudusgram.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.e.dudusgram.R;
import com.e.dudusgram.Utils.ViewCommentsFragment;
import com.e.dudusgram.Utils.ViewPostFragment;
import com.e.dudusgram.Utils.ViewProfileFragment;
import com.e.dudusgram.models.Comment;
import com.e.dudusgram.models.Like;
import com.e.dudusgram.models.Photo;
import com.e.dudusgram.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements
        ProfileFragment.OnGridImageSelectedListener ,
        ViewPostFragment.OnCommentThreadSelectedListener,
        ViewProfileFragment.OnGridImageSelectedListener {

    private static final String TAG = "ProfileActivity";

    Photo photoNew = new Photo();

    @Override
    public void onCommentThreadSelectedListener(Photo photo) {
        Log.d(TAG, "onCommentThreadSelectedListener: selected a comment thread");

        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }

    @Override
    public void onGridImageSelected(Photo photo, int activityNumber) {
        Log.d(TAG, "onGridImageSelected: selected and image gridview: " + photo.toString());

        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putInt(getString(R.string.activity_number), activityNumber);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();

    }

    private Context mContext = ProfileActivity.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: started.");

        init();

    }

    private void init(){
        Log.d(TAG, "init: inflating." + getString(R.string.profile_fragment));

        Intent intent = getIntent();
        
        if(intent.hasExtra(getString(R.string.calling_activity))){
            Log.d(TAG, "init: searching for user object attached as intent extra");
            
            if(intent.hasExtra(getString(R.string.intent_user))){
                User user = intent.getParcelableExtra(getString(R.string.intent_user));

                if(!user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    Log.d(TAG, "init: inflating view profile");

                    ViewProfileFragment fragment = new ViewProfileFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(getString(R.string.intent_user),
                            intent.getParcelableExtra(getString(R.string.intent_user)));
                    fragment.setArguments(args);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.addToBackStack(getString(R.string.view_profile_fragment));
                    transaction.commit();
                }else{
                    Log.d(TAG, "init: inflating Profile");

                    ProfileFragment fragment = new ProfileFragment();
                    FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.addToBackStack(getString(R.string.profile_fragment));
                    transaction.commit();
                }

            }else if (intent.hasExtra(getString(R.string.photo_id_from_notification))) {

                String photoID = intent.getStringExtra(getString(R.string.photo_id_from_notification));

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                Query query = reference
                        .child(getString(R.string.dbname_photos))
                        .orderByChild(getString(R.string.field_photo_id))
                        .equalTo(photoID);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                            Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                            try{
                                photoNew.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                                photoNew.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                                photoNew.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                                photoNew.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                                photoNew.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                                photoNew.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                                ArrayList<Comment> comments = new ArrayList<Comment>();
                                for (DataSnapshot dSnapshot : singleSnapshot
                                        .child(getString(R.string.field_comments)).getChildren()){
                                    Comment comment = new Comment();
                                    comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                    comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                    comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                    comments.add(comment);
                                }

                                photoNew.setComments(comments);

                                List<Like> likesList = new ArrayList<>();

                                for (DataSnapshot dSnapshot : singleSnapshot.child(getString(R.string.field_likes))
                                        .getChildren()){

                                    Like like = new Like();
                                    like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                                    likesList.add(like);
                                }

                                photoNew.setLikes(likesList);
                            }catch (NullPointerException e){
                                Log.e(TAG, "onDataChange: NullPointerException: " + e.getMessage() );
                            }

                            ViewPostFragment fragment = new ViewPostFragment();
                            Bundle args = new Bundle();
                            args.putParcelable(getString(R.string.photo), photoNew);
                            args.putInt(getString(R.string.activity_number), 4);
                            fragment.setArguments(args);

                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.container, fragment);
                            transaction.addToBackStack(getString(R.string.view_post_fragment));
                            transaction.commit();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        Log.d(TAG, "onCancelled: query canceled.");

                    }
                });

            } else {
                Toast.makeText(mContext, "something went wrong", Toast.LENGTH_SHORT).show();
            }
            
        }else{
            Log.d(TAG, "init: inflating Profile");
            
            ProfileFragment fragment = new ProfileFragment();
            FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack(getString(R.string.profile_fragment));
            transaction.commit();
        }
        
    }

    @Override
    public void onBackPressed() {
        getSupportFragmentManager().popBackStack();

        Log.d(TAG, "Test: " + getSupportFragmentManager().getBackStackEntryCount());
        //this is a hack
        if (getSupportFragmentManager().getBackStackEntryCount() < 2) {
            finish();
        }
    }
}
