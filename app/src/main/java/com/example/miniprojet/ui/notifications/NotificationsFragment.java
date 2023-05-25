package com.example.miniprojet.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.miniprojet.R;
import com.example.miniprojet.adapters.DemandeAdapter;
import com.example.miniprojet.databinding.FragmentNotificationsBinding;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Annonce;
import com.example.miniprojet.models.RendezVous;
import com.example.miniprojet.models.Reservation;
import com.example.miniprojet.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private ArrayList notifications;
    private DemandeAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        /*NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);*/

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        User user = User.getInstance(getContext());
        if(user.isLoggedin()){
            binding.recyclerNotifications.setVisibility(View.VISIBLE);
            binding.textMessages.setVisibility(View.GONE);

            notifications = new ArrayList();
            adapter = new DemandeAdapter(notifications, getContext());
            binding.recyclerNotifications.setAdapter(adapter);

            Annonce.getUserAnnonces(user.getID(), getContext(), new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    ArrayList<Annonce> annonces = (ArrayList<Annonce>) args;
                    try {
                        for (int i = 0; i < annonces.size(); i++) {
                            Annonce a = annonces.get(i);
                            JSONObject filterObj = new JSONObject();
                            filterObj.put("idAnnonce", a.getIdAnnonce()+"");
                            if(a.getType().equals("Location")){
                                filterObj.put("etatReservation", "Pending");
                                Reservation.getReservations(filterObj, getContext(), new RequestFinished() {
                                    @Override
                                    public void onFinish(ArrayList args) {
                                        if(args.size() < 1) return;
                                        ArrayList<Reservation> reservations = (ArrayList<Reservation>) args;
                                        adapter.addWithReservationsList(reservations);
                                        binding.recyclerNotifications.setAdapter(adapter);
                                    }

                                    @Override
                                    public void onError(ArrayList args) {

                                    }
                                });

                            } else if (a.getType().equals("Vente")) {
                                filterObj.put("etatRendezVous", "Pending");
                                RendezVous.getAllRendezVous(filterObj, getContext(), new RequestFinished() {
                                    @Override
                                    public void onFinish(ArrayList args) {
                                        if(args.size() < 1) return;
                                        ArrayList<RendezVous> rendezVous = (ArrayList<RendezVous>) args;
                                        adapter.addWithRendezVousList(rendezVous);
                                        binding.recyclerNotifications.setAdapter(adapter);
                                     }

                                    @Override
                                    public void onError(ArrayList args) {

                                    }
                                });
                            }

                        }
                    }
                    catch (JSONException e) {
                        Toast.makeText(getContext(), "JSON Error", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(ArrayList args) {

                }
            });


        }
        else{
            binding.recyclerNotifications.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);
            binding.textMessages.setText(getResources().getText(R.string.login_request));
            binding.textMessages.setVisibility(View.VISIBLE);
        }



        /*
        final TextView textView = binding.textNotifications;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);*/
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}