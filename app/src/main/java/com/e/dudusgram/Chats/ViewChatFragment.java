package com.e.dudusgram.Chats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.e.dudusgram.Login.LoginActivity;
import com.e.dudusgram.R;
import com.e.dudusgram.Utils.CustomText;
import com.e.dudusgram.Utils.FirebaseMethods;
import com.e.dudusgram.Utils.MessageListAdapter;
import com.e.dudusgram.models.Members;
import com.e.dudusgram.models.Message;
import com.e.dudusgram.models.MessagesForDisplay;
import com.e.dudusgram.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ViewChatFragment extends Fragment {

    private static final String TAG = "ViewChatFragment";

    public ViewChatFragment() {
        super();
        setArguments(new Bundle());
    }

    private Context mContext;
    private String currentUserUsername;
    private String currentProfilePhoto;
    private RecyclerView mRecyclerView;
    private MessageListAdapter adapter;
    private ImageView mCheckmark, mBackArrow, mProfilePhoto;
    private CustomText mTextMessage;
    private TextView mUsername;

    private ArrayList<MessagesForDisplay> messages;
    private String mConversationID;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_chat, container, false);
        mRecyclerView = view.findViewById(R.id.reyclerview_message_list);
        mCheckmark = view.findViewById(R.id.chatbox_send);
        mTextMessage = view.findViewById(R.id.edittext_chatbox);
        mUsername = view.findViewById(R.id.username);
        mBackArrow = view.findViewById(R.id.backArrow);
        mProfilePhoto = view.findViewById(R.id.chat_profile_photo);
        mContext = getActivity();
        mFirebaseMethods = new FirebaseMethods(mContext);
        messages = new ArrayList<>();

        try{
            mConversationID = getConversationIDFromBundle();
            setupFirebaseAuth();
            setupWidgets();
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            prepareConversation(mConversationID);
            adapter = new MessageListAdapter(mContext, messages);
            mRecyclerView.setAdapter(adapter);
        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }

        return view;
    }

    private String getTimestamp(){

        SimpleDateFormat sdf = new SimpleDateFormat(mContext.getString(R.string.date_pattern), Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone(mContext.getString(R.string.timezone)));

        return sdf.format(new Date());
    }

    private void setupWidgets() {

        Query query = myRef.child(getString(R.string.dbname_members))
                .child(mConversationID);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Members members = dataSnapshot.getValue(Members.class);
                String user_id;

                if (members.getFirstUser().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    user_id = members.getSecondUser();
                } else {
                    user_id = members.getFirstUser();
                }

                Query query1 = myRef.child(getString(R.string.dbname_user_account_settings))
                        .child(user_id);

                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        UserAccountSettings settings = dataSnapshot.getValue(UserAccountSettings.class);

                        mUsername.setText(settings.getUsername());
                        ImageLoader imageLoader = ImageLoader.getInstance();
                        imageLoader.displayImage(settings.getProfile_photo(), mProfilePhoto);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mCheckmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mTextMessage.getText().toString().length() > 0) {
                    String timestamp = getTimestamp();
                    mFirebaseMethods.addNewChatMessage(mConversationID,currentUserUsername, mTextMessage.getText().toString(), timestamp);
                    MessagesForDisplay newMessage = new MessagesForDisplay(currentUserUsername, currentProfilePhoto, mTextMessage.getText().toString(), timestamp);
                    messages.add(newMessage);
                    adapter.notifyDataSetChanged();
                    mTextMessage.setText("");
                } else {
                    Toast.makeText(mContext, getString(R.string.empty_message), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private String getConversationIDFromBundle(){
        Log.d(TAG, "getConversationFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null){
            return bundle.getString(mContext.getString(R.string.conversation));
        } else{
            return null;
        }
    }

    private void prepareConversation (final String conversationID) {

        Query query = myRef.child(getString(R.string.dbname_messages))
                .child(conversationID);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Message message = snapshot.getValue(Message.class);

                    final MessagesForDisplay messageForDisplay = new MessagesForDisplay();
                    messageForDisplay.setMessage(message.getMessage());
                    messageForDisplay.setTimestamp(message.getTimestamp());

                    Query query1 = myRef.child(getString(R.string.dbname_user_account_settings))
                            .child(message.getUserID());

                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            UserAccountSettings user = dataSnapshot.getValue(UserAccountSettings.class);

                            messageForDisplay.setUsername(user.getUsername());
                            messageForDisplay.setProfile_picture(user.getProfile_photo());

                            messages.add(messageForDisplay);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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


    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        Query query = myRef
                .child(getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUserUsername = dataSnapshot.getValue(UserAccountSettings.class).getUsername();
                currentProfilePhoto = dataSnapshot.getValue(UserAccountSettings.class).getProfile_photo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
    }

    /**
     * Checks if the @param "user" is logged in.
     * @param user
     */

    private void checkCurrentUser(FirebaseUser user){
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if (user == null){
            Intent intent = new Intent(mContext, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
