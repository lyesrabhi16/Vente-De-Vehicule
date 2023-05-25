package com.example.miniprojet.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miniprojet.R;
import com.example.miniprojet.adapters.AnnonceAdapter;
import com.example.miniprojet.adapters.DemandeAdapter;
import com.example.miniprojet.databinding.FragmentDashboardBinding;
import com.example.miniprojet.interfaces.RecyclerViewInterface;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Annonce;
import com.example.miniprojet.models.Demande;
import com.example.miniprojet.models.RendezVous;
import com.example.miniprojet.models.Reservation;
import com.example.miniprojet.models.User;
import com.example.miniprojet.ui.annonce.AnnonceActivity;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private ArrayList<Annonce> annonces;
    private AnnonceAdapter adapter;
    private ArrayList<Demande> reservations;
    private DemandeAdapter ReservationAdapter;
    private ArrayList<Demande> rendezVousList;
    private DemandeAdapter RendezVousAdapter;

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
                    Intent annonce = new Intent(getContext(), AnnonceActivity.class);
                    annonce.putExtra("idAnnonce", annonces.get(position).getIdAnnonce());
                    startActivity(annonce);
                }
            });

            binding.recyclerDashboard.setAdapter(adapter);

            reservations = new ArrayList<>();
            ReservationAdapter = new DemandeAdapter(reservations, getContext());
            binding.recyclerDashboardReservations.setAdapter(ReservationAdapter);

            JSONObject filterObj = new JSONObject();
            try {
                filterObj.put("idClient", user.getID());
                Reservation.getReservations(filterObj, getContext(), new RequestFinished() {
                    @Override
                    public void onFinish(ArrayList args) {
                        ArrayList<Reservation>  r = (ArrayList<Reservation>) args;
                        ReservationAdapter.setList(new ArrayList<Demande>());
                        ReservationAdapter.addWithReservationsList(r);
                        binding.recyclerDashboardReservations.setAdapter(ReservationAdapter);
                    }

                    @Override
                    public void onError(ArrayList args) {

                    }
                });
            } catch (JSONException e) {
                Toast.makeText(getContext(), "JSON Error", Toast.LENGTH_SHORT).show();
            }

            rendezVousList = new ArrayList<>();
            RendezVousAdapter = new DemandeAdapter(rendezVousList, getContext());
            binding.recyclerDashboardRendezVous.setAdapter(RendezVousAdapter);

            JSONObject filterObjRendezVous = new JSONObject();
            try {
                filterObjRendezVous.put("idClient", user.getID());
                RendezVous.getAllRendezVous(filterObjRendezVous, getContext(), new RequestFinished() {
                    @Override
                    public void onFinish(ArrayList args) {
                        ArrayList<RendezVous>  r = (ArrayList<RendezVous>) args;
                        ReservationAdapter.setList(new ArrayList<Demande>());
                        RendezVousAdapter.addWithRendezVousList(r);
                        binding.recyclerDashboardRendezVous.setAdapter(RendezVousAdapter);
                    }

                    @Override
                    public void onError(ArrayList args) {

                    }
                });
            } catch (JSONException e) {
                Toast.makeText(getContext(), "JSON Error", Toast.LENGTH_SHORT).show();
            }


            binding.buttonAnnonces.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switchRecyclerVisibility(binding.buttonAnnonces, binding.recyclerDashboard);
                }
            });
            binding.buttonRendezvous.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switchRecyclerVisibility(binding.buttonRendezvous, binding.recyclerDashboardRendezVous);
                }
            });
            binding.buttonReservation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switchRecyclerVisibility(binding.buttonReservation, binding.recyclerDashboardReservations);
                }
            });

            binding.progressBar.setVisibility(View.VISIBLE);
            Annonce.getUserAnnonces(user.getID(), getContext(), new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    if(binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.buttonAnnonces.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.baseline_arrow_drop_up_24), null);
                    binding.recyclerDashboard.setVisibility(View.VISIBLE);

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
    public void switchRecyclerVisibility(MaterialButton button, RecyclerView recyclerView){
        if(recyclerView.getVisibility() == View.VISIBLE){
            button.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.baseline_arrow_drop_down_24), null);
            recyclerView.setVisibility(View.GONE);
        }
        else{
            button.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.baseline_arrow_drop_up_24), null);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}