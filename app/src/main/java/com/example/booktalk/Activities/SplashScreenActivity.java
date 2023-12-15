package com.example.booktalk.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.booktalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        //firebase init
        firebaseAuth = FirebaseAuth.getInstance();

        // open home screen after 2 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUser();
//                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            }
        },2000);
    }

    private void checkUser() {

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null){
            // user is not logged in
            // start main activity
            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            finish();
            Log.d("general", "checkUser: user not found");
        }
        else {
            //check the user details
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange( DataSnapshot snapshot) {
                            String userType =""+ snapshot.child("UserType").getValue();

                            if(userType.equals("user")){
                                startActivity(new Intent(SplashScreenActivity.this, DashboardUserActivity.class));
                                finish();
                                Log.d("general", "onDataChange: user found");
                            }
                            else if(userType.equals("admin")){
                                startActivity(new Intent(SplashScreenActivity.this, DashboardAdminActivity.class));
                                finish();
                                Log.d("general", "onDataChange: admin found");
                            }
                        }

                        @Override
                        public void onCancelled( DatabaseError error) {

                        }
                    });
        }
    }
}