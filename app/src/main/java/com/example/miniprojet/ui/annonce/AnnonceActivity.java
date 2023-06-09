package com.example.miniprojet.ui.annonce;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.miniprojet.R;
import com.example.miniprojet.Server;
import com.example.miniprojet.databinding.ActivityAnnonceBinding;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Annonce;
import com.example.miniprojet.models.Client;
import com.example.miniprojet.models.User;
import com.example.miniprojet.ui.Demande.DemandeActivity;
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
    private ArrayList changed;

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

        BAnnonce.buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String titre = BAnnonce.titre.getText().toString().trim();
                String desc = BAnnonce.desc.getText().toString().trim();
                String type = BAnnonce.type.getText().toString().trim();
                String marque = BAnnonce.marque.getText().toString().trim();
                String modele = BAnnonce.modele.getText().toString().trim();
                String couleur = BAnnonce.couleur.getText().toString().trim();
                String transmission = BAnnonce.transmission.getText().toString().trim();
                String kilometrage = BAnnonce.kilometrage.getText().toString().trim();
                String annee = BAnnonce.year.getText().toString().trim();
                String moteur = BAnnonce.moteur.getText().toString().trim();
                String energie = BAnnonce.energie.getText().toString().trim();
                String prix = BAnnonce.prix.getText().toString().trim();

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

                Annonce.updateAnnonce(annonce, getApplicationContext(), new RequestFinished() {
                    @Override
                    public void onFinish(ArrayList args) {

                    }

                    @Override
                    public void onError(ArrayList args) {

                    }
                });
            }
        });
        BAnnonce.buttonSupprimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Annonce.delAnnonce(annonce.getIdAnnonce(), getApplicationContext(), new RequestFinished() {
                    @Override
                    public void onFinish(ArrayList args) {
                        finish();
                    }

                    @Override
                    public void onError(ArrayList args) {

                    }
                });
            }
        });
        BAnnonce.buttonReserver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(annonce == null) return;
                Intent demande = new Intent(getApplicationContext(), DemandeActivity.class);
                demande.putExtra("idAnnonce", annonce.getIdAnnonce());
                demande.putExtra("TYPE",DemandeActivity.TYPE_RESERVATION);
                startActivity(demande);
            }
        });
        BAnnonce.buttonDemanderRendezvous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(annonce == null) return;
                Intent demande = new Intent(getApplicationContext(), DemandeActivity.class);
                demande.putExtra("idAnnonce", annonce.getIdAnnonce());
                demande.putExtra("TYPE",DemandeActivity.TYPE_RENDEZVOUS);
                startActivity(demande);
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

                            BAnnonce.type.setSimpleItems(getResources().getStringArray(R.array.types_actions));
                            BAnnonce.marque.setSimpleItems(getResources().getStringArray(R.array.marque));
                            BAnnonce.couleur.setSimpleItems(getResources().getStringArray(R.array.couleur));
                            BAnnonce.transmission.setSimpleItems(getResources().getStringArray(R.array.boite));
                            BAnnonce.energie.setSimpleItems(getResources().getStringArray(R.array.energie));


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



            changed = new ArrayList();
            BAnnonce.titre.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ShowOrHideSave_Manager( String.join("", charSequence), annonce.getTitle(),BAnnonce.titre.getId());
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ShowOrHideSave_Manager(editable.toString(), annonce.getTitle(),BAnnonce.titre.getId());

                }
            });
            BAnnonce.desc.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ShowOrHideSave_Manager(String.join("", charSequence), annonce.getDesc(),BAnnonce.desc.getId());

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ShowOrHideSave_Manager(editable.toString(), annonce.getDesc(),BAnnonce.desc.getId());

                }
            });
            BAnnonce.type.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ShowOrHideSave_Manager(String.join("", charSequence), annonce.getType(),BAnnonce.type.getId());

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ShowOrHideSave_Manager(editable.toString(), annonce.getType(),BAnnonce.type.getId());

                }
            });
            BAnnonce.marque.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ShowOrHideSave_Manager(String.join("", charSequence), annonce.getMarque(),BAnnonce.marque.getId());

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ShowOrHideSave_Manager(editable.toString(), annonce.getMarque(),BAnnonce.marque.getId());

                }
            });
            BAnnonce.modele.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ShowOrHideSave_Manager(String.join("", charSequence), annonce.getModele(),BAnnonce.modele.getId());

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ShowOrHideSave_Manager(editable.toString(), annonce.getModele(),BAnnonce.modele.getId());
                }
            });

            BAnnonce.couleur.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ShowOrHideSave_Manager(String.join("", charSequence), annonce.getCouleur(),BAnnonce.couleur.getId());

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ShowOrHideSave_Manager(editable.toString(), annonce.getCouleur(),BAnnonce.couleur.getId());
                }
            });
            BAnnonce.transmission.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ShowOrHideSave_Manager(String.join("", charSequence), annonce.getTransmission(),BAnnonce.transmission.getId());

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ShowOrHideSave_Manager(editable.toString(), annonce.getTransmission(),BAnnonce.transmission.getId());
                }
            });
            BAnnonce.kilometrage.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ShowOrHideSave_Manager(String.join("", charSequence), annonce.getKilometrage(),BAnnonce.kilometrage.getId());

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ShowOrHideSave_Manager(editable.toString(), annonce.getKilometrage(),BAnnonce.kilometrage.getId());
                }
            });

            //
            BAnnonce.year.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ShowOrHideSave_Manager(String.join("", charSequence), annonce.getAnnee(),BAnnonce.year.getId());

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ShowOrHideSave_Manager(editable.toString(), annonce.getAnnee(),BAnnonce.year.getId());
                }
            });

            BAnnonce.moteur.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ShowOrHideSave_Manager(String.join("", charSequence), annonce.getMoteur(),BAnnonce.moteur.getId());

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ShowOrHideSave_Manager(editable.toString(), annonce.getMoteur(),BAnnonce.moteur.getId());
                }
            });
            BAnnonce.energie.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ShowOrHideSave_Manager(String.join("", charSequence), annonce.getEnergie(),BAnnonce.energie.getId());

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ShowOrHideSave_Manager(editable.toString(), annonce.getEnergie(),BAnnonce.energie.getId());
                }
            });
            BAnnonce.prix.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ShowOrHideSave_Manager(String.join("", charSequence), annonce.getPrix(),BAnnonce.prix.getId());

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ShowOrHideSave_Manager(editable.toString(), annonce.getPrix(),BAnnonce.prix.getId());
                }
            });

            ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            ClipData clipData = data.getClipData();
                            if(clipData != null){
                                for (int i = 0; i < clipData.getItemCount(); i++) {
                                    Uri selectedImageUri = clipData.getItemAt(i).getUri();
                                    Toast.makeText(AnnonceActivity.this, i+ " uri : " +selectedImageUri, Toast.LENGTH_SHORT).show();
                                    try {
                                        Bitmap img = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                                        Server.saveBitmap(getApplicationContext(), "imageAnnonce-["+idAnnonce+"]-["+i+"]", img);
                                        JSONObject o = new JSONObject();
                                        o.put("imgB64", Server.ImageToBase64(img, getApplicationContext()));
                                        o.put("id", idAnnonce);
                                        o.put("format", "jpeg");
                                        o.put("type", Server.TYPE_IMAGE_ANNONCE);
                                        o.put("index", i);
                                        Server.sendImageToserver(o, getApplicationContext());
                                        if(i == 0)
                                            BAnnonce.imageAnnonce.setImageURI(selectedImageUri);
                                    } catch (IOException e) {
                                        Toast.makeText(getApplicationContext(), "Error : "+e, Toast.LENGTH_SHORT).show();
                                    } catch (JSONException e) {
                                        Toast.makeText(getApplicationContext(), "Error : "+e, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            else{
                                Uri selectedImageUri = data.getData();
                                try {
                                    Bitmap img = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                                    Server.saveBitmap(getApplicationContext(), "imageAnnonce-["+idAnnonce+"]-[0]", img);
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
                }
            });

            BAnnonce.imageAnnonce.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent gallery = new Intent();
                    gallery.setType("image/*");
                    gallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    gallery.setAction(Intent.ACTION_GET_CONTENT);
                    galleryLauncher.launch(gallery);
                }
            });
        }
        else{
            Toast.makeText(this, "unrecognized announcement", Toast.LENGTH_SHORT).show();
        }

    }
    public void ShowOrHideSave_Manager(String text, String original, int id){
        if(text.equals(original)){
            if(changed.contains(id+"")){
                changed.remove(id+"");
            }
        }
        else{
            changed.add(id+"");
        }
        ShowOrHideSaveBtn();
    }
    public void ShowOrHideSaveBtn(){
        if(BAnnonce == null)return;
        if(changed.size()>=1){
            BAnnonce.buttonSave.setVisibility(View.VISIBLE);
        }
        else{
            BAnnonce.buttonSave.setVisibility(View.GONE);
        }
    }
}