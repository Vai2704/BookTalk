package com.example.booktalk.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.booktalk.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {

    //view binding
    private ActivityPdfEditBinding binding;
    //book id get from intent started form AdapterPdfAdmin
    private String bookId;
    //progress dialog
    private ProgressDialog progressDialog;

    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    private static final String TAG ="BOOK_EDIT_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookId = getIntent().getStringExtra("bookId");

        //setup progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadCategories();
        loadBookInfo();
        //handle click ,pick a category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });

        //handle click, go to previous screen
        binding.backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //handle click, begin upload
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    String title = "" , description ="";
    private void validateData() {
        //get data
        title = binding.titleEt.toString().trim();
        description = binding.descriptionEt.toString().trim();

        if(TextUtils.isEmpty(title)){
            Toast.makeText(this, "Enter title...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description)){
            Toast.makeText(this, "Enter description...", Toast.LENGTH_SHORT).show();
        } 
        else if(TextUtils.isEmpty(selectedCategoryId)){
            Toast.makeText(this, "Pick Category...", Toast.LENGTH_SHORT).show();
        }
        else{
            updatePdf();
        }
    }

    private void updatePdf() {
        Log.d(TAG, "updatePdf: Starting updating pdf info to db...");
        //show progress 
        progressDialog.setMessage("Updating info...");
        //setup data to update 
        HashMap<String, Object> map = new HashMap<>();
        map.put("title",""+title);
        map.put("description",""+description);
        map.put("categoryId",""+selectedCategoryId);
        
        
        //start updating
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .updateChildren(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Successfully Updated");
                        progressDialog.dismiss();
                        Toast.makeText(PdfEditActivity.this, "Successfully Updated pdf", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to update pdf due to "+e.getMessage());
                        Toast.makeText(PdfEditActivity.this, " Failed to update pdf due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading Book Info");

        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        selectedCategoryId =""+ snapshot.child("categoryId").getValue();
                        String description = ""+ snapshot.child("description").getValue();
                        String title = ""+ snapshot.child("title").getValue();
                        //set to views
                        binding.titleEt.setText(title);
                        binding.descriptionEt.setText(description);


                        Log.d(TAG, "onDataChange: Loading Book Category Info");
                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Categories");
                        refBookCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //get category
                                        String category = ""+ snapshot.child("category").getValue();
                                        //set to category view in
                                        binding.categoryTv.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String selectedCategoryId ="", selectedCategoryTitle ="";

    private void categoryDialog(){
        //make string array from arraylist of string
        String[] categoryArray = new String[categoryTitleArrayList.size()];
        for(int i = 0; i<categoryTitleArrayList.size();i++){
            categoryArray[i] = categoryTitleArrayList.get(i);
        }

        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Category")
                .setItems(categoryArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedCategoryId = categoryIdArrayList.get(i);
                        selectedCategoryTitle = categoryTitleArrayList.get(i);

                        //set to textView
                        binding.categoryTv.setText(selectedCategoryTitle);
                    }
                })
                .show();
    }
    private void loadCategories() {
        Log.d(TAG, "loadCategories: Loading Categories");

        categoryIdArrayList = new ArrayList<>();
        categoryTitleArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArrayList.clear();
                categoryTitleArrayList.clear();

                for(DataSnapshot ds: snapshot.getChildren()){
                    String id =""+ ds.child("id").getValue();
                    String category = ""+ ds.child("category").getValue();
                    categoryTitleArrayList.add(category);
                    categoryIdArrayList.add(id);

                    Log.d(TAG, "onDataChange: ID:" +id);
                    Log.d(TAG, "onDataChange: Category:" +category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}