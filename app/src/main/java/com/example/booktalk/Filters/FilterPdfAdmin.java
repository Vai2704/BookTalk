package com.example.booktalk.Filters;

import android.widget.Filter;

import com.example.booktalk.Adapters.AdapterCategory;
import com.example.booktalk.Adapters.AdapterPdfAdmin;
import com.example.booktalk.Models.ModelCategory;
import com.example.booktalk.Models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {

    // arraylist in which we want to search
    ArrayList<ModelPdf> filterList;
    //adapter in which filter need to be implemented
    AdapterPdfAdmin adapterPdfAdmin;

    // constructors

    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        //value should not be null and empty
        if(charSequence != null && charSequence.length()>0){
            // change to uppercase or lower case to avoid case sensitivity
            charSequence = charSequence.toString().toLowerCase();
            ArrayList<ModelPdf> filteredModels = new ArrayList<>();
            for (int i =0; i<filterList.size();i++){
                // validate
                if(filterList.get(i).getTitle().toLowerCase().contains(charSequence)){
                    // add to filtered list
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else{
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        // apply filter changes
        adapterPdfAdmin.pdfArrayList = (ArrayList<ModelPdf>) filterResults.values;

        //notify changes
        adapterPdfAdmin.notifyDataSetChanged();
    }
}
