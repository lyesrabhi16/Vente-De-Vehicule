package com.example.miniprojet;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.miniprojet.databinding.ActivityMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.tomtom.sdk.location.GeoPoint;
import com.tomtom.sdk.map.display.TomTomMap;
import com.tomtom.sdk.map.display.image.ImageFactory;
import com.tomtom.sdk.map.display.marker.Marker;
import com.tomtom.sdk.map.display.marker.MarkerOptions;
import com.tomtom.sdk.map.display.style.LoadingStyleFailure;
import com.tomtom.sdk.map.display.style.StandardStyles;
import com.tomtom.sdk.map.display.style.StyleLoadingCallback;
import com.tomtom.sdk.map.display.style.StyleMode;
import com.tomtom.sdk.map.display.ui.MapFragment;
import com.tomtom.sdk.map.display.ui.MapReadyCallback;
import com.tomtom.sdk.map.display.ui.UiComponentClickListener;

public class MapActivity extends AppCompatActivity {

    private ActivityMapBinding MBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        MBind = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(MBind.getRoot());



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && getApplicationContext().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
        }
        MapFragment map = (MapFragment) MBind.mapFragment.getFragment();

        map.getMapAsync(
                new MapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull TomTomMap tomTomMap) {

                        map.getCurrentLocationButton().addCurrentLocationButtonClickListener(new UiComponentClickListener() {
                            @Override
                            public void onClick() {
                                /*Todo: map current location button*/
                                Toast.makeText(MapActivity.this, "To be implemented later", Toast.LENGTH_SHORT).show();
                            }
                        });

                        tomTomMap.setStyleMode(StyleMode.DARK);

                        tomTomMap.loadStyle(StandardStyles.INSTANCE.getSATELLITE(), new StyleLoadingCallback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailure(@NonNull LoadingStyleFailure loadingStyleFailure) {

                            }
                        });

                        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);
                        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();

                        locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                MarkerOptions markerOptions = new MarkerOptions(
                                        new GeoPoint(task.getResult().getLatitude(), task.getResult().getLongitude()),
                                        ImageFactory.INSTANCE.fromResource(R.drawable.baseline_location_on_24),
                                        null,
                                        null,
                                        Marker.Companion.getDEFAULT_PIN_ICON_ANCHOR(),
                                        Marker.Companion.getDEFAULT_PLACEMENT_ANCHOR(),
                                        Marker.Companion.getDEFAULT_SHIELD_IMAGE_ANCHOR(),
                                        "",
                                        null,
                                        ""
                                );
                                tomTomMap.addMarker(markerOptions);
                                tomTomMap.zoomToMarkers(1);
                                    /*
                                    MapOptions mapOptions = new MapOptions(getResources().getString(R.string.API_KEY_TOMTOM),new, new Padding(1));

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
                                    });*/
                            }
                        });
                    }
                });

    }
}