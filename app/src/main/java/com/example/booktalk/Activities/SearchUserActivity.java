package com.example.booktalk.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booktalk.Adapters.SearchUserRecyclerAdapter;
import com.example.booktalk.Models.UserModel;
import com.example.booktalk.R;
import com.example.booktalk.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class SearchUserActivity extends AppCompatActivity {

    EditText searchInput;
    ImageButton searchButton;
    ImageButton backButton;
    RecyclerView recyclerView;
    
    private static final String TAG = "SEARCH_USER_TAG";
    SearchUserRecyclerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        searchInput = findViewById(R.id.search_username_input);
        searchButton = findViewById(R.id.search_user_btn);
        backButton = findViewById(R.id.back_btn);
        recyclerView = findViewById(R.id.search_user_recycler_view);

        Log.d(TAG, "onCreate: requestFocus called");
        searchInput.requestFocus();


        backButton.setOnClickListener(v->{
            onBackPressed();

        });
        searchButton.setOnClickListener(v->{
            Log.d(TAG, "onCreate: search onClick..");
            String searchTerm = searchInput.getText().toString();
            Log.d(TAG, "onCreate: searchTerm");
            if(searchTerm.isEmpty() || searchTerm.length()<3){
                Log.d(TAG, "onCreate: Searching for user");
                searchInput.setError("Invalid Username");
                return;
            }
            Log.d(TAG, "onCreate: searchRecyclerView called");
            setupSearchRecyclerView(searchTerm);

        });
    }
    void setupSearchRecyclerView(String searchTerm){
        Log.d(TAG, "setupSearchRecyclerView: Showing user list...");
        Query query = FirebaseUtil.allUserCollectionReference()
                .whereGreaterThanOrEqualTo("username",searchTerm)
                .whereLessThanOrEqualTo("username",searchTerm+'\uf8ff');

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class).build();

        adapter = new SearchUserRecyclerAdapter(options,getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null)
            adapter.startListening();
    }
}