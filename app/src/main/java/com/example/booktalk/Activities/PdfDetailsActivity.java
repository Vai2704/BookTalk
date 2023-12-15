package com.example.booktalk.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import android.Manifest;

import com.example.booktalk.Adapters.AdapterPdfFavorite;
import com.example.booktalk.Models.ModelPdf;
import com.example.booktalk.MyApplication;
import com.example.booktalk.R;
import com.example.booktalk.databinding.ActivityPdfDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfDetailsActivity extends AppCompatActivity {

    private ActivityPdfDetailsBinding binding;

    //pdf id, get from intent
    String bookId, bookTitle, bookUrl;

    boolean isMyFavorite = false;

    FirebaseAuth firebaseAuth;
    private static final  String TAG_DOWNLOAD = "DOWNLOAD_TAG";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //get data from intent
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        // at start hide download button, because we need book url that we will load late in function loadBookDetails()
        binding.downloadBookBtn.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }

        loadBookDetails();
        //increment book view count, whenever this page starts
        MyApplication.incrementBookViewCont(bookId);




        //handle click, back btn
        binding.backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handle click, open to view pdf
        binding.readBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(PdfDetailsActivity.this, PdfViewActivity.class);
                intent1.putExtra("bookId",bookId);
                startActivity(intent1);
            }
        });

        //handle click, download pdf
        binding.downloadBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG_DOWNLOAD, "onClick: Checking Permission");
                if(ContextCompat.checkSelfPermission(PdfDetailsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG_DOWNLOAD, "onClick: Permission already Granted can download book");
                    MyApplication.downloadBook(PdfDetailsActivity.this,""+bookTitle,""+bookId,""+bookUrl);
                }
                else{
                    Log.d(TAG_DOWNLOAD, "onClick: Permission was not granted, request permission...");
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });

        //handle click, add/remove favorite
        binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(PdfDetailsActivity.this, "You're not logged in", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(isMyFavorite){
                        //in favorite, remove from favorite
                        MyApplication.removeFromFavorite(PdfDetailsActivity.this, bookId);
                    }
                    else{
                        //not in favorite, add to favorite
                        MyApplication.addToFavorite(PdfDetailsActivity.this, bookId);
                    }
                }
            }
        });

    }



    //request permission
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->{
                if(isGranted){
                    Log.d(TAG_DOWNLOAD, "Permission Granted");
                    MyApplication.downloadBook(this,""+bookTitle,""+bookId, ""+bookUrl);
                }
                else {
                    Log.d(TAG_DOWNLOAD, " Permission denied...");
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
                }
            });

    private void loadBookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookTitle =""+ snapshot.child("title").getValue();
                        String description =""+ snapshot.child("description").getValue();
                        String categoryId =""+ snapshot.child("categoryId").getValue();
                        String viewCount =""+ snapshot.child("viewCount").getValue();
                        String downloadCount =""+ snapshot.child("downloadCount").getValue();
                        bookUrl =""+ snapshot.child("url").getValue();
                        String timeStamp =""+ snapshot.child("timeStamp").getValue();

                        //required data is loaded, show download button
                        binding.downloadBookBtn.setVisibility(View.VISIBLE);

                        //format date
                        String date = MyApplication.formatTimestamp(Long.parseLong(timeStamp));

                        MyApplication.loadCategory(
                                ""+categoryId,
                                binding.categoryTv
                        );

                        MyApplication.loadPdfFormatUrlSinglePage(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.pdfView,
                                binding.progressBar,
                                binding.pageTv
                        );
                        MyApplication.loadSize(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.sizeTv
                        );


                        //set data
                        binding.titleTv.setText(bookTitle);
                        binding.descriptionTv.setText(description);
                        binding.viewsTv.setText(viewCount.replace("null","N/A"));
                        binding.downloadTv.setText(downloadCount.replace("null","N/A"));
                        binding.dateTv.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

   private void checkIsFavorite(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isMyFavorite = snapshot.exists();
                        if(isMyFavorite){
                            //exists in favorite
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_fav,0,0);
                            binding.favoriteBtn.setText("Remove Favorite");
                        }
                        else{
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_fav_border,0,0);
                            binding.favoriteBtn.setText("Add to Favorite");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}