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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.e.dudusgram.Login.LoginActivity;
import com.e.dudusgram.R;
import com.e.dudusgram.Utils.BottomNavigationViewHelper;
import com.e.dudusgram.Utils.ChatsListAdapter;
import com.e.dudusgram.Utils.FirebaseMethods;
import com.e.dudusgram.models.Chat;
import com.e.dudusgram.models.Conversation;
import com.e.dudusgram.models.Members;
import com.e.dudusgram.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragment";
    private static final int ACTIVITY_NUM = 3;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private BottomNavigationViewEx bottomNavigationView;

    private Context mContext;
    private String currentUserUsername;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        mContext = getActivity();
        recyclerView = view.findViewById(R.id.my_recycler_view);
        layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        bottomNavigationView = view.findViewById(R.id.bottomNavViewBar);

        setupBottomNavigationView();
        setupFirebaseAuth();
        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView(){

        Log.d(TAG, "setupRecyclerView: setting up recycler view");

        final ArrayList<String> userChats = new ArrayList<>();

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        Query query = myRef
                .child(mContext.getString(R.string.dbname_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.field_chats));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    userChats.add(snapshot.getKey());
                }

                getData(userChats);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    private void getData(ArrayList<String> userChats){
        Log.d(TAG, "getData: " + userChats);

        final ArrayList<Conversation> conversations = new ArrayList<>();


        for (final String i : userChats){
            final Conversation conversation = new Conversation();

            conversation.setConversation_id(i);

            final Query query = myRef.child(getString(R.string.dbname_members))
                    .child(i);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Members members = new Members();
                    members = dataSnapshot.getValue(Members.class);

                    String interlocutor;

                    if (members.getFirstUser().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        interlocutor = members.getSecondUser();
                    } else {
                        interlocutor = members.getFirstUser();
                    }

                    Query getInterlocutor = myRef.child(getString(R.string.dbname_user_account_settings))
                            .child(interlocutor);

                    getInterlocutor.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            UserAccountSettings user = dataSnapshot.getValue(UserAccountSettings.class);

                            conversation.setProfile_image(user.getProfile_photo());
                            conversation.setUsername(user.getUsername());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    Query getMessage = myRef.child(getString(R.string.dbname_chat))
                            .child(i);

                    getMessage.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            Chat chat = dataSnapshot.getValue(Chat.class);

                            if (chat.getLast_message().contains(currentUserUsername)){
                                chat.setLast_message(chat.getLast_message().replace(currentUserUsername, "You"));
                            }

                            conversation.setMessage(chat.getLast_message());
                            conversation.setTimestamp(chat.getTimestamp());

                            Log.d(TAG, "Test: Conversation: " + conversation.toString());

                            conversations.add(conversation);
                            mAdapter.notifyDataSetChanged();

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

            Log.d(TAG, "Test: Conversations: " + conversations);

            mAdapter = new ChatsListAdapter(mContext, R.layout.layout_chat, conversations);
            recyclerView.setAdapter(mAdapter);

        }


    }

    /**
     * BottomNavigationView setup
     */

    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext, getActivity(), bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
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
                .child(getString(R.string.dbname_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_username));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUserUsername = dataSnapshot.getValue().toString();
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
