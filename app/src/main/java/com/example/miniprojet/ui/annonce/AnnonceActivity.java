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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class AnnonceActivity extends AppCompatActivity {

    private ActivityAnnonceBinding BAnnonce;
    private int idAnnonce;
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

        if(idAnnonce != -1){

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