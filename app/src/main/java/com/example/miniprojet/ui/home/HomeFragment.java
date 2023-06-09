package com.example.miniprojet.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.miniprojet.Server;
import com.example.miniprojet.adapters.AnnonceAdapter;
import com.example.miniprojet.databinding.FragmentFilterBinding;
import com.example.miniprojet.databinding.FragmentHomeBinding;
import com.example.miniprojet.interfaces.RecyclerViewInterface;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Annonce;
import com.example.miniprojet.models.HomeViewModel;
import com.example.miniprojet.ui.annonce.AddAnnonceActivity;
import com.example.miniprojet.ui.annonce.AnnonceActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding Home;
    private FragmentFilterBinding filter;
    private Server dbc;
    private ArrayList<Annonce> Annonces;
    private AnnonceAdapter adapter;
    private JSONObject filterObj;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        Home = FragmentHomeBinding.inflate(inflater, container, false);
        View root = Home.getRoot();

        Annonces = new ArrayList<Annonce>();

        adapter = new AnnonceAdapter(Annonces, getContext(),new RecyclerViewInterface() {
            @Override
            public void onItemClick(int position) {
                Intent annonce = new Intent(getContext(), AnnonceActivity.class);
                annonce.putExtra("idAnnonce", Annonces.get(position).getIdAnnonce());
                startActivity(annonce);
            }
        });

        Home.recyclerHome.setAdapter(adapter);
        filterObj = new JSONObject();
        Filter();

        filter = FragmentFilterBinding.bind(Home.containerFilter.getRoot());

        filter.type.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String selected = Home.containerFilter.type.getText().toString();
                    if (selected.equals("All")){
                        filterObj.remove("typeVehicule");
                    }
                    else{
                        filterObj.put("typeVehicule", Home.containerFilter.type.getText().toString());
                    }
                    Filter();
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                }
            }
        });
        filter.marque.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String selected = Home.containerFilter.marque.getText().toString();
                    if (selected.equals("All")){
                        filterObj.remove("marqueVehicule");
                    }
                    else{
                        filterObj.put("marqueVehicule", Home.containerFilter.marque.getText().toString());
                    }
                    Filter();
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                }
            }
        });
        filter.couleur.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String selected = Home.containerFilter.couleur.getText().toString();
                    if (selected.equals("All")){
                        filterObj.remove("couleurVehicule");
                    }
                    else{
                        filterObj.put("couleurVehicule", Home.containerFilter.couleur.getText().toString());
                    }
                    Filter();
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                }
            }
        });
        filter.transmission.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String selected = Home.containerFilter.transmission.getText().toString();
                    if (selected.equals("All")){
                        filterObj.remove("transmissionVehicule");
                    }
                    else{
                        filterObj.put("transmissionVehicule", Home.containerFilter.transmission.getText().toString());
                    }
                    Filter();
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                }
            }
        });
        filter.energie.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String selected = Home.containerFilter.energie.getText().toString();
                    if (selected.equals("All")){
                        filterObj.remove("energieVehicule");
                    }
                    else{
                        filterObj.put("energieVehicule", Home.containerFilter.energie.getText().toString());
                    }
                    Filter();
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                }
            }
        });



        Home.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent AddAnnonce = new Intent(getContext(), AddAnnonceActivity.class);
                startActivity(AddAnnonce);
            }
        });

        /*
        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
         */
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Filter();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Home = null;
    }
    public void Filter(){
        Home.progressBar.setVisibility(View.VISIBLE);
        Annonce.getAnnonces(filterObj, getContext(), new RequestFinished() {
            @Override
            public void onFinish(ArrayList args) {

                if(Home == null) return;
                Home.progressBar.setVisibility(View.GONE);
                Annonces = (ArrayList<Annonce>) args;
                adapter.setList(Annonces);
                Home.recyclerHome.setAdapter(adapter);
            }

            @Override
            public void onError(ArrayList args) {

                if(Home == null) return;
                Home.progressBar.setVisibility(View.GONE);
            }
        });
    }
}