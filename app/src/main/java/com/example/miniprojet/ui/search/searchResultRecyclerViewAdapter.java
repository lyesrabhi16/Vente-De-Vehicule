package com.example.miniprojet.ui.search;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.miniprojet.R;
import com.example.miniprojet.databinding.FragmentSearchResultItemBinding;

import java.util.List;


public class searchResultRecyclerViewAdapter extends RecyclerView.Adapter<searchResultRecyclerViewAdapter.ViewHolder> {

    private final List<searchResultItem> itemList;

    public searchResultRecyclerViewAdapter(List<searchResultItem> items) {
        itemList = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentSearchResultItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        searchResultItem item = itemList.get(position);
        holder.Title.setText(item.getTitle());
        holder.subTitle.setText(item.getSubTitle());
        holder.img.setImageResource(R.drawable.ic_account_circle_48);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView Title;
        public TextView subTitle;
        public ImageView img;

        public ViewHolder(FragmentSearchResultItemBinding binding) {
            super(binding.getRoot());
            Title = binding.Title;
            subTitle = binding.subTitle;
            img = binding.AccountImageView;
        }
    }
}