package com.example.miniprojet.ui.Demande;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.example.miniprojet.R;
import com.example.miniprojet.databinding.ActivityDemandeBinding;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Annonce;
import com.example.miniprojet.models.Client;
import com.example.miniprojet.models.Demande;
import com.example.miniprojet.models.RendezVous;
import com.example.miniprojet.models.Reservation;
import com.example.miniprojet.models.User;
import com.example.miniprojet.ui.messages.chat.ChatActivity;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.tomtom.sdk.map.display.MapOptions;
import com.tomtom.sdk.map.display.TomTomMap;
import com.tomtom.sdk.map.display.marker.MarkerOptions;
import com.tomtom.sdk.map.display.ui.MapReadyCallback;

import java.util.ArrayList;
import java.util.Calendar;

public class DemandeActivity extends AppCompatActivity {

    private ActivityDemandeBinding DBind;
    private Annonce annonce;
    private Demande demande;
    private RendezVous rendezVous;
    private Reservation reservation;
    private Client demendeur;
    private Client recepteur;
    private TomTomMap map;
    private MarkerOptions markerOptions;
    private MapOptions mapOptions;
    private int Type = -1;
    public static int TYPE_RESERVATION = 0;
    public static int TYPE_RENDEZVOUS = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        DBind = ActivityDemandeBinding.inflate(getLayoutInflater());
        setContentView(DBind.getRoot());


        User user = User.getInstance(getApplicationContext());


        DBind.mapView.onCreate(savedInstanceState);

        int idAnnonce = getIntent().getIntExtra("idAnnonce", -1);

