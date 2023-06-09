package com.example.miniprojet.ui.map

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.miniprojet.R
import com.example.miniprojet.databinding.ActivityMapBinding
import com.example.miniprojet.ui.Demande.DemandeActivity
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.android.AndroidLocationProvider
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.gesture.MapClickListener
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.style.*
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.map.display.ui.MapReadyCallback

class MapActivityKotlin : AppCompatActivity() {

    private lateinit var MBind : ActivityMapBinding
    private lateinit var mapFragment : MapFragment
    private lateinit var location: GeoPoint

    companion object {
        public val MODE_READ = 0
        public val MODE_WRITE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide();
        MBind = ActivityMapBinding.inflate(layoutInflater)
        setContentView(MBind.root)


        val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }
        val locationProvider : LocationProvider = AndroidLocationProvider(applicationContext)
        locationProvider.enable()

        val mode = intent.getIntExtra("MODE", -1)
        val longitude = intent.getStringExtra("longitude")?.toDouble()
        val latitude = intent.getStringExtra("latitude")?.toDouble()

        Toast.makeText(applicationContext, "MODE: "+mode, Toast.LENGTH_SHORT).show()
        if(mode == MODE_READ){
            Toast.makeText(applicationContext, "MODE_READ", Toast.LENGTH_SHORT).show()
            MBind.buttonSave.visibility = View.GONE
            MBind.buttonSave.isVisible = false
        }

        mapFragment = MBind.mapFragment.getFragment()
        mapFragment.getMapAsync(MapReadyCallback { map: TomTomMap ->
            map.setStyleMode(StyleMode.DARK)

            val onStyleLoadedCallback = object : StyleLoadingCallback {
                override fun onSuccess() {
                    /* YOUR CODE GOES HERE */
                }

                override fun onFailure(error: LoadingStyleFailure) {
                    /* YOUR CODE GOES HERE */
                }
            }
            map.loadStyle(StandardStyles.SATELLITE, onStyleLoadedCallback)


            map.setLocationProvider(locationProvider)

            val locationMarkerOptions = LocationMarkerOptions(
                type = LocationMarkerOptions.Type.Pointer
            )
            map.enableLocationMarker(locationMarkerOptions)
            if(longitude == null || latitude == null) {
                if(map.currentLocation != null){
                    val currentLocation : GeoLocation = map.currentLocation as GeoLocation
                    location = currentLocation.position
                }
                else{
                    location = locationProvider.lastKnownLocation?.position!!
                }

            }
            else{
                location = GeoPoint(latitude, longitude)
            }

            var markerOptions : MarkerOptions = MarkerOptions(
                coordinate = location,
                pinImage = ImageFactory.fromResource(R.drawable.baseline_location_on_48),
                balloonText = location.toPlainString("|")
            )
            map.addMarker(markerOptions)
            map.zoomToMarkers(10)
            map.addMapClickListener(MapClickListener { coordinate: GeoPoint ->
                if(mode == MODE_WRITE) {
                    map.removeMarkers()
                    markerOptions = MarkerOptions(
                        coordinate = coordinate,
                        pinImage = ImageFactory.fromResource(R.drawable.baseline_location_on_48),
                        balloonText = coordinate.toPlainString(" ")
                    )
                    map.addMarker(markerOptions)
                    location = coordinate
                }
                true
            })
            map.addMarkerClickListener { marker: Marker ->
                if (!marker.isSelected()) {
                    marker.select()
                }
            }
        })

        MBind.buttonBack.setOnClickListener(View.OnClickListener { view: View? ->
            finish()
        })
        MBind.buttonSave.setOnClickListener(View.OnClickListener { view: View? ->
            if(mode == MODE_WRITE){
                val BackToDemande : Intent = Intent(applicationContext, DemandeActivity::class.java)
                BackToDemande.putExtra("ACTIVITY_CODE", DemandeActivity.ACTIVITY_RESUME)
                BackToDemande.putExtra("longitude", ""+location.longitude)
                BackToDemande.putExtra("latitude", ""+location.latitude)
                BackToDemande.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                BackToDemande.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(BackToDemande)
                finish()
            }
        })


    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }
}