package com.example.miniprojet.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miniprojet.R;
import com.example.miniprojet.Server;
import com.example.miniprojet.databinding.AnnonceBinding;
import com.example.miniprojet.databinding.DemandeBinding;
import com.example.miniprojet.databinding.PersonItemBinding;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Annonce;
import com.example.miniprojet.models.Client;
import com.example.miniprojet.models.Demande;
import com.example.miniprojet.models.RendezVous;
import com.example.miniprojet.models.Reservation;

import java.util.ArrayList;

public class DemandeAdapter extends RecyclerView.Adapter<DemandeAdapter.ViewHolder> {
    private ArrayList<Demande> list;
    private Context ctx;
    public DemandeAdapter(ArrayList<Demande> list, Context ctx) {

        this.ctx = ctx;
        this.list = list;
    }


    public ArrayList<Demande> getList() {
        return list;
    }

    public void setList(ArrayList<Demande> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public DemandeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DemandeAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.demande, parent, false));
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull DemandeAdapter.ViewHolder holder, int position) {
        Demande item = list.get(position);

        holder.title.setText(item.getTitle());
        holder.etat.setText(item.getEtat());
        holder.containerPerson.Title.setText(item.getPerson().getNom() + " " + item.getPerson().getPrenom() +" ");
        holder.containerPerson.subTitle.setText(item.getPerson().getEmail());
        holder.containerPerson.Avatar.setImageResource(R.drawable.ic_account_circle_48_bleu_clair);

        holder.containerAnnonce.image.setImageResource(R.raw.app_logo_rounded_square);
        holder.containerAnnonce.title.setText(item.getAnnonce().getTitle());
        holder.containerAnnonce.type.setText(item.getAnnonce().getType());
        holder.containerAnnonce.qte.setText(item.getAnnonce().getPrix()+"da");
        holder.containerAnnonce.date.setText(item.getAnnonce().getAnnee()+"");
        holder.containerAnnonce.desc.setText(item.getAnnonce().getDesc());
        holder.containerAnnonce.Avatar.setImageResource(R.drawable.ic_account_circle_48);
        holder.containerAnnonce.Title.setText(item.getAnnonce().getUserTitle());
        holder.containerAnnonce.subTitle.setText(item.getAnnonce().getUserSubTitle());

        Bitmap imgAvatarPerson = Server.getBitmap(ctx, "imageClient-["+item.getPerson().getIdClient()+"].jpeg");
        if(imgAvatarPerson != null){
            holder.containerPerson.Avatar.setImageBitmap(imgAvatarPerson);
        }
        else {
            Server.getImage("imageClient-["+item.getPerson().getIdClient()+ "].jpeg", ctx, new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    Bitmap imgAvatar = (Bitmap) args.get(0);
                    if(imgAvatar!=null){
                        holder.containerPerson.Avatar.setImageBitmap(imgAvatar);
                    }
                    else
                        holder.containerPerson.Avatar.setImageResource(R.drawable.ic_account_circle_48_bleu_clair);
                }

                @Override
                public void onError(ArrayList args) {
                    holder.containerPerson.Avatar.setImageResource(R.drawable.ic_account_circle_48_bleu_clair);
                }
            });
        }

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

    @Override
    public int getItemCount() {
        return list.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView title, etat;
        private PersonItemBinding containerPerson;
        private AnnonceBinding containerAnnonce;
        public ViewHolder(View view){
            super(view);
            DemandeBinding bind = DemandeBinding.bind(view);
            title = bind.title;
            etat = bind.etat;
            containerPerson = bind.containerPerson;
            containerAnnonce = bind.containerAnnonce;
        }
    }
    public void addWithRendezVousList(ArrayList<RendezVous> rendezVousList){
        for (int i = 0; i < rendezVousList.size(); i++) {
            RendezVous rendezVous = rendezVousList.get(i);
            Demande d = new Demande();
            d.setTitle("RendezVous");
            d.setEtat(rendezVous.getEtatRendezVous());
            Client.getClient(rendezVous.getIdClient(), ctx, new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    Client c = (Client) args.get(0);
                    d.setPerson(c);
                    Annonce.getAnnonce(rendezVous.getIdAnnonce(), ctx, new RequestFinished() {
                        @Override
                        public void onFinish(ArrayList args) {
                            Annonce annonce = (Annonce) args.get(0);
                            d.setAnnonce(annonce);
                            Boolean add = true;
                            for (int i = 0; i < list.size(); i++) {
                                Demande  d1 = list.get(i);
                                if(d1.getAnnonce().getIdAnnonce() == d.getAnnonce().getIdAnnonce())
                                    add = false;
                            }
                            if(add){
                                list.add(d);
                                notifyItemInserted(list.size()-1);
                            }

                        }

                        @Override
                        public void onError(ArrayList args) {

                        }
                    });
                }

                @Override
                public void onError(ArrayList args) {

                }
            });


        }
    }
    public void addWithReservationsList(ArrayList<Reservation> reservations){
        for (int i = 0; i < reservations.size(); i++) {
            Reservation reservation = reservations.get(i);
            Demande d = new Demande();
            d.setTitle("Reservation");
            d.setEtat(reservation.getEtatReservation());
            Client.getClient(reservations.get(i).getIdClient(), ctx, new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    Client c = (Client) args.get(0);
                    d.setPerson(c);
                    Annonce.getAnnonce(reservation.getIdAnnonce(), ctx, new RequestFinished() {
                        @Override
                        public void onFinish(ArrayList args) {
                            Annonce annonce = (Annonce) args.get(0);
                            d.setAnnonce(annonce);
                            Boolean add = true;
                            for (int i = 0; i < list.size(); i++) {
                                Demande  d1 = list.get(i);
                                if(d1.getAnnonce().getIdAnnonce() == d.getAnnonce().getIdAnnonce())
                                    add = false;
                            }
                            if(add){
                                list.add(d);
                                notifyItemInserted(list.size()-1);
                            }


                        }

                        @Override
                        public void onError(ArrayList args) {

                        }
                    });
                }

                @Override
                public void onError(ArrayList args) {

                }
            });


        }
    }
}
