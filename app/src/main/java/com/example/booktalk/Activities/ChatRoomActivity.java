package com.example.booktalk.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.booktalk.ChatFragment;
import com.example.booktalk.R;
import com.example.booktalk.utils.FirebaseUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.messaging.FirebaseMessaging;

public class ChatRoomActivity extends AppCompatActivity {

    ImageButton searchButton;
    ImageView backIv;

    private  static  final String TAG = "CHAT_ROOM_TAG";
    ChatFragment chatFragment;
//    ProfileFragment profileFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        chatFragment = new ChatFragment();
//        profileFragment = new ProfileFragment();

        searchButton = findViewById(R.id.main_search_btn);
        backIv = findViewById(R.id.backIv);


        searchButton.setOnClickListener((v)->{
            startActivity(new Intent(ChatRoomActivity.this,SearchUserActivity.class));
            Log.d(TAG, "onCreate: going to search user page");
        });

        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

//        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                if (item.getItemId() == R.id.menu_chat) {
//                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, chatFragment).commit();
//                }
//                return true;
//            }
//        });
//        bottomNavigationView.setSelectedItemId(R.id.menu_chat);


        getFCMToken();
    }
    void getFCMToken(){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String token = task.getResult();
                FirebaseUtil.currentUserDetails().update("fcmToken",token);
            }
        });
    }
}