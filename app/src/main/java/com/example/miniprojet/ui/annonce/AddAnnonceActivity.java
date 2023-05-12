package com.example.miniprojet.ui.annonce;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniprojet.R;
import com.example.miniprojet.databinding.ActivityAddAnnonceBinding;

public class AddAnnonceActivity extends AppCompatActivity {
    private ActivityAddAnnonceBinding BAddA;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_annonce);
        getSupportActionBar().hide();

        BAddA = ActivityAddAnnonceBinding.inflate(getLayoutInflater());
        BAddA.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}