package edu.msudenver.lab04

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.msudenver.lab04.databinding.ActivityMapsBinding
import java.util.Timer
import java.util.TimerTask

class MapsFragment : Fragment(), OnMapReadyCallback {
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var carMarker: Marker? = null
    private var userMarker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityMapsBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {

                    prepareViewModel()
                } else {

                    if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                        showPermissionRationale {
                            requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                        }
                    }
                }
            }
    }
    fun startTimer(period: Long) {
        val timer = Timer()
        val locationViewModel =
            ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)
        timer.schedule(object : TimerTask() {
            override fun run() {
                getLastLocation { location ->
                val currentLocation = locationViewModel.coordinates.value
                if (currentLocation == null || currentLocation != location) {
                    Log.d("MapsFragment", "Location updated: Lat=${location.latitude}, Lon=${location.longitude}")

                    locationViewModel.setCoordinates(location)
                }
                }
            }
        }, 0, period)

    }
    private fun prepareViewModel() {
        val locationViewModel =
            ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)
        locationViewModel.coordinates.observe(viewLifecycleOwner) { coordinates ->
            coordinates?.let {
                updateMapLocation(it)
                updateUserMarker(it)
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
            }
        }
        view?.findViewById<Button>(R.id.im_parked_here_button)?.setOnClickListener {
            getLastLocation { location ->

                updateCarMarker(location)
            }
            locationViewModel.increaseTotal()
        }

        startTimer(1000)
        }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        when {
            hasLocationPermission() -> prepareViewModel()
            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }
            else -> requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }

    }
    @SuppressLint("MissingPermission")
    private fun getLastLocation(callback: (LatLng) -> Unit) {
        Log.d("MapsFragment", "getLastLocation() called.")
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLocation = LatLng(it.latitude, it.longitude)
                    Log.d("MapsFragment", "Location retrieved: Lat=${location.latitude}, Lon=${location.longitude}")
                    callback(currentLocation)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MapsFragment", "Failed to get location", exception)
            }
    }
    private fun updateMapLocation(location: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 7f))
    }
    private fun addMarkerAtLocation(
        location: LatLng, title: String,
        markerIcon: BitmapDescriptor? = null) = mMap.addMarker(
        MarkerOptions().title(title).position(location)
            .apply { markerIcon?.let { icon(markerIcon) } }
    )

    private fun updateUserMarker(latLng: LatLng) {
        if (userMarker == null) {
            userMarker = addMarkerAtLocation(latLng, "You're Here",
                getBitmapDescriptorFromVector(R.drawable.user_pin)
            )
        } else { userMarker?.apply { position = latLng } }
    }


    private fun getBitmapDescriptorFromVector(@DrawableRes
                                              vectorDrawableResourceId: Int): BitmapDescriptor? {
        val bitmap = ContextCompat.getDrawable(requireContext(),
            vectorDrawableResourceId)?.let { vectorDrawable ->
            vectorDrawable.setBounds(0, 0,
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight)
            val drawableWithTint = DrawableCompat
                .wrap(vectorDrawable)
            DrawableCompat.setTint(drawableWithTint,
                Color.RED)
            val bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawableWithTint.draw(canvas)
            bitmap
        }?: return null
        return BitmapDescriptorFactory.fromBitmap(bitmap)
            .also { bitmap?.recycle() }
    }



    private fun updateCarMarker(latLng: LatLng) {
        if (carMarker == null) {
            carMarker = addMarkerAtLocation(latLng, "My Parking Spot",
                getBitmapDescriptorFromVector(R.drawable.car_pin)
            )
        } else { carMarker?.apply { position = latLng } }
    }



    private fun hasLocationPermission() =

        ContextCompat.checkSelfPermission(
            requireContext(),
            ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    private fun showPermissionRationale(
        positiveAction: () -> Unit
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle("Location permission")
            .setMessage("We need your permission to find your current position")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                positiveAction()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create().show()
    }

}
