package com.testerslol123.flashchatnewfirebase;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MainChatActivity extends AppCompatActivity {

    // TODO: Add member variables here:
    private String mDisplayName;
    private ListView mChatListView;
    private EditText mInputText;
    private ImageButton mSendButton;
    private DatabaseReference mDatabaseReference;
    private ChatListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);

        // TODO: Set up the display name and get the Firebase reference
        setupDisplayName();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();


        // Link the Views in the layout to the Java code
        mInputText = (EditText) findViewById(R.id.messageInput);
        mSendButton = (ImageButton) findViewById(R.id.sendButton);
        mChatListView = (ListView) findViewById(R.id.chat_list_view);

        // TODO: Send the message when the "enter" button is pressed
        mInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                sendMessage();
                return true;
            }
        });

        // TODO: Add an OnClickListener to the sendButton to send a message
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    // TODO: Retrieve the display name from the Shared Preferences
    private void setupDisplayName () {
//        SharedPreferences prefs = getSharedPreferences(RegisterActivity.CHAT_PREFS, MODE_PRIVATE);
//        mDisplayName = prefs.getString(RegisterActivity.DISPLAY_NAME_KEY, null);
//
//        if (mDisplayName == null) mDisplayName = "Anonymous";

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDisplayName = user.getDisplayName();
    }

    private void sendMessage() {
        Log.d("FlashChat", "I type something");
        // TODO: Grab the text the user typed in and push the message to Firebase
        String input = mInputText.getText().toString();

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digest.length; i++) {
                sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
            }
            Log.d("Kripto", "Hasil Hash SHA-512 = " + sb);

            if(!input.equals("")) {
                InstantMessage chat = new InstantMessage(input, mDisplayName);
//                InstantMessage chat = new InstantMessage(sb.toString(), mDisplayName);
                mDatabaseReference.child("messages").push().setValue(chat);

                // Get current User
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String email = user.getEmail();

                Log.d("FireStore", "email user = " + email);

                // Access a Cloud Firestore instance from your Activity
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                Date currentTime = Calendar.getInstance().getTime();

                // Save message to fire store
                Map<String, Object> messageFirestore = new HashMap<>();
                messageFirestore.put("message", input);
                messageFirestore.put("email", email);
                messageFirestore.put("chat_time", currentTime);

                db.collection("Messages")
                    .add(messageFirestore)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("FireStore", "Save Message with snapshot ID: " + documentReference.getId());

                            mInputText.setText("");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("FireStore", "Error adding save message", e);
                        }
                    });
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    // TODO: Override the onStart() lifecycle method. Setup the adapter here.
    @Override
    public void onStart() {
        super.onStart();
        mAdapter = new ChatListAdapter(this, mDatabaseReference, mDisplayName);
        mChatListView.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        // TODO: Remove the Firebase event listener on the adapter.
        mAdapter.cleanup();
    }

}
