package com.example.miniprojet.adapters;

import android.view.Gravity;
import android.widget.LinearLayout;
import com.example.miniprojet.models.User;
import com.google.android.material.color.MaterialColors;
import com.example.miniprojet.Ui;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miniprojet.R;
import com.example.miniprojet.Server;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.ChatItem;

import java.util.ArrayList;

public class ChatItemAdapter extends RecyclerView.Adapter<ChatItemAdapter.ViewHolder> {

    private ArrayList<ChatItem> list;
    private Context ctx;
    public ChatItemAdapter(ArrayList<ChatItem> list, Context ctx) {

        this.ctx = ctx;
        this.list = list;
    }


    public ArrayList<ChatItem> getList() {
        return list;
    }

    public void setList(ArrayList<ChatItem> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatItem item = list.get(position);
        holder.Header.setText(item.getHeader());
        holder.Message.setText(item.getMessage());
        holder.Footer.setText(item.getFooter());
        holder.Avatar.setImageResource(R.drawable.ph_avatar);

        // mes messages à droite, en couleur ; ceux de l'autre à gauche, avec son avatar
        boolean mine = item.getUserID() == User.getInstance(ctx).getID();
        holder.Row.setGravity(mine ? Gravity.END | Gravity.BOTTOM : Gravity.START | Gravity.BOTTOM);
        holder.AvatarCard.setVisibility(mine ? View.GONE : View.VISIBLE);
        holder.Header.setVisibility(View.GONE);
        holder.Message.setBackgroundResource(mine ? R.drawable.bg_bubble_out : R.drawable.bg_bubble_in);
        holder.Message.setTextColor(MaterialColors.getColor(holder.Message, mine ? com.google.android.material.R.attr.colorOnPrimary : com.google.android.material.R.attr.colorOnSurface));


        Bitmap imgAvatar = Server.getBitmap(ctx, "imageClient-["+item.getUserID()+"].jpeg");
        if(imgAvatar != null){
            holder.Avatar.setImageBitmap(imgAvatar);
        }
        else {
            Server.getImage("imageClient-["+item.getUserID()+ "].jpeg", ctx, new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    Bitmap imgAvatar = (Bitmap) args.get(0);
                    if(imgAvatar!=null){
                        holder.Avatar.setImageBitmap(imgAvatar);
                    }
                    else
                        holder.Avatar.setImageResource(R.drawable.ph_avatar);
                }

                @Override
                public void onError(ArrayList args) {
                    holder.Avatar.setImageResource(R.drawable.ph_avatar);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView Message, Header, Footer;
        private ImageView Avatar;
        private LinearLayout Row;
        private View AvatarCard;
        public ViewHolder(View view){
            super(view);
            Message = view.findViewById(R.id.body);
            Header = view.findViewById(R.id.header);
            Footer = view.findViewById(R.id.footer);
            Avatar = view.findViewById(R.id.Avatar);
            Row = view.findViewById(R.id.row);
            AvatarCard = view.findViewById(R.id.cardView_image);
        }
    }
}
