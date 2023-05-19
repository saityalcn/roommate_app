package com.example.estdate.ui.dashboard

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.estdate.R
import com.example.estdate.databinding.FragmentDashboardBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class DashboardFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        if(_binding != null) {
            Log.d("TAG", "MSG")

        }

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.mapView

        mapView.onCreate(savedInstanceState)

        initMapView()

        mapView.onResume()
    }

    override fun onResume() {
        super.onResume()
        initMapView()

        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()

        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()

        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()

        mapView.onLowMemory()
    }

    override fun onMapReady(map: GoogleMap) {

    }

    fun initMapView(){
        val mapView = binding.mapView
        mapView.getMapAsync { googleMap ->
            googleMap.setOnMapLoadedCallback {
                val latLng = LatLng(39.00, 37.00)
                googleMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
            }
        }
    }


}