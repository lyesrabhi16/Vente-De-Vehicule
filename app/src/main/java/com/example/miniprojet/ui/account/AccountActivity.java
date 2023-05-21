package com.example.miniprojet.ui.account;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniprojet.ui.annonce.AnnoncesActivity;
import com.example.miniprojet.databinding.ActivityAccountBinding;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Client;
import com.example.miniprojet.ui.messages.chat.ChatActivity;

import java.util.ArrayList;

public class AccountActivity extends AppCompatActivity {

    private ActivityAccountBinding ABind;
    private int accountID;
    private Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        ABind = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(ABind.getRoot());

        ABind.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        accountID = getIntent().getIntExtra("accountID", -1);
        if(accountID != -1){
            ProgressDialog prgrs = new ProgressDialog(AccountActivity.this);
            prgrs.setMessage("Getting user info...");
            prgrs.show();

            Client.getClient(accountID, getApplicationContext(), new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    prgrs.dismiss();
                    client = (Client) args.get(0);
                    ABind.nom.setText(client.getNom());
                    ABind.prenom.setText(client.getPrenom());
                    ABind.age.setText(client.getAge()+"");
                    ABind.email.setText(client.getEmail());
                    ABind.numTel.setText(client.getNumTel());
                }

                @Override
                public void onError(ArrayList args) {
                    prgrs.dismiss();
                }
            });
            ABind.buttonSendMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent chat = new Intent(getApplicationContext(), ChatActivity.class);
                    chat.putExtra("userID", client.getIdClient());
                    chat.putExtra("accountName", client.getNom()+" "+ client.getPrenom());
                    startActivity(chat);
                }
            });
            ABind.buttonAnnonces.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent annonces = new Intent(getApplicationContext(), AnnoncesActivity.class);
                    annonces.putExtra("accountID", client.getIdClient());
                    startActivity(annonces);
                }
            });
        }

    }
}