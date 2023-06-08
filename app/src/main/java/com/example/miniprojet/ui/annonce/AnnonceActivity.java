package com.example.miniprojet.ui.annonce;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.miniprojet.Server;
import com.example.miniprojet.databinding.ActivityAnnonceBinding;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Annonce;
import com.example.miniprojet.models.Client;
import com.example.miniprojet.models.User;
import com.example.miniprojet.ui.account.AccountActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class AnnonceActivity extends AppCompatActivity {

    private ActivityAnnonceBinding BAnnonce;
    private int idAnnonce;
    private Annonce annonce;
    private Client client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        BAnnonce = ActivityAnnonceBinding.inflate(getLayoutInflater());
        setContentView(BAnnonce.getRoot());

        idAnnonce = getIntent().getIntExtra("idAnnonce",-1);
        BAnnonce.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        BAnnonce.buttonSupprimer.setVisibility(View.GONE);
        BAnnonce.buttonSupprimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AnnonceActivity.this, "to be implemented later", Toast.LENGTH_SHORT).show();
            }
        });
        BAnnonce.buttonReserver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AnnonceActivity.this, "to be implemented later", Toast.LENGTH_SHORT).show();
            }
        });
        BAnnonce.buttonDemanderRendezvous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AnnonceActivity.this, "to be implemented later", Toast.LENGTH_SHORT).show();
            }
        });

        BAnnonce.containerPerson.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(client==null) return;
                Intent account = new Intent(getApplicationContext(), AccountActivity.class);
                account.putExtra("accountID", client.getIdClient());
                startActivity(account);
            }
        });

        if(idAnnonce != -1){

            Annonce.getAnnonce(idAnnonce, getApplicationContext(), new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    if(args.size()<1)return;
                    annonce = (Annonce) args.get(0);
                    client = (Client) args.get(1);
                    if(BAnnonce != null){
                        BAnnonce.titre.setText(annonce.getTitle());
                        BAnnonce.desc.setText(annonce.getDesc());
                        BAnnonce.type.setText(annonce.getType());
                        BAnnonce.marque.setText(annonce.getMarque());
                        BAnnonce.modele.setText(annonce.getModele());
                        BAnnonce.couleur.setText(annonce.getCouleur());
                        BAnnonce.transmission.setText(annonce.getTransmission());
                        BAnnonce.kilometrage.setText(annonce.getKilometrage());
                        BAnnonce.year.setText(annonce.getAnnee()+"");
                        BAnnonce.moteur.setText(annonce.getMoteur());
                        BAnnonce.energie.setText(annonce.getEnergie());
                        BAnnonce.prix.setText(annonce.getPrix());
                        BAnnonce.containerPerson.Title.setText(annonce.getUserTitle());
                        BAnnonce.containerPerson.subTitle.setText(annonce.getUserSubTitle());
                        Server.getImage("imageClient-[" + client.getIdClient() + "].jpeg", getApplicationContext(), new RequestFinished() {
                            @Override
                            public void onFinish(ArrayList args) {
                                Bitmap img = (Bitmap) args.get(0);
                                if(BAnnonce!=null && img!=null){
                                    BAnnonce.containerPerson.Avatar.setImageBitmap(img);
                                }
                            }

                            @Override
                            public void onError(ArrayList args) {

                            }
                        });

                        if(client.getIdClient() == User.getInstance(getApplicationContext()).getID() || User.getInstance(getApplicationContext()).isAdmin()){
                            BAnnonce.imageAnnonce.setClickable(true);
                            BAnnonce.titre.setEnabled(true);
                            BAnnonce.desc.setEnabled(true);
                            BAnnonce.type.setEnabled(true);
                            BAnnonce.marque.setEnabled(true);
                            BAnnonce.modele.setEnabled(true);
                            BAnnonce.couleur.setEnabled(true);
                            BAnnonce.transmission.setEnabled(true);
                            BAnnonce.kilometrage.setEnabled(true);
                            BAnnonce.year.setEnabled(true);
                            BAnnonce.moteur.setEnabled(true);
                            BAnnonce.energie.setEnabled(true);
                            BAnnonce.prix.setEnabled(true);
                            BAnnonce.buttonSupprimer.setVisibility(View.VISIBLE);
                            BAnnonce.buttonReserver.setVisibility(View.GONE);
                            BAnnonce.buttonDemanderRendezvous.setVisibility(View.GONE);
                        }
                        else{
                            BAnnonce.imageAnnonce.setClickable(false);
                            BAnnonce.titre.setEnabled(false);
                            BAnnonce.desc.setEnabled(false);
                            BAnnonce.type.setEnabled(false);
                            BAnnonce.marque.setEnabled(false);
                            BAnnonce.modele.setEnabled(false);
                            BAnnonce.couleur.setEnabled(false);
                            BAnnonce.transmission.setEnabled(false);
                            BAnnonce.kilometrage.setEnabled(false);
                            BAnnonce.year.setEnabled(false);
                            BAnnonce.moteur.setEnabled(false);
                            BAnnonce.energie.setEnabled(false);
                            BAnnonce.prix.setEnabled(false);
                            BAnnonce.buttonSupprimer.setVisibility(View.GONE);
                            BAnnonce.buttonReserver.setVisibility(View.VISIBLE);
                            BAnnonce.buttonDemanderRendezvous.setVisibility(View.VISIBLE);
                        }


                    }
                }

                @Override
                public void onError(ArrayList args) {

                }
            });

            Bitmap img = Server.getBitmap(getApplicationContext(), "imageAnnonce-["+idAnnonce+"].jpeg");
            if(img!=null){
                BAnnonce.imageAnnonce.setImageBitmap(img);
            }
            else{
                Server.getImage("imageAnnonce-[" + idAnnonce + "].jpeg", getApplicationContext(), new RequestFinished() {
                    @Override
                    public void onFinish(ArrayList args) {
                        Bitmap img = (Bitmap) args.get(0);
                        BAnnonce.imageAnnonce.setImageBitmap(img);
                    }

                    @Override
                    public void onError(ArrayList args) {

                    }
                });
            }

            ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedImageUri = data.getData();
                            try {
                                Bitmap img = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                                Server.saveBitmap(getApplicationContext(), "imageAnnonce-["+idAnnonce+"]", img);
                                JSONObject o = new JSONObject();
                                o.put("imgB64", Server.ImageToBase64(img, getApplicationContext()));
                                o.put("id", idAnnonce);
                                o.put("format", "jpeg");
                                o.put("type", Server.TYPE_IMAGE_ANNONCE);
                                Server.sendImageToserver(o, getApplicationContext());
                                BAnnonce.imageAnnonce.setImageURI(selectedImageUri);
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), "Error : "+e, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), "Error : "+e, Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                }
            });
            BAnnonce.imageAnnonce.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryLauncher.launch(gallery);
                }
            });
        }
        else{
            Toast.makeText(this, "unrecognized announcement", Toast.LENGTH_SHORT).show();
        }

    }
}