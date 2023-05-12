package com.example.miniprojet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.miniprojet.R;
import com.example.miniprojet.databinding.FragmentSearchResultItemBinding;
import com.example.miniprojet.interfaces.RecyclerViewInterface;
import com.example.miniprojet.models.searchResultItem;

import java.util.List;


public class searchResultRecyclerViewAdapter extends RecyclerView.Adapter<searchResultRecyclerViewAdapter.ViewHolder> {

    private final List<searchResultItem> itemList;
    private final RecyclerViewInterface recyclerViewInterface;

    public searchResultRecyclerViewAdapter(List<searchResultItem> items, RecyclerViewInterface recyclerViewInterface) {
        itemList = items;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentSearchResultItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), recyclerViewInterface);

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

        public ViewHolder(FragmentSearchResultItemBinding binding, RecyclerViewInterface recyclerViewInterface) {
            super(binding.getRoot());
            Title = binding.Title;
            subTitle = binding.subTitle;
            img = binding.AccountImageView;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerViewInterface.onItemClick(getBindingAdapterPosition());
                }
            });
        }
    }
}