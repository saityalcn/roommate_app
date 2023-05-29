package com.example.estdate.ui.home

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.estdate.adapters.ListAdapter
import com.example.estdate.databinding.FragmentHomeBinding
import com.example.estdate.models.Student
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.estdate.R
import com.google.android.gms.location.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.messaging.FirebaseMessaging
import java.io.IOException
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    lateinit var studentsList: RecyclerView
    lateinit var studentAdapter: com.example.estdate.adapters.ListAdapter
    lateinit var students: MutableList<Student>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val auth = Firebase.auth
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0.lastLocation?.let { location ->
                    GetAddressTask().execute(location)
                }
            }
        }


        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return binding.root
    }

    fun checkFcmToken(student: Student){
        val auth = Firebase.auth
        val db = Firebase.firestore

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                student.fcmToken = token
            }

            val docRef = db.collection("students").document(auth.currentUser!!.uid)

            docRef.update(student.toMap())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager: LinearLayoutManager = LinearLayoutManager(context,
            LinearLayoutManager.VERTICAL,false)


        val db = Firebase.firestore
        val auth = Firebase.auth

        if(auth.currentUser != null) {
            db.collection("students").document(auth.currentUser!!.uid).get().addOnSuccessListener {
                val student: Student = Student.fromDocumentSnapshot(it)
                val imageView = binding.profileImage
                Glide.with(this)
                    .load(student.profileImageUrl)
                    .into(imageView)

                checkFcmToken(student)
            }
        }

        view.findViewById<ImageView>(R.id.filterBtn).setOnClickListener {
            createFilterPopup()
        }

        studentsList = view.findViewById<RecyclerView>(R.id.students_list)
        students = mutableListOf<Student>()


        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        progressBar.visibility = View.VISIBLE
        studentsList.visibility = View.GONE

        val searchView = requireView().findViewById<SearchView>(R.id.searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val newData:MutableList<Student> = mutableListOf<Student>()
                if(::studentAdapter.isInitialized) {
                    students.forEach {
                        if (it.name.toLowerCase().contains(newText!!.toLowerCase()) || it.surname.toLowerCase().contains(newText!!.toLowerCase())) {
                            newData.add(it)
                        }
                    }
                    studentAdapter.setData(newData)
                    studentAdapter.notifyDataSetChanged()
                }
                return true
            }

        })


        val studentsDb = db.collection("students")

        studentsDb.get().addOnSuccessListener {
            Log.d("TAG", it.size().toString())
            for (map in it) {
                val std: Student = Student.toObject(map)
                students.add(std)
            }
            studentAdapter = ListAdapter(list=students)
            studentsList.adapter = studentAdapter
            studentsList.layoutManager = layoutManager

            val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
            if(progressBar != null)
                progressBar.visibility = View.GONE

            studentsList.visibility = View.VISIBLE
        }.addOnFailureListener{
            Log.d("FAILURE", it.toString())
        }

    }

    override fun onStart() {
        super.onStart()
        if (checkLocationPermission()) {
            startLocationUpdates()
        } else {
            requestLocationPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    private fun checkLocationPermission(): Boolean {
        val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
        val granted = PackageManager.PERMISSION_GRANTED
        return ContextCompat.checkSelfPermission(requireContext(), permission) == granted
    }

    private fun requestLocationPermission() {
        val permission = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        val requestCode = 123
        ActivityCompat.requestPermissions(requireActivity(), permission, requestCode)
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

                if(maxDurationEditText.text.toString() != "")
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

        if(::studentAdapter.isInitialized){
            students.forEach{
                if ((it.state == state || state == "")){
                    if((it.distance.toInt() in (minDistance + 1) until maxDistance) && (it.duration.toInt() in (minDuration + 1) until maxDuration))
                        newData.add(it)
                }
            }
            studentAdapter.setData(newData)
            studentAdapter.notifyDataSetChanged()
        }
    }


    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }


        if (checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class GetAddressTask : AsyncTask<Location, Void, Address?>() {

        override fun doInBackground(vararg locations: Location): Address? {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val location = locations[0]
            var address: Address? = null
            val db = Firebase.firestore
            val auth = Firebase.auth
            try {
                if(auth.currentUser != null) {
                    db.collection("students").document(auth.currentUser!!.uid).update(
                        hashMapOf(
                            "locationLatitude" to location.latitude,
                            "locationLongitude" to location.longitude,
                        ) as Map<String, Long>
                    )
                }
                val addresses: List<Address>? = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1,

                )

                if (addresses!=null && addresses.isNotEmpty()) {
                    address = addresses[0]
                    val neighborhood = address.subLocality
                    val district = address.subAdminArea
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return address
        }

        override fun onPostExecute(address: Address?) {
            val auth = Firebase.auth
            val db = Firebase.firestore

            if (address != null) {
                val neighborhood = address.subLocality
                val district = address.subAdminArea

                if(auth.currentUser != null) {
                    db.collection("students").document(auth.currentUser!!.uid).update(
                        hashMapOf(
                            "address" to address.subAdminArea,
                        ) as Map<String, String>
                    )
                }

                binding.locationTextView.text = district
            }
        }
    }


}