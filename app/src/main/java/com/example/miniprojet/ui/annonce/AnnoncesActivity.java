package com.example.miniprojet.ui.annonce;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniprojet.R;
import com.example.miniprojet.adapters.AnnonceAdapter;
import com.example.miniprojet.databinding.ActivityAnnoncesBinding;
import com.example.miniprojet.interfaces.RecyclerViewInterface;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Annonce;
import com.example.miniprojet.models.Client;

import java.util.ArrayList;

public class AnnoncesActivity extends AppCompatActivity {

    private ActivityAnnoncesBinding binding;
    private Client client;
    private ArrayList<Annonce> annonces;
    private AnnonceAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivityAnnoncesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        annonces = new ArrayList<Annonce>();
        adapter = new AnnonceAdapter(annonces, getApplicationContext(), new RecyclerViewInterface() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(AnnoncesActivity.this, "item clicked", Toast.LENGTH_SHORT).show();
            }
        });

        int accountID = getIntent().getIntExtra("accountID", -1);
        if (accountID != -1){
            binding.progressBar.setVisibility(View.VISIBLE);
            Client.getClient(accountID, getApplicationContext(), new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    client = (Client) args.get(0);
                    Annonce.getUserAnnonces(client.getIdClient(), getApplicationContext(), new RequestFinished() {
                        @Override
                        public void onFinish(ArrayList args) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.main.setVisibility(View.VISIBLE);
                            binding.recyclerAnnonces.setVisibility(View.VISIBLE);
                            annonces = (ArrayList<Annonce>) args;
                            adapter.setList(annonces);
                            binding.recyclerAnnonces.setAdapter(adapter);
                            if(annonces.size() < 1){
                                Toast.makeText(AnnoncesActivity.this, getResources().getText(R.string.no_annoncements), Toast.LENGTH_SHORT).show();
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
        else{
            Toast.makeText(this, "user not recognized", Toast.LENGTH_SHORT).show();
        }
        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}