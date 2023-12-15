package com.example.booktalk.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booktalk.Activities.PdfDetailsActivity;
import com.example.booktalk.Filters.FilterPdfUser;
import com.example.booktalk.Models.ModelPdf;
import com.example.booktalk.MyApplication;
import com.example.booktalk.databinding.RowPdfUserBinding;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> implements Filterable {

    private Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterArrayList;
    private FilterPdfUser filter;

    private RowPdfUserBinding binding;

    private static final String TAG = "ADAPTER_PDF_USER";
    public AdapterPdfUser(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderPdfUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfUser holder, int position) {
        /*get data, set data, handle click*/

        //get data
        ModelPdf model = pdfArrayList.get(position);
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        String categoryId = model.getCategoryId();
        String pdfId = model.getId();
        long timestamp = model.getTimestamp();

        //convert time
        String date = MyApplication.formatTimestamp(timestamp);

        //set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(date);

        MyApplication.loadPdfFormatUrlSinglePage(""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar,
                null//here we don't need page number that's why we passed null.
        );
        MyApplication.loadCategory(""+categoryId,
                holder.categoryTv
        );
        MyApplication.loadSize(
                ""+pdfUrl,
                ""+title,
                holder.sizeTv
        );

        //handle click, show pdf details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailsActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterPdfUser(filterArrayList,this);
        }
        return filter;
    }

    class  HolderPdfUser extends RecyclerView.ViewHolder{

        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        PDFView pdfView;
        ProgressBar progressBar;

        public HolderPdfUser(@NonNull View itemView) {
            super(itemView);

            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
        }
    }
}
