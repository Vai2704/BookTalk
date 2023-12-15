package com.example.booktalk.Filters;

import android.widget.Filter;

import com.example.booktalk.Adapters.AdapterCategory;
import com.example.booktalk.Models.ModelCategory;

import java.util.ArrayList;

public class FilterCategory extends Filter {

    // arraylist in which we want to search
    ArrayList<ModelCategory> filterList;
    //adapter in which filter need to be implemented
    AdapterCategory adapterCategory;

    // constructors

    public FilterCategory(ArrayList<ModelCategory> filterList, AdapterCategory adapterCategory) {
        this.filterList = filterList;
        this.adapterCategory = adapterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        //value should not be null and empty
        if(charSequence != null && charSequence.length()>0){
            // change to uppercase or lower case to avoid case sensitivity
            charSequence = charSequence.toString().toLowerCase();
            ArrayList<ModelCategory> filteredModels = new ArrayList<>();
            for (int i =0; i<filterList.size();i++){
                // validate
                if(filterList.get(i).getCategory().toLowerCase().contains(charSequence)){
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
        adapterCategory.list = (ArrayList<ModelCategory>) filterResults.values;

        //notify changes
        adapterCategory.notifyDataSetChanged();
    }
}
