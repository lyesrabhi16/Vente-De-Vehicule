package com.example.miniprojet.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.miniprojet.Server;
import com.example.miniprojet.adapters.AnnonceAdapter;
import com.example.miniprojet.databinding.FragmentHomeBinding;
import com.example.miniprojet.interfaces.RecyclerViewInterface;
import com.example.miniprojet.models.Annonce;
import com.example.miniprojet.models.HomeViewModel;
import com.example.miniprojet.ui.annonce.AddAnnonceActivity;

import java.util.ArrayList;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Server dbc;
    private AnnonceAdapter adapter;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        ArrayList<Annonce> Annonces = new ArrayList<Annonce>();

        Annonce a = new Annonce();
        a.setTitle("titre");
        a.setType("type");
        a.setQte("Prix");
        a.setDate("date");
        a.setDesc("description");
        a.setUserTitle("user");
        a.setUserSubTitle("email");
        Annonces.add(a);
        Annonces.add(a);
        Annonces.add(a);
        Annonces.add(a);

        adapter = new AnnonceAdapter(Annonces, new RecyclerViewInterface() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(getContext(), "item "+ position +" clicked", Toast.LENGTH_SHORT).show();
            }
        });

        binding.recyclerHome.setAdapter(adapter);


        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}