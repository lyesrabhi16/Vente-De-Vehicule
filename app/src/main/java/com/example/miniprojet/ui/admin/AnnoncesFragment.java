package com.example.miniprojet.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.miniprojet.adapters.AnnonceAdapter;
import com.example.miniprojet.databinding.FragmentAnnoncesBinding;
import com.example.miniprojet.databinding.FragmentFilterBinding;
import com.example.miniprojet.interfaces.RecyclerViewInterface;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Annonce;
import com.example.miniprojet.ui.annonce.AddAnnonceActivity;
import com.example.miniprojet.ui.annonce.AnnonceActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class AnnoncesFragment extends Fragment {

    private FragmentAnnoncesBinding binding;
    private ArrayList<Annonce> annonces;
    private AnnonceAdapter annonceAdapter;
    private FragmentFilterBinding filter;
    public AnnoncesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentAnnoncesBinding.inflate(getLayoutInflater());
        annonces = new ArrayList<Annonce>();

        annonceAdapter = new AnnonceAdapter(annonces, getContext(),new RecyclerViewInterface() {
            @Override
            public void onItemClick(int position) {
                Intent annonce = new Intent(getContext(), AnnonceActivity.class);
                annonce.putExtra("idAnnonce", annonces.get(position).getIdAnnonce());
                startActivity(annonce);
            }
        });

        binding.containerHome.recyclerHome.setAdapter(annonceAdapter);
        JSONObject filterObj = new JSONObject();
        Filter(filterObj);

        filter = FragmentFilterBinding.bind(binding.containerHome.containerFilter.getRoot());

        filter.type.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String selected = binding.containerHome.containerFilter.type.getText().toString();
                    if (selected.equals("All")){
                        filterObj.remove("typeVehicule");
                    }
                    else{
                        filterObj.put("typeVehicule", binding.containerHome.containerFilter.type.getText().toString());
                    }
                    Filter(filterObj);
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                }
            }
        });
        filter.marque.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String selected = binding.containerHome.containerFilter.marque.getText().toString();
                    if (selected.equals("All")){
                        filterObj.remove("marqueVehicule");
                    }
                    else{
                        filterObj.put("marqueVehicule", binding.containerHome.containerFilter.marque.getText().toString());
                    }
                    Filter(filterObj);
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                }
            }
        });
        filter.couleur.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String selected = binding.containerHome.containerFilter.couleur.getText().toString();
                    if (selected.equals("All")){
                        filterObj.remove("couleurVehicule");
                    }
                    else{
                        filterObj.put("couleurVehicule", binding.containerHome.containerFilter.couleur.getText().toString());
                    }
                    Filter(filterObj);
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                }
            }
        });
        filter.transmission.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String selected = binding.containerHome.containerFilter.transmission.getText().toString();
                    if (selected.equals("All")){
                        filterObj.remove("transmissionVehicule");
                    }
                    else{
                        filterObj.put("transmissionVehicule", binding.containerHome.containerFilter.transmission.getText().toString());
                    }
                    Filter(filterObj);
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                }
            }
        });
        filter.energie.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String selected = binding.containerHome.containerFilter.energie.getText().toString();
                    if (selected.equals("All")){
                        filterObj.remove("energieVehicule");
                    }
                    else{
                        filterObj.put("energieVehicule", binding.containerHome.containerFilter.energie.getText().toString());
                    }
                    Filter(filterObj);
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                }
            }
        });



        binding.containerHome.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent AddAnnonce = new Intent(getContext(), AddAnnonceActivity.class);
                startActivity(AddAnnonce);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAnnoncesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void Filter(JSONObject filterObj){
        binding.containerHome.progressBar.setVisibility(View.VISIBLE);
        Annonce.getAnnonces(filterObj, getContext(), new RequestFinished() {
            @Override
            public void onFinish(ArrayList args) {

                if(binding == null) return;
                binding.containerHome.progressBar.setVisibility(View.GONE);
                annonces = (ArrayList<Annonce>) args;
                annonceAdapter.setList(annonces);
                binding.containerHome.recyclerHome.setAdapter(annonceAdapter);
            }

            @Override
            public void onError(ArrayList args) {

                if(binding == null) return;
                binding.containerHome.progressBar.setVisibility(View.GONE);
            }
        });
    }
}