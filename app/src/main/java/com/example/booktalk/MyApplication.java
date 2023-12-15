package com.example.booktalk;

import static com.example.booktalk.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.booktalk.Adapters.AdapterPdfAdmin;
import com.example.booktalk.Models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

//application class runs before launcher activity
public class MyApplication extends Application {

    private static final  String TAG_DOWNLOAD = "DOWNLOAD_TAG";
    @Override
    public void onCreate() {
        super.onCreate();
    }

    //created a static method to convert timestamp in proper date format
    public static final String formatTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        //format timestamp to dd/mm/yyyy
        String date = DateFormat.format("dd/MM/yyyy",cal).toString();

        return date;
    }
    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {

        String TAG = "DELETE_BOOK_TAG";

        Log.d(TAG, "deleteBook: Deleting book");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Deleting "+bookTitle+"....");
        progressDialog.show();

        Log.d(TAG, "deleteBook: Deleting book from storage....");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Deleted form storage");
                        Log.d(TAG, "onSuccess: Now deleting info from db");

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
                        ref.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Deleted from db too");
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "Book deleted successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to delete form db due to "+e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to delete from storage due to "+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public static void loadSize(String pdfUrl, String pdfTitle, TextView sizeTv) {

        String TAG = "PDF_SIZE_TAG";
        //using url we can get file and its metadata from firebase storage
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        //get size in bytes
                        double bytes = storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess: "+pdfTitle+""+bytes);

                        //convert bytes to kb,mb
                        double kb =bytes/1024;
                        double mb = kb/1024;

                        if(mb>= 1){
                           sizeTv.setText(String.format("%.2f",mb)+"MB");
                        }
                        else  if(kb>= 1){
                            sizeTv.setText(String.format("%.2f",kb)+"KB");
                        }
                        else{
                           sizeTv.setText(String.format("%.2f",bytes)+"bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to getting metadata
                        Log.d(TAG, "onFailure: "+e.getMessage());
//                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void loadPdfFormatUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar, TextView pagesTv) {
        String TAG = "PDF_LOAD_SINGLE_TAG";
        //using url we can get file and its metadata from firebase storage

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: "+pdfTitle+"successfully got the title");

                        //set to pdfView
                        pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        //hide progress
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        //hide progress
                                       progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: "+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        //pdf loaded
                                        //hide progress
                                       progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: pdf loaded successfully");

                                        //if pagesTv is not null then set page numbers
                                        if(pagesTv != null){
                                            pagesTv.setText(""+nbPages);
                                        }
                                    }
                                })
                                .load();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //hide progress
                       progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onFailure: Failed to getting file from url due to" +e.getMessage());
                    }
                });
    }

    public static void loadCategory(String categoryId, TextView categoryTv) {
        //get category using category Id

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = ""+snapshot.child("category").getValue();

                        //set to category text view
                        categoryTv.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void incrementBookViewCont(String bookId){
        // Get book views count
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String viewCount =""+ snapshot.child("viewCount").getValue();
                        //In case of null replace with 0
                        if(viewCount.equals("") || viewCount.equals("null")){
                            viewCount = "0";
                        }
                        //2) increment views count
                        long newViewCount = Long.parseLong(viewCount) +1;
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("viewCount" , newViewCount);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
                        ref.child(bookId)
                                .updateChildren(map);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static  void downloadBook(Context context,String bookTitle, String bookId, String bookUrl){
        Log.d(TAG_DOWNLOAD, "downloadBook: Downloading Book...");
        String nameWithExtension = bookTitle+" .pdf";
        Log.d(TAG_DOWNLOAD, "downloadBook: Name "+nameWithExtension);

        //progress dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Downloading"+ nameWithExtension+"...");
        progressDialog.show();

        //download from firebase storage using url
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG_DOWNLOAD, "onSuccess: Book Downloaded ");
                        Log.d(TAG_DOWNLOAD, "onSuccess: Saving book...");
                        saveDownloadedBook(context,progressDialog,bytes,nameWithExtension,bookId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to download due to "+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failed to download due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static void saveDownloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String bookId) {
        Log.d(TAG_DOWNLOAD, "saveDownloadedBook:Saving downloaded book  ");
        try {
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadFolder.mkdirs();

            String filePath = downloadFolder.getPath()+"/"+ nameWithExtension;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Saved to download folder", Toast.LENGTH_SHORT).show();
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saved to download folder");
            progressDialog.dismiss();

            incrementBookDownloadCount(bookId);
        }
        catch (Exception e){
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Failed saving to download folder due to "+e.getMessage());
            Toast.makeText(context, "Failed saving to download folder due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    private static void incrementBookDownloadCount(String bookId) {
        Log.d(TAG_DOWNLOAD, "incrementBookDownloadCount: Incrementing book download count");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String downloadCount =""+ snapshot.child("downloadCount").getValue();
                        Log.d(TAG_DOWNLOAD, "onDataChange: Downloads Count: "+ downloadCount);

                        if(downloadCount.equals("")|| downloadCount.equals("null")){
                            downloadCount = "0";
                        }

                        //convert to long
                        Long newDownloadCount = Long.parseLong(downloadCount) +1;
                        Log.d(TAG_DOWNLOAD, "onDataChange: New Download Count" +newDownloadCount);


                        //setup data to update
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("downloadCount", newDownloadCount);

                        //update new incremented downloads count to db
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
                        ref.child(bookId).updateChildren(map)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG_DOWNLOAD, "onSuccess: Downloads Count updated...");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG_DOWNLOAD, "onFailure: Failed to update Downloads Count due to "+e.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



    public static void addToFavorite(Context context, String bookId){
        //we can only add if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            Toast.makeText(context, "Please Log In ", Toast.LENGTH_SHORT).show();
        }
        else {
            long timestamp = System.currentTimeMillis();

            //setup data to add in firebase ab of current user for favorite book
            HashMap<String, Object> map = new HashMap<>();
            map.put("bookId", ""+bookId);
            map.put("timestamp",""+timestamp);

            //save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .setValue(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Added to favorite list...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to add in favorite list due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public static void removeFromFavorite(Context context, String bookId){
        //we can only remove if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() == null){
            Toast.makeText(context, "Please Log In ", Toast.LENGTH_SHORT).show();
        }
        else {
            //remove to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Removed from favorite list...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to remove from favorite list due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
