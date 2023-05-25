package com.example.miniprojet.ui.annonce;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniprojet.databinding.ActivityAddAnnonceBinding;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Annonce;
import com.example.miniprojet.models.User;

import java.util.ArrayList;

public class AddAnnonceActivity extends AppCompatActivity {
    private ActivityAddAnnonceBinding BAddA;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BAddA = ActivityAddAnnonceBinding.inflate(getLayoutInflater());
        setContentView(BAddA.getRoot());
        getSupportActionBar().hide();

        BAddA.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        User user = User.getInstance(getApplicationContext());
        if (user.isLoggedin()){
            BAddA.main.setVisibility(View.VISIBLE);
            BAddA.loginRequest.setVisibility(View.GONE);
            BAddA.buttonAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Annonce annonce = new Annonce();
                    String titre = BAddA.titre.getText().toString().trim();
                    String desc = BAddA.desc.getText().toString().trim();
                    String type = BAddA.type.getText().toString().trim();
                    String marque = BAddA.marque.getText().toString().trim();
                    String modele = BAddA.modele.getText().toString().trim();
                    String couleur = BAddA.couleur.getText().toString().trim();
                    String transmission = BAddA.transmission.getText().toString().trim();
                    String kilometrage = BAddA.kilometrage.getText().toString().trim();
                    String annee = BAddA.year.getText().toString().trim();
                    String moteur = BAddA.moteur.getText().toString().trim();
                    String energie = BAddA.energie.getText().toString().trim();
                    String prix = BAddA.prix.getText().toString().trim();

                    annonce.setIdUser(User.getInstance(getApplicationContext()).getID());
                    annonce.setTitle(titre);
                    annonce.setDesc(desc);
                    annonce.setType(type);
                    annonce.setMarque(marque);
                    annonce.setModele(modele);
                    annonce.setCouleur(couleur);
                    annonce.setTransmission(transmission);
                    annonce.setKilometrage(kilometrage);
                    annonce.setAnnee(annee);
                    annonce.setMoteur(moteur);
                    annonce.setEnergie(energie);
                    annonce.setPrix(prix);

                    Annonce.AddAnnonce(annonce, getApplicationContext(), new RequestFinished() {
                        @Override
                        public void onFinish(ArrayList args) {
                            Toast.makeText(AddAnnonceActivity.this, "Annonce Added.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(ArrayList args) {
                        }
                    });

                }
            });
        }
        else{
            BAddA.main.setVisibility(View.GONE);
            BAddA.loginRequest.setVisibility(View.VISIBLE);
        }


    }
}