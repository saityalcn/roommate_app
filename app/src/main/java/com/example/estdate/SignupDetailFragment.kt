package com.example.estdate

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.estdate.models.Student
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignupDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignupDetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var progressBar: ProgressBar;

    private val statesList: MutableList<String> = mutableListOf("Kalacak Ev/Oda arıyor","Ev/Oda arkadaşı arıyor","Aramıyor")

    private val CAMERA_REQUEST_CODE = 100
    private var imageUri: Uri? = null

    lateinit var selectedImage: Uri
    lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                selectedImage = uri
                requireView().findViewById<ImageView>(R.id.imageViewSelectedPhoto)
                    .setImageURI(uri)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup_detail, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SignupDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignupDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = Firebase.auth

        requireView().findViewById<ProgressBar>(R.id.progressBar).visibility = View.INVISIBLE

        createPopupMenu()

        requireView().findViewById<Button>(R.id.saveBtn).setOnClickListener {
            onSaveBtnClick(it)
        }


        val spinner = requireView().findViewById<Spinner>(R.id.stateSelectSpinner)
        if (spinner != null) {
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item, statesList
            )
            spinner.adapter = adapter
        }

        if(auth.currentUser != null) {
            Firebase.firestore.collection("graduates").document(auth.currentUser!!.uid).get().addOnSuccessListener {
                if(it["uid"] != null)
                    initFields()
            }
        }
    }

    fun initFields(){
        val db = Firebase.firestore
        val auth = Firebase.auth

        val progressBar = requireView().findViewById<ProgressBar>(R.id.progressBar)



    }



    fun createPopupMenu(){
        val button = requireView().findViewById<ImageView>(R.id.imageViewSelectPhoto)
        val popupMenu = PopupMenu(requireView().context, button)
        popupMenu.menuInflater.inflate(R.menu.addphoto_popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.openCamera -> {
                    if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_REQUEST_CODE)
                    }

                    if(ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_REQUEST_CODE)
                    }

                    if(ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        openCamera()
                    }

                    true
                }
                R.id.selectGallery -> {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    true
                }
                else -> false
            }
        }
        button.setOnClickListener {
            popupMenu.show()
        }
    }

    fun openCamera(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.DESCRIPTION, "")
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = activity?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
    }

    fun onSaveBtnClick(view: View){
        onCreateBtnClick(view)
    }

    fun onCreateBtnClick(view: View){
        val auth = Firebase.auth
        val db = Firebase.firestore
        val progressBar = requireView().findViewById<ProgressBar>(R.id.progressBar)
        val scrollView = requireView().findViewById<ScrollView>(R.id.scrollView)

        val name = requireView().findViewById<EditText>(R.id.editTextName).text.toString()
        val surname = requireView().findViewById<EditText>(R.id.editTextSurname).text.toString()

        val department = requireView().findViewById<EditText>(R.id.editTextDepartment).text.toString()
        val grade = requireView().findViewById<EditText>(R.id.editTextGrade).text.toString()
        val selectedState: String = requireView().findViewById<Spinner>(R.id.stateSelectSpinner).selectedItem as String
        val distance = requireView().findViewById<EditText>(R.id.editTextDistance).text.toString()
        val duration = requireView().findViewById<EditText>(R.id.editTextYears).text.toString()
        val contactEmail = requireView().findViewById<EditText>(R.id.editTextEmail).text.toString()
        val contactPhone = requireView().findViewById<EditText>(R.id.editTextPhoneNumber).text.toString()

        progressBar.visibility = View.VISIBLE
        scrollView.visibility = View.INVISIBLE

        uploadImage().addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener {uri ->
                val student: Student = Student(name, surname, auth.currentUser!!.uid, department,grade,selectedState,distance,duration,contactEmail,contactPhone,uri.toString())
                db.collection("students").document(auth.currentUser!!.uid).set(student.toMap())
                    .addOnSuccessListener {
                        val intent = Intent(activity, MainActivity::class.java)
                        startActivity(intent)
                    }.addOnFailureListener {
                        progressBar.visibility = View.INVISIBLE
                        scrollView.visibility = View.VISIBLE
                        showErrorSnackbar(view, it.localizedMessage)
                    }
            }.addOnFailureListener {
                progressBar.visibility = View.INVISIBLE
                scrollView.visibility = View.VISIBLE
                showErrorSnackbar(view, it.localizedMessage)
            }
        }
    }

    fun uploadImage(): UploadTask {
        var storageRef = Firebase.storage.reference
        val time = System.currentTimeMillis()
        val profileImageRef = storageRef.child("profiles/" + time.toString())
        val imageUploadTask = profileImageRef.putFile(selectedImage)
        return imageUploadTask
    }

    fun showErrorSnackbar(view: View, message: String) {
        val snack: Snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val view = snack.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        view.setBackgroundColor(Color.RED)
        snack.show()
    }


}