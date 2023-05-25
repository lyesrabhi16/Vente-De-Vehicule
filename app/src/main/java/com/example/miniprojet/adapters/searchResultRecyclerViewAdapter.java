package com.example.miniprojet.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.miniprojet.R;
import com.example.miniprojet.Server;
import com.example.miniprojet.databinding.AnnonceBinding;
import com.example.miniprojet.databinding.FragmentSearchResultItemBinding;
import com.example.miniprojet.databinding.PersonItemBinding;
import com.example.miniprojet.interfaces.RecyclerViewInterface;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.searchResultItem;

import java.util.ArrayList;
import java.util.List;


public class searchResultRecyclerViewAdapter extends RecyclerView.Adapter<searchResultRecyclerViewAdapter.ViewHolder> {


    private List itemList;
    private Context ctx;
    private final RecyclerViewInterface recyclerViewInterface;

    public searchResultRecyclerViewAdapter(List items, Context ctx, RecyclerViewInterface recyclerViewInterface) {
        itemList = items;
        this.ctx = ctx;
        this.recyclerViewInterface = recyclerViewInterface;
    }
    public List getItemList() {
        return itemList;
    }

    public void setItemList(List itemList) {
        this.itemList = itemList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentSearchResultItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), recyclerViewInterface);

    }

    @Override
    public int getItemViewType(int position) {
        return ((searchResultItem)itemList.get(position)).getType();
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        searchResultItem item = (searchResultItem) itemList.get(position);
        if(item.getType() == searchResultItem.RESULT_PERSON){
            holder.containerAnnonce.getRoot().setVisibility(View.GONE);
            holder.containerPerson.getRoot().setVisibility(View.VISIBLE);
            holder.containerPerson.Title.setText(item.getPerson().getTitle());
            holder.containerPerson.subTitle.setText(item.getPerson().getSubTitle());
            holder.containerPerson.Avatar.setImageResource(R.drawable.ic_account_circle_48_bleu_clair);
            Bitmap imgAvatar = Server.getBitmap(ctx, "imageClient-["+item.getPerson().getUserID()+"].jpeg");
            if(imgAvatar != null){
                holder.containerPerson.Avatar.setImageBitmap(imgAvatar);
            }
            else {
                Server.getImage("imageClient-["+item.getPerson().getUserID()+ "].jpeg", ctx, new RequestFinished() {
                    @Override
                    public void onFinish(ArrayList args) {
                        Bitmap imgAvatar = (Bitmap) args.get(0);
                        if(imgAvatar != null)
                            holder.containerPerson.Avatar.setImageBitmap(imgAvatar);
                        else
                            holder.containerPerson.Avatar.setImageResource(R.drawable.ic_account_circle_48_bleu_clair);
                    }

                    @Override
                    public void onError(ArrayList args) {
                        holder.containerPerson.Avatar.setImageResource(R.drawable.ic_account_circle_48_bleu_clair);
                    }
                });
            }
        }
        else{
            holder.containerAnnonce.getRoot().setVisibility(View.VISIBLE);
            holder.containerPerson.getRoot().setVisibility(View.GONE);
            holder.containerAnnonce.image.setImageResource(R.raw.app_logo_rounded_square);
            holder.containerAnnonce.title.setText(item.getAnnonce().getTitle());
            holder.containerAnnonce.type.setText(item.getAnnonce().getType());
            holder.containerAnnonce.qte.setText(item.getAnnonce().getPrix()+"da");
            holder.containerAnnonce.date.setText(item.getAnnonce().getAnnee()+"");
            holder.containerAnnonce.desc.setText(item.getAnnonce().getDesc());
            holder.containerAnnonce.Avatar.setImageResource(R.drawable.ic_account_circle_48);
            holder.containerAnnonce.Title.setText(item.getAnnonce().getUserTitle());
            holder.containerAnnonce.subTitle.setText(item.getAnnonce().getUserSubTitle());
            holder.containerAnnonce.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), "to be implemented later", Toast.LENGTH_SHORT).show();
                }
            });
            Bitmap imgAnnonce = Server.getBitmap(ctx, "imageAnnonce-["+item.getAnnonce().getIdAnnonce()+"].jpeg");
            if(imgAnnonce != null){
                holder.containerAnnonce.image.setImageBitmap(imgAnnonce);
            }
            else {
                Server.getImage("imageAnnonce-[" +item.getAnnonce().getIdAnnonce()+ "].jpeg", ctx, new RequestFinished() {
                    @Override
                    public void onFinish(ArrayList args) {
                        Bitmap imgAnnonce = (Bitmap) args.get(0);
                        holder.containerAnnonce.image.setImageBitmap(imgAnnonce);
                    }

                    @Override
                    public void onError(ArrayList args) {

                    }
                });
            }
            Bitmap imgAvatar = Server.getBitmap(ctx, "imageClient-["+item.getAnnonce().getIdUser()+"].jpeg");
            if(imgAvatar != null){
                holder.containerAnnonce.Avatar.setImageBitmap(imgAvatar);
            }
            else {
                Server.getImage("imageClient-["+item.getAnnonce().getIdUser()+ "].jpeg", ctx, new RequestFinished() {
                    @Override
                    public void onFinish(ArrayList args) {
                        Bitmap imgAvatar = (Bitmap) args.get(0);
                        holder.containerAnnonce.Avatar.setImageBitmap(imgAvatar);
                    }

                    @Override
                    public void onError(ArrayList args) {

                    }
                });
            }
        }

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public PersonItemBinding containerPerson;
        public AnnonceBinding containerAnnonce;

        public ViewHolder(FragmentSearchResultItemBinding binding, RecyclerViewInterface recyclerViewInterface) {
            super(binding.getRoot());
            containerPerson = binding.containerPerson;
            containerAnnonce = binding.containerAnnonce;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerViewInterface.onItemClick(getBindingAdapterPosition());
                }
            });
        }
    }
}