package com.example.booktalk.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booktalk.Filters.FilterPdfAdmin;
import com.example.booktalk.Models.ModelPdf;
import com.example.booktalk.MyApplication;
import com.example.booktalk.Activities.PdfDetailsActivity;
import com.example.booktalk.Activities.PdfEditActivity;
import com.example.booktalk.databinding.RowPdfAdminBinding;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {

//    public static final long MAX_BYTES_PDF = 50000000;//50Mb
    //Context
    private Context context;
    //arraylist to hold list of data of type ModelPdf
   public ArrayList<ModelPdf> pdfArrayList, filterList;

   private FilterPdfAdmin filter;
    private static final String TAG = "PDF_ADAPTER_TAG";
    //view binding of row_pdf_admin.xml
    private RowPdfAdminBinding binding;
    //progress dialog
    private ProgressDialog progressDialog;

    //Constructor of this class
    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;

        //init progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind layout using view binding
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {

        /*Get data, set data, handle clicks */

        //get data
        ModelPdf model = pdfArrayList.get(position);
        String pdfId = model.getId();
        String categoryId = model.getCategoryId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        long timestamp = model.getTimestamp();

        //we need to convert timestamp to dd/mm/yyyy format
        String formattedDate = MyApplication.formatTimestamp(timestamp);

        //set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);
        
        //load further details like category, pdf from url, pdf size in separate functions
        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTv
                );

        MyApplication.loadPdfFormatUrlSinglePage(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar,
                null//here we don't need page number that's why we passed null.
                );

        MyApplication.loadSize(
               ""+pdfUrl,
               ""+title,
               holder.sizeTv
               );

        //handle click, show dialog with options Edit, Delete
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionDialog(model, holder);
            }
        });

        //handle click, open pdf details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfDetailsActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });
    }

    private void moreOptionDialog(ModelPdf model, HolderPdfAdmin holder) {
        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();
        //option show in dialog
        String [] options = {"Edits","Delete"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Optons")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //handle dialog option click
                        if(i == 0){
                            //Edit clicked, Open new activity to edit the info of book
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId",bookId);
                            context.startActivity(intent);

                        }
                        else if(i ==1){
                            //Delete clicked
                            MyApplication.deleteBook(context,
                                    ""+ bookId,
                                    ""+ bookUrl,
                                    ""+bookTitle
                            );
                        }
                    }
                })
                .show();
    }


    @Override
    public int getItemCount() {
        return pdfArrayList.size();// return number of records/ Arraylist size.
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterPdfAdmin(filterList, this);
        }
        return filter;
    }

    //view holder class for row_pdf_admin.xml
    class HolderPdfAdmin extends RecyclerView.ViewHolder{
        //UI views of row_pdf_admin.xml
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv,descriptionTv,categoryTv,sizeTv,dateTv;
        ImageButton moreBtn;
        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
           categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.moreBtn;
        }
    }
}
