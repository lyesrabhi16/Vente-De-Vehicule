package com.example.miniprojet.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miniprojet.R;
import com.example.miniprojet.databinding.AnnonceBinding;
import com.example.miniprojet.interfaces.RecyclerViewInterface;
import com.example.miniprojet.models.Annonce;

import java.util.ArrayList;

public class AnnonceAdapter extends RecyclerView.Adapter<AnnonceAdapter.ViewHolder> {
    private ArrayList<Annonce> list;
    private final RecyclerViewInterface recyclerViewInterface;
    public AnnonceAdapter(ArrayList<Annonce> list, RecyclerViewInterface recyclerViewInterface) {

        this.list = list;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    public ArrayList<Annonce> getList() {
        return list;
    }

    public void setList(ArrayList<Annonce> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AnnonceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.annonce, parent, false), recyclerViewInterface);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull AnnonceAdapter.ViewHolder holder, int position) {
        Annonce item = list.get(position);
        holder.img.setImageResource(R.raw.app_logo_rounded_square);
        holder.Title.setText(item.getTitle());
        holder.type.setText(item.getType());
        holder.qte.setText(item.getQte());
        holder.date.setText(item.getDate());
        holder.desc.setText(item.getDesc());
        holder.userAvatar.setImageResource(R.drawable.ic_account_circle_48);
        holder.userTitle.setText(item.getUserTitle());
        holder.userSubTitle.setText(item.getUserSubTitle());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView img, userAvatar;
        private TextView Title, type, qte, date, desc, userTitle, userSubTitle;
        public ViewHolder(View view, RecyclerViewInterface recyclerViewInterface){

            super(view);
            AnnonceBinding bind = AnnonceBinding.bind(view);
            img = view.findViewById(bind.image.getId());
            Title = view.findViewById(bind.title.getId());
            type = view.findViewById(bind.type.getId());
            qte = view.findViewById(bind.qte.getId());
            date = view.findViewById(bind.date.getId());
            desc = view.findViewById(bind.desc.getId());
            userTitle = view.findViewById(bind.Title.getId());
            userSubTitle = view.findViewById(bind.subTitle.getId());
            userAvatar = view.findViewById(bind.Avatar.getId());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerViewInterface.onItemClick(getBindingAdapterPosition());
                }
            });
        }

    }
}
