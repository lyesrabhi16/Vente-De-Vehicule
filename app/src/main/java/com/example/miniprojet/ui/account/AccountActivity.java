package com.example.miniprojet.ui.account;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniprojet.databinding.ActivityAccountBinding;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Client;

import java.util.ArrayList;

public class AccountActivity extends AppCompatActivity {

    private ActivityAccountBinding ABind;
    private int accountID;

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
                    Client c = (Client) args.get(0);
                    ABind.nom.setText(c.getNom());
                    ABind.prenom.setText(c.getPrenom());
                    ABind.age.setText(c.getAge()+"");
                    ABind.email.setText(c.getEmail());
                    ABind.numTel.setText(c.getNumTel());
                }

                @Override
                public void onError(ArrayList args) {
                    prgrs.dismiss();
                }
            });
        }

    }
}