package com.example.booktalk.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booktalk.Activities.PdfDetailsActivity;
import com.example.booktalk.Models.ModelPdf;
import com.example.booktalk.MyApplication;
import com.example.booktalk.databinding.RowPdfFavBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterPdfFavorite extends RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite>{

    private Context context;
    private ArrayList<ModelPdf> favArrayList;

    //view binding
    private RowPdfFavBinding binding;

    private static final String TAG = "FAV_BOOK_TAG";

    //Constructor
    public AdapterPdfFavorite(Context context, ArrayList<ModelPdf> favArrayList) {
        this.context = context;
        this.favArrayList = favArrayList;
    }

    @NonNull
    @Override
    public HolderPdfFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfFavBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfFavorite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfFavorite holder, int position) {

        ModelPdf model = favArrayList.get(position);

        loadBookDetails(model, holder);

        //handle click to open book detail page
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailsActivity.class);
                intent.putExtra("bookId",model.getId());
                context.startActivity(intent);
            }
        });

        //handle click to remove book from favorite
        holder.removeFavIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.removeFromFavorite(context, model.getId());
            }
        });


    }

    private void loadBookDetails(ModelPdf model, HolderPdfFavorite holder) {
        String bookId = model.getId();
        Log.d(TAG, "loadBookDetails: book details loading" + bookId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get book info

                        String bookTitle = ""+ snapshot.child("title").getValue();
                        String description = "" +snapshot.child("description").getValue();
                        String categoryId = "" +snapshot.child("categoryId").getValue();
                        String bookUrl = "" +snapshot.child("url").getValue();
                        String timeStamp = ""+snapshot.child("timeStamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String viewCount = ""+snapshot.child("viewCount").getValue();
                        String downloadCount = ""+snapshot.child("downloadCount").getValue();


                        model.setFavorite(true);
                        model.setTitle(bookTitle);
                        model.setDescription(description);
                        model.setTimestamp(Long.parseLong(timeStamp));
                        model.setCategoryId(categoryId);
                        model.setUid(uid);
                        model.setUrl(bookUrl);


                        //formate date
                        String date = MyApplication.formatTimestamp(Long.parseLong(timeStamp));

                        MyApplication.loadCategory(categoryId, holder.categoryTv);
                        MyApplication.loadPdfFormatUrlSinglePage(""+ bookUrl,""+ bookTitle, holder.pdfView, holder.progressBar, null);
                        MyApplication.loadSize(""+bookUrl,""+bookTitle,holder.sizeTv );

                        //set data to views
                        holder.titleTv.setText(bookTitle);
                        holder.descriptionTv.setText(description);
                        holder.dateTv.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return favArrayList.size();
    }

    //ViewHolder class
    class HolderPdfFavorite extends RecyclerView.ViewHolder{

        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, dateTv, categoryTv, sizeTv;
        ImageView removeFavIv;
        public HolderPdfFavorite(@NonNull View itemView) {
            super(itemView);

            //init UI view
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            dateTv = binding.dateTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            removeFavIv = binding.removeFavIv;
        }
    }
}
