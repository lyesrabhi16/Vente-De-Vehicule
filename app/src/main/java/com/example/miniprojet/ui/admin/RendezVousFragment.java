package com.example.miniprojet.ui.admin;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRendezVousBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

}