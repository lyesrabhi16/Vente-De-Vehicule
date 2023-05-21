package com.example.miniprojet.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.miniprojet.adapters.AnnonceAdapter;
import com.example.miniprojet.databinding.FragmentDashboardBinding;
import com.example.miniprojet.interfaces.RecyclerViewInterface;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Annonce;
import com.example.miniprojet.models.User;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private ArrayList<Annonce> annonces;
    private AnnonceAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        /*DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);*/

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        User user = User.getInstance(getContext());
        if (user.isLoggedin()){
            binding.textDashboard.setVisibility(View.GONE);
            binding.main.setVisibility(View.VISIBLE);

            annonces = new ArrayList<Annonce>();
            adapter = new AnnonceAdapter(annonces, getContext(), new RecyclerViewInterface() {
                @Override
                public void onItemClick(int position) {
                    Toast.makeText(getContext(), "item clicked", Toast.LENGTH_SHORT).show();
                }
            });

            binding.recyclerDashboard.setAdapter(adapter);

            binding.progressBar.setVisibility(View.VISIBLE);
            Annonce.getUserAnnonces(user.getID(), getContext(), new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    if(binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);

                    annonces = (ArrayList<Annonce>) args;
                    adapter.setList(annonces);
                    binding.recyclerDashboard.getAdapter().notifyDataSetChanged();
                }

                @Override
                public void onError(ArrayList args) {
                    if(binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                }
            });
        }
        else{
            binding.textDashboard.setVisibility(View.VISIBLE);
            binding.main.setVisibility(View.GONE);
        }
        /*final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);*/
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}