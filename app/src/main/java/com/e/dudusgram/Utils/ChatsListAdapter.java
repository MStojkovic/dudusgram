package com.e.dudusgram.Utils;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.e.dudusgram.Chats.ChatsActivity;
import com.e.dudusgram.R;
import com.e.dudusgram.models.Conversation;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsListAdapter extends RecyclerView.Adapter<ChatsListAdapter.MyViewHolder> {
    private static final String TAG = "ChatsListAdapter";

    private ArrayList<Conversation> mDataset;
    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        CircleImageView profile_image;
        TextView mUsername, mMessage, mTimestamp;
        String conversationId;


        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            profile_image = itemView.findViewById(R.id.chat_profile_image);
            mUsername = itemView.findViewById(R.id.username);
            mMessage = itemView.findViewById(R.id.message);
            mTimestamp = itemView.findViewById(R.id.timestamp);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ChatsListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<Conversation> myDataset) {
        mDataset = myDataset;
        mContext = context;
        layoutResource = resource;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChatsListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {

        View view = mInflater.inflate(layoutResource, parent, false);

        return new MyViewHolder(view);

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NotNull final MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Conversation conversation = mDataset.get(position);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(
                conversation.getProfile_image(),
                holder.profile_image);

        holder.mUsername.setText(conversation.getUsername());
        holder.mMessage.setText(conversation.getMessage());
        holder.mTimestamp.setText(conversation.getTimestamp());
        holder.conversationId = conversation.getConversation_id();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Loading conversation for user: " + holder.mUsername.getText());

                ((ChatsActivity)mContext).onConversationSelected(holder.conversationId);

            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    Conversation getItem(int id){
        return mDataset.get(id);
    }
}