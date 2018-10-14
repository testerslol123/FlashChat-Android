package com.testerslol123.flashchatnewfirebase;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class ChatroomsActivity extends Activity {
    ListView chatroomListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatrooms);
        chatroomListView = findViewById(R.id.chatroomListView);
    }


}
