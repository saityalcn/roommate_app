package com.example.estdate.ui.dashboard

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.estdate.LoginActivity
import com.example.estdate.ProfileActivity
import com.example.estdate.R
import com.example.estdate.databinding.FragmentDashboardBinding
import com.example.estdate.models.Student
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DashboardFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var students: MutableList<Student>

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

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.mapView

        binding.fab.setImageResource(R.drawable.ic_filter)

        binding.fab.setOnClickListener{
            createFilterPopup()
        }

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
        val db = Firebase.firestore

        db.collection("students").get().addOnSuccessListener {
            val students: MutableList<Student> = mutableListOf()
            for(map in it){
                val std: Student = Student.toObject(map)
                students.add(std)
            }
            this.students = students
            mapView.getMapAsync { googleMap ->
                googleMap.setOnMapLoadedCallback {
                    students.forEach { item ->
                        Log.d("LATITUDE", item.locationLatitude.toString())
                        Log.d("LONGITUDE", item.locationLongitude.toString())
                        val latLng = LatLng(item.locationLatitude.toDouble(), item.locationLongitude.toDouble())
                        val marker = googleMap.addMarker(
                            MarkerOptions().position(latLng).title(item.name + " " + item.surname)
                        )
                        this.googleMap = googleMap
                        marker?.tag = item.uid
                        marker?.showInfoWindow()
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                        googleMap.setOnMarkerClickListener { marker ->
                            val intent: Intent = Intent(requireContext(), ProfileActivity::class.java)
                            intent.putExtra("uid", marker.tag.toString())
                            requireActivity().startActivity(intent)
                            true
                        }
                    }
                }
            }
        }
    }

    fun createFilterPopup(){
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())

        val dialogView = inflater.inflate(R.layout.dialog_filter, null)

        val minDistanceEditText = dialogView.findViewById<EditText>(R.id.etMinDistance)
        val maxDistanceEditText = dialogView.findViewById<EditText>(R.id.etMaxDistance)
        val minDurationEditText = dialogView.findViewById<EditText>(R.id.etMinDuration)
        val maxDurationEditText = dialogView.findViewById<EditText>(R.id.etMaxDuration)

        val states: MutableList<String> = mutableListOf<String>("","Kalacak Ev/Oda arıyor","Ev/Oda arkadaşı arıyor","Aramıyor")

        val spinner = dialogView.findViewById<Spinner>(R.id.stateSelectSpinner)
        if (spinner != null) {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item, states
            )
            spinner.adapter = adapter
        }

        builder.apply {
            setView(dialogView)
            setPositiveButton("Onayla") { _, _ ->
                var minDistance = 0
                var maxDistance = 5000

                var minDuration = 0
                var maxDuration = 5000

                if(minDistanceEditText.text.toString() != "")
                    minDistance = minDistanceEditText.text.toString().toInt()

                if(maxDistanceEditText.text.toString() != "")
                    maxDistance = maxDistanceEditText.text.toString().toInt()

                if(minDurationEditText.text.toString() != "")
                    minDuration = minDurationEditText.text.toString().toInt()

                if(maxDistanceEditText.text.toString() != "")
                    maxDuration = maxDurationEditText.text.toString().toInt()

                filter(minDistance,maxDistance,minDuration,maxDuration,spinner.selectedItem as String)
            }
            setNegativeButton("İptal") { _, _ ->
            }
        }

        builder.create().show()

    }

    fun filter(minDistance: Int,maxDistance: Int,minDuration: Int, maxDuration: Int, state: String){

        val newData:MutableList<Student> = mutableListOf<Student>()

        if(::googleMap.isInitialized){
            students.forEach{
                if ((it.state == state || state == "")){
                    if((it.distance.toInt() in (minDistance + 1) until maxDistance) && (it.duration.toInt() in (minDuration + 1) until maxDuration))
                        newData.add(it)
                }
            }

            googleMap.clear()
            newData.forEach{item ->
                val latLng = LatLng(item.locationLatitude.toDouble(), item.locationLongitude.toDouble())
                val marker = googleMap.addMarker(
                    MarkerOptions().position(latLng).title(item.name + " " + item.surname)
                )
                marker?.tag = item.uid
                marker?.showInfoWindow()
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                googleMap.setOnMarkerClickListener { marker ->
                    val intent: Intent = Intent(requireContext(), ProfileActivity::class.java)
                    intent.putExtra("uid", marker.tag.toString())
                    requireActivity().startActivity(intent)
                    true
                }
            }
        }
/*
        val newData:MutableList<Graduate> = mutableListOf<Graduate>()
        var temp: Int = maxGradYear

        if(maxGradYear == 0)
            temp = 5000

        if(::gradAdapter.isInitialized) {
            graduates.forEach {
                if (it.currentJobCity.toLowerCase().contains(city.toLowerCase()) && it.currentJobCountry.toLowerCase().contains(country.toLowerCase())){
                    if((it.graduateYear.toInt() in (minGradYear + 1) until temp) && (it.programName == program || program == ""))
                        newData.add(it)
                }
            }
            gradAdapter.setData(newData)
            gradAdapter.notifyDataSetChanged()
        }
        */
    }




}