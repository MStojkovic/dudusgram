package com.e.dudusgram.Chats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.e.dudusgram.R;
import com.e.dudusgram.Utils.FirebaseMethods;
import com.e.dudusgram.models.Members;
import com.e.dudusgram.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class ChatsActivity extends AppCompatActivity {

    private static final String TAG = "ChatsActivity";

    private Context mContext = ChatsActivity.this;
    private String currentUserUsername;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        Log.d(TAG, "onCreate: started.");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        Query query = myRef
                .child(getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUserUsername = dataSnapshot.getValue(UserAccountSettings.class).getUsername();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        init();

    }

    private void init(){
        Log.d(TAG, "init: inflating." + getString(R.string.profile_fragment));

        final Intent intent = getIntent();

        if (intent.hasExtra(mContext.getString(R.string.calling_activity))) {

            if (intent.hasExtra(getString(R.string.user_id_for_chat))) {

                Query query = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_members))
                        .orderByChild(getString(R.string.field_first_user))
                        .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        boolean found = false;

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            Log.d(TAG, "Test: " + snapshot.getValue());

                            if (snapshot.getValue(Members.class).getSecondUser().equals(intent.getStringExtra(getString(R.string.user_id_for_chat)))) {

                                found = true;

                                ViewChatFragment fragment = new ViewChatFragment();
                                Bundle args = new Bundle();
                                args.putString(getString(R.string.conversation), snapshot.getKey());
                                fragment.setArguments(args);

                                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.container, fragment);
                                transaction.addToBackStack(getString(R.string.view_chat_fragment));
                                transaction.commit();
                            }
                        }

                        if (!found) {

                            FirebaseMethods mFirebaseMethods = new FirebaseMethods(mContext);
                            String newConversation = mFirebaseMethods.sendMessageWithNoExistingChat(intent.getStringExtra(getString(R.string.user_id_for_chat)), currentUserUsername, "This is the beginning of your conversation");

                            ViewChatFragment fragment = new ViewChatFragment();
                            Bundle args = new Bundle();
                            args.putString(getString(R.string.conversation), newConversation);
                            fragment.setArguments(args);

                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.container, fragment);
                            transaction.addToBackStack(getString(R.string.view_chat_fragment));
                            transaction.commit();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        } else {

            Log.d(TAG, "init: inflating Chats");

            ChatsFragment fragment = new ChatsFragment();
            FragmentTransaction transaction = ChatsActivity.this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack(getString(R.string.chats_fragment));
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

    public void onConversationSelected (String conversationID){

        ViewChatFragment fragment = new ViewChatFragment();
        Bundle args = new Bundle();
        args.putString(getString(R.string.conversation), conversationID);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_chat_fragment));
        transaction.commit();

    }

}