package com.example.miniprojet.ui.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.miniprojet.R
import com.example.miniprojet.databinding.ActivityMapBinding
import com.example.miniprojet.ui.Demande.DemandeActivity
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener

class MapActivityKotlin : AppCompatActivity() {

    private lateinit var MBind : ActivityMapBinding
    private lateinit var map : MapboxMap
    private lateinit var markers : PointAnnotationManager

    // lieu choisi : celui reçu avec l'intent, la position de l'appareil, ou le dernier point touché
    private var location : Point? = null
    private var mode = -1

    companion object {
        public val MODE_READ = 0
        public val MODE_WRITE = 1

        private const val REQUEST_LOCATION = 0
        private const val ZOOM_PLACE = 12.0
        // vue d'ensemble de la France tant qu'aucun lieu n'est connu
        private val DEFAULT_CENTER = Point.fromLngLat(2.35, 46.6)
        private const val ZOOM_DEFAULT = 4.5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        // le jeton doit être posé avant la création de la MapView
        MapboxOptions.accessToken = getString(R.string.mapbox_access_token)
        MBind = ActivityMapBinding.inflate(layoutInflater)
        setContentView(MBind.root)

        mode = intent.getIntExtra("MODE", -1)
        val longitude = intent.getStringExtra("longitude")?.toDoubleOrNull()
        val latitude = intent.getStringExtra("latitude")?.toDoubleOrNull()
        if (longitude != null && latitude != null) {
            location = Point.fromLngLat(longitude, latitude)
        }

        if (mode == MODE_READ) {
            MBind.buttonSave.visibility = View.GONE
        }

        map = MBind.mapView.mapboxMap
        markers = MBind.mapView.annotations.createPointAnnotationManager()
        map.loadStyle(Style.SATELLITE_STREETS) {
            map.addOnMapClickListener { point ->
                if (mode == MODE_WRITE) {
                    show(point)
                }
                true
            }
            val place = location
            if (place != null) {
                show(place)
            } else {
                map.setCamera(CameraOptions.Builder().center(DEFAULT_CENTER).zoom(ZOOM_DEFAULT).build())
                locateDevice()
            }
        }

        MBind.buttonBack.setOnClickListener { finish() }
        MBind.buttonSave.setOnClickListener {
            val chosen = location
            if (mode == MODE_WRITE && chosen == null) {
                Toast.makeText(applicationContext, R.string.map_choose_place, Toast.LENGTH_SHORT).show()
            } else if (mode == MODE_WRITE && chosen != null) {
                val backToDemande = Intent(applicationContext, DemandeActivity::class.java)
                backToDemande.putExtra("ACTIVITY_CODE", DemandeActivity.ACTIVITY_RESUME)
                backToDemande.putExtra("longitude", "" + chosen.longitude())
                backToDemande.putExtra("latitude", "" + chosen.latitude())
                backToDemande.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                backToDemande.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(backToDemande)
                finish()
            }
        }
    }

    /** Pose l'unique repère sur [point] et y centre la carte. */
    private fun show(point: Point) {
        location = point
        markers.deleteAll()
        val pin = AppCompatResources.getDrawable(this, R.drawable.baseline_location_on_48)?.toBitmap()
        if (pin != null) {
            markers.create(
                PointAnnotationOptions()
                    .withPoint(point)
                    .withIconImage(pin)
                    .withIconAnchor(IconAnchor.BOTTOM)
            )
        }
        map.setCamera(CameraOptions.Builder().center(point).zoom(ZOOM_PLACE).build())
    }

    /** Sans lieu reçu, part de la dernière position connue de l'appareil, si l'accès à la position est accordé. */
    private fun locateDevice() {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
            return
        }
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val last = manager.getProviders(true)
            .mapNotNull { manager.getLastKnownLocation(it) }
            .maxByOrNull { it.time }
        if (last != null && location == null) {
            show(Point.fromLngLat(last.longitude, last.latitude))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locateDevice()
        }
    }
}
