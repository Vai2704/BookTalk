package com.example.booktalk.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.booktalk.Adapters.AdapterPdfFavorite;
import com.example.booktalk.Models.ModelPdf;
import com.example.booktalk.MyApplication;
import com.example.booktalk.R;
import com.example.booktalk.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private static final String TAG = "PROFILE_TAG";    
    //firebase auth, loading user data using user uid
    private FirebaseAuth firebaseAuth;

    //Arraylist to store the books
    private ArrayList<ModelPdf> pdfArrayList;

    //adapter to set in recyclerview
    private AdapterPdfFavorite adapterPdfFavorite;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //setup firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();

        loadFavoriteBook();

        //handle click, edit button
        binding.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class));
            }
        });
        //handle click, logout button
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                startActivity(new Intent(ProfileActivity.this,MainActivity.class));
            }
        });

        //handle click, back button
        binding.backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

    private void loadFavoriteBook(){
        //init list
        pdfArrayList = new ArrayList<>();

        //load favorite books from database
        //users->user id > favorite

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){
                            //we will only get the bookId here
                            String bookId = ""+ ds.child("bookId").getValue();
                            ModelPdf modelPdf = new ModelPdf();
                            modelPdf.setId(bookId);

                            //add model to list
                            pdfArrayList.add(modelPdf);
                        }

                        adapterPdfFavorite = new AdapterPdfFavorite(ProfileActivity.this, pdfArrayList);

                        //set adapter to recycler view
                        binding.bookRv.setAdapter(adapterPdfFavorite);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadUserInfo() {
        Log.d(TAG, "loadUserInfo: Loading info of user"+firebaseAuth.getUid());
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get all info of user here from snapshot
                        String email =""+ snapshot.child("email").getValue();
                        String name =""+ snapshot.child("name").getValue();
                        String profileImage =""+ snapshot.child("profileImage").getValue();
                        String timestamp =""+ snapshot.child("timestamp").getValue();
                        String uid =""+ snapshot.child("uid").getValue();
                        String UserType =""+ snapshot.child("UserType").getValue();

                        //format date to dd/mm/yyyy
                        String formattedDate = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        //setup data to ui
                        binding.nameTv.setText(name);
                        binding.emailTv.setText(email);

                        //set image, using glide
                        Glide.with(ProfileActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.ic_person)
                                .into(binding.profileIv);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}