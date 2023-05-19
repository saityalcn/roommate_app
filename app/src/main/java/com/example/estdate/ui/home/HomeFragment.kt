package com.example.estdate.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.estdate.adapters.ListAdapter
import com.example.estdate.databinding.FragmentHomeBinding
import com.example.estdate.models.Student
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.estdate.R

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    lateinit var studentsList: RecyclerView
    lateinit var studentAdapter: com.example.estdate.adapters.ListAdapter
    lateinit var students: MutableList<Student>

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

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager: LinearLayoutManager = LinearLayoutManager(context,
            LinearLayoutManager.VERTICAL,false)


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


        val db = Firebase.firestore

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

    fun createFilterPopup(){
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())

        val dialogView = inflater.inflate(R.layout.dialog_filter, null)

        val minGradYear = dialogView.findViewById<EditText>(R.id.etMinGradYear)
        val maxGradYear = dialogView.findViewById<EditText>(R.id.etMaxGradYear)
        val city = dialogView.findViewById<EditText>(R.id.etCity)
        val country = dialogView.findViewById<EditText>(R.id.etCountry)

        val programs: MutableList<String> = mutableListOf<String>()

        programs.add("")
        programs.add("Lisans")
        programs.add("Yüksek Lisans")
        programs.add("Doktora")

        val spinner = dialogView.findViewById<Spinner>(R.id.programSelectSpinner)
        if (spinner != null) {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item, programs
            )
            spinner.adapter = adapter
        }

        builder.apply {
            setView(dialogView)
            setPositiveButton("Onayla") { _, _ ->
                var minGradYearNum = 0
                var maxGradYearNum = 5000

                if(minGradYear.text.toString() != "")
                    minGradYearNum = minGradYear.text.toString().toInt()

                if(maxGradYear.text.toString() != "")
                    maxGradYearNum = maxGradYear.text.toString().toInt()

                val country = country.text.toString()
                val city = city.text.toString()
                val program = spinner.selectedItem.toString()

                filter(minGradYearNum,maxGradYearNum,country,city,program)
            }
            setNegativeButton("İptal") { _, _ ->
            }
        }

        builder.create().show()

    }

    fun filter(minGradYear: Int,maxGradYear: Int,country: String, city: String, program: String){

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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}