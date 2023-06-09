package com.example.miniprojet.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.miniprojet.adapters.DemandeAdapter;
import com.example.miniprojet.databinding.FragmentRendezVousBinding;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.RendezVous;
import com.example.miniprojet.ui.Demande.DemandeActivity;

import org.json.JSONObject;

import java.util.ArrayList;

public class RendezVousFragment extends Fragment {

    private FragmentRendezVousBinding binding;
    private ArrayList rendezVousList;
    private DemandeAdapter RendezVousAdapter;

    public RendezVousFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = FragmentRendezVousBinding.inflate(getLayoutInflater());

        rendezVousList = new ArrayList<>();
        RendezVousAdapter = new DemandeAdapter(rendezVousList, getContext());
        binding.recyclerRendezVous.setAdapter(RendezVousAdapter);

        JSONObject filterObjRendezVous = new JSONObject();
        binding.progressBar.setVisibility(View.VISIBLE);
        RendezVous.getAllRendezVous(filterObjRendezVous, getContext(), new RequestFinished() {
            @Override
            public void onFinish(ArrayList args) {
                if(binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                ArrayList<RendezVous>  r = (ArrayList<RendezVous>) args;
                RendezVousAdapter.addWithRendezVousList(r);
                binding.recyclerRendezVous.setAdapter(RendezVousAdapter);
            }

            @Override
            public void onError(ArrayList args) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });

        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "clicked", Toast.LENGTH_SHORT).show();
                Intent demande = new Intent(getContext(), DemandeActivity.class);
                demande.putExtra("TYPE", DemandeActivity.TYPE_RENDEZVOUS);
                startActivity(demande);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRendezVousBinding.inflate(inflater, container, false);
        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent demande = new Intent(getContext(), DemandeActivity.class);
                demande.putExtra("TYPE", DemandeActivity.TYPE_RENDEZVOUS);
                startActivity(demande);
            }
        });
        return binding.getRoot();
    }

}