package com.e.dudusgram.Chats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.e.dudusgram.R;
import com.e.dudusgram.models.Conversation;

public class ChatsActivity extends AppCompatActivity {

    private static final String TAG = "ChatsActivity";

    private Context mContext = ChatsActivity.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        Log.d(TAG, "onCreate: started.");

        init();

    }

    private void init(){
        Log.d(TAG, "init: inflating." + getString(R.string.profile_fragment));

        Intent intent = getIntent();


        Log.d(TAG, "init: inflating Profile");

        ChatsFragment fragment = new ChatsFragment();
        FragmentTransaction transaction = ChatsActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.chats_fragment));
        transaction.commit();


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