package com.example.miniprojet.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.miniprojet.adapters.DemandeAdapter;
import com.example.miniprojet.databinding.FragmentReservationsBinding;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Reservation;

import org.json.JSONObject;

import java.util.ArrayList;


public class ReservationsFragment extends Fragment {

    private FragmentReservationsBinding binding;
    private ArrayList reservatinos;
    private DemandeAdapter ReservationAdapter;

    public ReservationsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentReservationsBinding.inflate(getLayoutInflater());
        reservatinos = new ArrayList<Reservation>();
        ReservationAdapter = new DemandeAdapter(reservatinos, getContext());
        binding.recyclerReservations.setAdapter(ReservationAdapter);

        JSONObject filterObjRendezVous = new JSONObject();
        binding.progressBar.setVisibility(View.VISIBLE);
        Reservation.getReservations(filterObjRendezVous, getContext(), new RequestFinished() {
            @Override
            public void onFinish(ArrayList args) {
                if(binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                ArrayList<Reservation>  r = (ArrayList<Reservation>) args;
                ReservationAdapter.addWithReservationsList(r);
                binding.recyclerReservations.setAdapter(ReservationAdapter);
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
        binding = FragmentReservationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}