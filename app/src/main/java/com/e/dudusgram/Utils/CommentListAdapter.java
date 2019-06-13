package com.e.dudusgram.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.e.dudusgram.Profile.ProfileActivity;
import com.e.dudusgram.R;
import com.e.dudusgram.models.Comment;
import com.e.dudusgram.models.User;
import com.e.dudusgram.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentListAdapter extends ArrayAdapter<Comment> {

    private static final String TAG = "CommentListAdapter";

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;
    private DatabaseReference mReference;
    private String mPostOwner;
    private String mPostID;
    private String mCurrentUserID = "";

    CommentListAdapter(@NonNull Context context, @LayoutRes int resource,
                       @NonNull List<Comment> objects, @NonNull String postID, @NonNull String postOwner) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResource = resource;
        mReference = FirebaseDatabase.getInstance().getReference();
        mPostID = postID;
        mPostOwner = postOwner;
    }

    private static class ViewHolder {
        TextView comment, username, timestamp, likes, delete;
        CircleImageView profileImage;
        User user = new User();
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.comment = convertView.findViewById(R.id.comment);
            holder.username = convertView.findViewById(R.id.comment_username);
            holder.timestamp = convertView.findViewById(R.id.comment_time_posted);
            holder.delete = convertView.findViewById(R.id.comment_delete);
            holder.likes = convertView.findViewById(R.id.comment_likes);
            holder.profileImage = convertView.findViewById(R.id.comment_profile_image);
            holder.delete.setVisibility(View.VISIBLE);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
            holder.likes.setVisibility(View.VISIBLE);
            holder.delete.setVisibility(View.VISIBLE);
        }

        mCurrentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // set the comment
        holder.comment.setText(getItem(position).getComment());
        holder.username.setText(getItem(0).getUser_id());

        // set the timestamp difference
        String timestampDifference = getTimestampDifference(getItem(position));

        if(!timestampDifference.equals("0")){
            holder.timestamp.setText(String.format("%s %s", timestampDifference, mContext.getString(R.string.days)));
        }else{
            holder.timestamp.setText(mContext.getString(R.string.today));
        }

        // set the username and profile image
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {

                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user: "
                            + singleSnapshot.getValue(UserAccountSettings.class).getUsername());

                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            holder.username.setClickable(false);
                            Log.d(TAG, "onClick: navigating to profile of: "
                                    + holder.user.getUsername());

                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user),
                                    holder.user);
                            mContext.startActivity(intent);
                            holder.username.setClickable(true);
                        }
                    });

                    ImageLoader imageLoader = ImageLoader.getInstance();

                    imageLoader.displayImage(
                            singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.profileImage);

                    holder.profileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            holder.profileImage.setClickable(false);
                            Log.d(TAG, "onClick: navigating to profile of: "
                                    + holder.user.getUsername());

                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user),
                                    holder.user);
                            mContext.startActivity(intent);
                            holder.profileImage.setClickable(true);
                        }
                    });

                    holder.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            holder.delete.setClickable(false);
                            new AlertDialog.Builder(mContext)
                                    .setTitle("Delete")
                                    .setMessage("Do you really want to delete the comment?")
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            FirebaseMethods firebaseMethods = new FirebaseMethods(getContext());
                                            firebaseMethods.deleteComment(mPostOwner, mPostID, getItem(position).getComment_id());
                                            remove(getItem(position));
                                            notifyDataSetChanged();
                                        }})
                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            holder.delete.setClickable(true);
                                        }
                                    }).show();
                        }
                    });

                    break;
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });

        if (mCurrentUserID.equals(getItem(position).getUser_id())
                || mCurrentUserID.equals(mPostOwner)) {
            holder.delete.setVisibility(View.VISIBLE);
        } else {
            holder.delete.setVisibility(View.GONE);
        }

        try {
            if (getItem(position).getDescription()) {
                holder.likes.setVisibility(View.GONE);
                holder.delete.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "getView: NullPointerException " + e.getMessage());
        }


        //get the user object
        Query userQuery = mReference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user: " +
                            singleSnapshot.getValue(User.class).getUsername());

                    holder.user = singleSnapshot.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return convertView;
    }

    /**
     * Returns a string containing the number of days ago the post was made
     * @return
     */

    private String getTimestampDifference(Comment comment){

        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(mContext.getString(R.string.date_pattern), Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Zagreb"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = comment.getDate_created();

        String difference;
        try{

            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24));

        } catch (ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage());
            difference = "0";
        }

        return difference;
    }
}
