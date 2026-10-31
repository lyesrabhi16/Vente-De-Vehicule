package com.example.miniprojet.adapters;

import com.example.miniprojet.models.User;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.example.miniprojet.Ui;
import com.example.miniprojet.databinding.AnnonceBinding;
import com.example.miniprojet.interfaces.RecyclerViewInterface;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Annonce;
import com.example.miniprojet.ui.Demande.DemandeActivity;
import com.example.miniprojet.ui.annonce.AnnonceActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class AnnonceAdapter extends RecyclerView.Adapter<AnnonceAdapter.ViewHolder> {
    private ArrayList<Annonce> list;
    private Context ctx;
    private final RecyclerViewInterface recyclerViewInterface;
    public AnnonceAdapter(ArrayList<Annonce> list, Context ctx, RecyclerViewInterface recyclerViewInterface) {

        this.list = list;
        this.ctx = ctx;
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
        holder.img.setImageResource(R.drawable.ph_car);
        holder.Title.setText(item.getTitle());
        Ui.typePill(holder.type, item.getType());
        holder.qte.setText(Ui.price(ctx, item.getPrix(), item.getType()));
        holder.date.setText(item.getAnnee());
        holder.desc.setText(item.getDesc());
        holder.button.setText("Location".equals(item.getType()) ? R.string.reserve : R.string.visit);
        // pas de demande sur sa propre annonce
        holder.button.setVisibility(item.getIdUser() == User.getInstance(ctx).getID() ? View.GONE : View.VISIBLE);
        holder.userAvatar.setImageResource(R.drawable.ph_avatar);
        holder.userTitle.setText(item.getUserTitle());
        holder.userSubTitle.setText(item.getUserSubTitle());
        holder.userSubTitle.setVisibility(item.getUserSubTitle() == null || item.getUserSubTitle().isEmpty() ? View.GONE : View.VISIBLE);

        holder.itemView.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx == null) return;
                Intent annonce = new Intent(ctx, AnnonceActivity.class);
                annonce.putExtra("idAnnonce", item.getIdAnnonce());
                ctx.startActivity(annonce);
            }
        });
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent demande = new Intent(ctx, DemandeActivity.class);
                demande.putExtra("idAnnonce",item.getIdAnnonce());
                ctx.startActivity(demande);
                /*if(item.getType().equals("Vente")){
                    RendezVous rendezVous = new RendezVous(
                            User.getInstance(ctx).getID(),
                            item.getIdAnnonce(),
                            "NULL",
                            "NULL",
                            "Pending"
                    );
                    RendezVous.AddRendezVous(rendezVous, ctx, new RequestFinished() {
                        @Override
                        public void onFinish(ArrayList args) {
                            Toast.makeText(ctx, "Rendez-vous demandé.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(ArrayList args) {
                            Toast.makeText(ctx, "Échec de la demande de rendez-vous.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (item.getType().equals("Location")) {
                    Reservation reservation = new Reservation(
                            User.getInstance(ctx).getID(),
                            item.getIdAnnonce(),
                            "NULL",
                            "NULL",
                            "NULL",
                            "Pending"
                    );
                    Reservation.AddReservation(reservation, ctx, new RequestFinished() {
                        @Override
                        public void onFinish(ArrayList args) {
                            Toast.makeText(ctx, "Réservation demandée.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(ArrayList args) {
                            Toast.makeText(ctx, "Échec de la demande de réservation.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    Toast.makeText(ctx, "Type d’annonce inconnu", Toast.LENGTH_SHORT).show();
                }*/
            }
        });

        Bitmap imgAnnonce = Server.getBitmap(ctx, "imageAnnonce-["+item.getIdAnnonce()+"].jpeg");
        if(imgAnnonce != null){
            holder.img.setImageBitmap(imgAnnonce);
        }
        else {
            Server.getImage("imageAnnonce-[" +item.getIdAnnonce()+ "].jpeg", ctx, new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    Bitmap imgAnnonce = (Bitmap) args.get(0);
                    holder.img.setImageBitmap(imgAnnonce);
                }

                @Override
                public void onError(ArrayList args) {

                }
            });
        }
        Bitmap imgAvatar = Server.getBitmap(ctx, "imageClient-["+item.getIdUser()+"].jpeg");
        if(imgAvatar != null){
            holder.userAvatar.setImageBitmap(imgAvatar);
        }
        else {
            Server.getImage("imageClient-["+item.getIdUser()+ "].jpeg", ctx, new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    Bitmap imgAvatar = (Bitmap) args.get(0);
                    holder.userAvatar.setImageBitmap(imgAvatar);
                }

                @Override
                public void onError(ArrayList args) {

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        private MaterialButton button;
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
            button = bind.button;

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerViewInterface.onItemClick(getBindingAdapterPosition());
                }
            });
        }

    }
}
