package com.e.dudusgram.Utils;

import android.app.DownloadManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.e.dudusgram.R;
import com.e.dudusgram.models.MessagesForDisplay;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final String TAG = "MessageListAdapter";

    private Context mContext;
    private ArrayList<MessagesForDisplay> mMessageList;
    private String currentUsername;

    public MessageListAdapter(@NonNull Context context, @NonNull ArrayList<MessagesForDisplay> messageList) {
        mContext = context;
        mMessageList = messageList;
        getCurrentUsername();
        Log.d(TAG, "Test: " + messageList);
    }

    private void getCurrentUsername() {

        Query query = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.field_username));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                currentUsername = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        MessagesForDisplay message = mMessageList.get(position);

        if (message.getUsername().equals(currentUsername)) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        Log.d(TAG, "Test: viewType " + viewType);

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }


    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MessagesForDisplay message =  mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = ( itemView.findViewById(R.id.text_message_time));
        }

        void bind(MessagesForDisplay message) {
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            timeText.setText(message.getTimestamp());
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        CircleImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            nameText = itemView.findViewById(R.id.text_message_name);
            profileImage = itemView.findViewById(R.id.image_message_profile);
        }

        void bind(MessagesForDisplay message) {
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            timeText.setText(message.getTimestamp());

            nameText.setText(message.getUsername());

            // Insert the profile image from the URL into the ImageView.
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(message.getProfile_picture(), profileImage);
        }
    }

}