        if(user.isLoggedin()){

            if (idAnnonce == -1){
                Toast.makeText(this, "Unrecognized Announcement", Toast.LENGTH_SHORT).show();
                finish();
            }



            demande = new Demande();

            rendezVous = new RendezVous();
            reservation = new Reservation();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return;
                }
            }

            DBind.mapView.onCreate(savedInstanceState);
            DBind.mapView.getMapAsync(
                    new MapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull TomTomMap tomTomMap) {
                            map = tomTomMap;

                            /*FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(DemandeActivity.this);
                            @SuppressLint("MissingPermission") Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                                @Override
                                public void onComplete(@NonNull Task<Location> task) {

                                    MapMarker = new MarkerOptions().position(new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude())).draggable(true);
                                    Map.addMarker(MapMarker);
                                    Map.addMapClickListener(new MapClickListener() {
                                        @Override
                                        public boolean onMapClicked(@NonNull GeoPoint geoPoint) {
                                            reservation.setLieuReservation(marker.getPosition().toString());
                                            rendezVous.setLieuRendezVous(marker.getPosition().toString());
                                            DBind.lieuText.setText(marker.getTitle());
                                            return false;
                                        }
                                    });
                                    Map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                                        @Override
                                        public void onMarkerDragStart(Marker marker) {

                                        }

                                        @Override
                                        public void onMarkerDrag(Marker marker) {

                                        }

                                        @Override
                                        public void onMarkerDragEnd(Marker marker) {

                                        }
                                    });
                                }
                            });*/
                        }
                    });


            ProgressDialog prgrs = new ProgressDialog(this);
            prgrs.setMessage("Getting Details...");
            prgrs.show();
            Annonce.getAnnonce(idAnnonce, getApplicationContext(), new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    if (DBind == null) return;
                    prgrs.dismiss();
                    annonce = (Annonce) args.get(0);
                    if(args.size() < 2 )return;
                    recepteur = (Client) args.get(1);
                    demande.setAnnonce(annonce);

                    DBind.containerPerson.Title.setText(annonce.getUserTitle());
                    DBind.containerPerson.subTitle.setText(annonce.getUserSubTitle());
                    DBind.dateLayout.setEnabled(true);


                    DBind.etat.setText("Pending");
                    if(annonce.getType().equals("Location")){
                        DBind.dateLayout.setHint(getResources().getText(R.string.date_debut));
                        DBind.dateLayout2.setVisibility(View.VISIBLE);
                        DBind.dateLayout2.setEnabled(true);
                        DBind.demandeType.setText("Reservation");
                        Type = TYPE_RESERVATION;
                        reservation.setIdClient(user.getID());
                        reservation.setIdAnnonce(annonce.getIdAnnonce());
                        Reservation.getReservation(annonce.getIdAnnonce(), user.getID(), getApplicationContext(), new RequestFinished() {
                            @Override
                            public void onFinish(ArrayList args) {
                                reservation = (Reservation) args.get(0);
                                if(reservation.getEtatReservation() != null)
                                    DBind.etat.setText(reservation.getEtatReservation());
                                DBind.dateText.setText(reservation.getDateDebut());
                                DBind.dateText2.setText(reservation.getDateFin());
                                if(reservation.getLieuReservation() != null){
                                    DBind.lieuText.setText(reservation.getLieuReservation());
                                }

                                DBind.buttonConfirmer.setVisibility(View.GONE);
                                DBind.buttonAnnuler.setVisibility(View.GONE);
                                DBind.buttonSupprimer.setVisibility(View.VISIBLE);
                                if(user.getID() == recepteur.getIdClient()){
                                    DBind.lieuLayout.setEnabled(true);

                                    if(reservation.getEtatReservation().equals("Pending")){
                                        DBind.buttonAccorder.setVisibility(View.VISIBLE);
                                        DBind.buttonRejeter.setVisibility(View.VISIBLE);
                                    }

                                }
                                Client.getClient(reservation.getIdClient(), getApplicationContext(), new RequestFinished() {
                                    @Override
                                    public void onFinish(ArrayList args) {
                                        demendeur = (Client) args.get(0);
                                        demande.setPerson(demendeur);
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
                        DBind.demandeType.setText("RendezVous");
                        Type = TYPE_RENDEZVOUS;
                        rendezVous.setIdClient(user.getID());
                        rendezVous.setIdAnnonce(annonce.getIdAnnonce());
                        RendezVous.getRendezVous(annonce.getIdAnnonce(), user.getID(), getApplicationContext(), new RequestFinished() {
                            @Override
                            public void onFinish(ArrayList args) {
                                rendezVous = (RendezVous) args.get(0);
                                if(rendezVous.getEtatRendezVous() != null)
                                    DBind.etat.setText(rendezVous.getEtatRendezVous());
                                DBind.dateText.setText(rendezVous.getDateRendezVous());
                                if(rendezVous.getLieuRendezVous() != null){
                                    DBind.lieuText.setText(rendezVous.getLieuRendezVous());
                                }

                                DBind.buttonConfirmer.setVisibility(View.GONE);
                                DBind.buttonAnnuler.setVisibility(View.GONE);
                                DBind.buttonSupprimer.setVisibility(View.VISIBLE);
                                if(user.getID() == recepteur.getIdClient()){
                                    DBind.lieuLayout.setEnabled(true);
                                    if(reservation.getEtatReservation().equals("Pending")){
                                        DBind.buttonAccorder.setVisibility(View.VISIBLE);
                                        DBind.buttonRejeter.setVisibility(View.VISIBLE);
                                    }
                                }
                                Client.getClient(rendezVous.getIdClient(), getApplicationContext(), new RequestFinished() {
                                    @Override
                                    public void onFinish(ArrayList args) {
                                        demendeur = (Client) args.get(0);
                                        demande.setPerson(demendeur);
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


                }

                @Override
                public void onError(ArrayList args) {
                    if (DBind == null) return;
                    prgrs.dismiss();
                }
            });


            Client.getClient(user.getID(), getApplicationContext(), new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    Client c = (Client) args.get(0);
                    demande.setPerson(c);
                }

                @Override
                public void onError(ArrayList args) {

                }
            });


        }
        else {
            Toast.makeText(this, "Please Log in", Toast.LENGTH_SHORT).show();
            finish();
        }

        DBind.dateLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (demande == null )return;
                DBind.etat.setText("Pending");
                if (demande.getAnnonce().getType().equals("Vente")){
                    demande.setTitle("RendezVous");
                    DBind.demandeType.setText("RendezVous");
                    MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select Rendez-Vous Date")
                            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                            .build();
                    datePicker.show(getSupportFragmentManager(), "Material_Date_Picker");

                    datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                        @Override
                        public void onPositiveButtonClick(Long selection) {

                            Calendar c = Calendar.getInstance();
                            c.setTimeInMillis(selection);
                            rendezVous.setDateRendezVous(ChatActivity.getDate("yyyy-MM-dd HH:mm:ss", c.getTime()));
                            DBind.dateText.setText(c.getTime().toString());
                        }
                    });

                } else if (demande.getAnnonce().getType().equals("Location")) {
                    demande.setTitle("Reservation");
                    DBind.demandeType.setText("Reservation");
                    MaterialDatePicker<Pair<Long, Long>> datePicker = MaterialDatePicker.Builder.dateRangePicker()
                            .setTitleText("Select Reservation Date Range")
                            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                            .setSelection(Pair.create(MaterialDatePicker.todayInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds()))
                            .build();
                    datePicker.show(getSupportFragmentManager(), "Material_Date_Picker");
                    datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
                        @Override
                        public void onPositiveButtonClick(Pair<Long, Long> selection) {
                            Calendar c1 = Calendar.getInstance();
                            c1.setTimeInMillis(selection.first);
                            reservation.setDateDebut(ChatActivity.getDate("yyyy-MM-dd HH:mm:ss", c1.getTime()));
                            DBind.dateText.setText(c1.getTime().toString());
                            Calendar c2 = Calendar.getInstance();
                            c2.setTimeInMillis(selection.second);
                            reservation.setDateFin(ChatActivity.getDate("yyyy-MM-dd HH:mm:ss", c2.getTime()));
                            DBind.dateText2.setText(c2.getTime().toString());
                        }
                    });
                }

            }
        });
        DBind.dateLayout2.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (demande == null )return;
                DBind.etat.setText("Pending");
                if (demande.getAnnonce().getType().equals("Vente")){
                    demande.setTitle("RendezVous");
                    DBind.demandeType.setText("RendezVous");
                    MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select Rendez-Vous Date")
                            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                            .build();
                    datePicker.show(getSupportFragmentManager(), "Material_Date_Picker");

                    datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                        @Override
                        public void onPositiveButtonClick(Long selection) {

                            Calendar c = Calendar.getInstance();
                            c.setTimeInMillis(selection);
                            rendezVous.setDateRendezVous(ChatActivity.getDate("yyyy-MM-dd HH:mm:ss", c.getTime()));
                            DBind.dateText.setText(c.getTime().toString());
                        }
                    });

                } else if (demande.getAnnonce().getType().equals("Location")) {
                    demande.setTitle("Reservation");
                    DBind.demandeType.setText("Reservation");
                    MaterialDatePicker<Pair<Long, Long>> datePicker = MaterialDatePicker.Builder.dateRangePicker()
                            .setTitleText("Select Reservation Date Range")
                            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                            .setSelection(Pair.create(MaterialDatePicker.todayInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds()))
                            .build();
                    datePicker.show(getSupportFragmentManager(), "Material_Date_Picker");
                    datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
                        @Override
                        public void onPositiveButtonClick(Pair<Long, Long> selection) {
                            Calendar c1 = Calendar.getInstance();
                            c1.setTimeInMillis(selection.first);
                            reservation.setDateDebut(ChatActivity.getDate("yyyy-MM-dd HH:mm:ss", c1.getTime()));
                            DBind.dateText.setText(c1.getTime().toString());
                            Calendar c2 = Calendar.getInstance();
                            c2.setTimeInMillis(selection.second);
                            reservation.setDateFin(ChatActivity.getDate("yyyy-MM-dd HH:mm:ss", c2.getTime()));
                            DBind.dateText2.setText(c2.getTime().toString());
                        }
                    });
                }

            }
        });

        DBind.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        DBind.buttonAnnuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        DBind.buttonConfirmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(demande.getAnnonce().getType().equals("Vente")){
                        rendezVous.setEtatRendezVous("Pending");
                        rendezVous.setLieuRendezVous("null");
                        RendezVous.AddRendezVous(rendezVous, getApplicationContext(), new RequestFinished() {
                            @Override
                            public void onFinish(ArrayList args) {
                                Toast.makeText(DemandeActivity.this, "Rendez-Vous Requested", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onError(ArrayList args) {
                                Toast.makeText(DemandeActivity.this, "failed to request Rendez-Vous", Toast.LENGTH_SHORT).show();

                            }
                        });
                } else if (demande.getAnnonce().getType().equals("Location")) {
                    reservation.setEtatReservation("Pending");
                    reservation.setLieuReservation("null");
                    Reservation.AddReservation(reservation, getApplicationContext(), new RequestFinished() {
                        @Override
                        public void onFinish(ArrayList args) {
                            Toast.makeText(DemandeActivity.this, "Reservation Requested", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(ArrayList args) {
                            Toast.makeText(DemandeActivity.this, "failed to request Reservation", Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        });

        DBind.buttonSupprimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Type == -1) return;
                ProgressDialog prgrs = new ProgressDialog(DemandeActivity.this);
                prgrs.setMessage("Deleting...");
                prgrs.show();

                if(Type == TYPE_RESERVATION){
                    Reservation.RemoveReservation(idAnnonce, demande.getPerson().getIdClient(), getApplicationContext(), new RequestFinished() {
                        @Override
                        public void onFinish(ArrayList args) {
                            finish();
                        }

                        @Override
                        public void onError(ArrayList args) {

                        }
                    });
                }
                if(Type == TYPE_RENDEZVOUS){
                    RendezVous.RemoveRendezVous(idAnnonce, demande.getPerson().getIdClient(), getApplicationContext(), new RequestFinished() {
                        @Override
                        public void onFinish(ArrayList args) {
                            finish();
                        }

                        @Override
                        public void onError(ArrayList args) {

                        }
                    });
                }

            }
        });
        DBind.buttonAccorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Type == -1) return;
                else if (Type == TYPE_RENDEZVOUS) {
                    rendezVous.setEtatRendezVous("Accepted");
                    RendezVous.updateRendezVous(rendezVous, getApplicationContext(), new RequestFinished() {
                        @Override
                        public void onFinish(ArrayList args) {
                            finish();
                        }

                        @Override
                        public void onError(ArrayList args) {

                        }
                    });
                } else if (Type == TYPE_RESERVATION) {
                    reservation.setEtatReservation("Accepted");
                    Reservation.updateReservation(reservation, getApplicationContext(), new RequestFinished() {
                        @Override
                        public void onFinish(ArrayList args) {
                            finish();
                        }


                        @Override
                        public void onError(ArrayList args) {

                        }
                    });
                }
            }
        });
        DBind.buttonRejeter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Type == -1) return;
                else if (Type == TYPE_RENDEZVOUS) {
                    rendezVous.setEtatRendezVous("Rejected");
                    RendezVous.updateRendezVous(rendezVous, getApplicationContext(), new RequestFinished() {
                        @Override
                        public void onFinish(ArrayList args) {
                            finish();
                        }

                        @Override
                        public void onError(ArrayList args) {

                        }
                    });
                } else if (Type == TYPE_RESERVATION) {
                    reservation.setEtatReservation("Rejected");
                    Reservation.updateReservation(reservation, getApplicationContext(), new RequestFinished() {
                        @Override
                        public void onFinish(ArrayList args) {
                            finish();
                        }


                        @Override
                        public void onError(ArrayList args) {

                        }
                    });
                }
            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();
        if(DBind == null)return;
        DBind.mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(DBind == null)return;
        DBind.mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(DBind == null)return;
        DBind.mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(DBind == null)return;
        DBind.mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(DBind == null)return;
        DBind.mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(DBind == null)return;
        DBind.mapView.onSaveInstanceState(outState);
    }


}