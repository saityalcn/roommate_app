package com.example.estdate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.example.estdate.databinding.FragmentNotificationsBinding
import com.example.estdate.databinding.FragmentProfileBinding
import com.example.estdate.models.Student
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(arguments?.getString("uid") != null)
            userId = arguments?.getString("uid")!!

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val auth = Firebase.auth

        binding.signOut.setOnClickListener{
            Firebase.auth.signOut()
            replaceFragment(ProfileNotAuthenticatedFragment())
        }

        binding.editProfile.setOnClickListener{
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            requireContext().startActivity(intent)
        }

        binding.showRequestsBtn.setOnClickListener {
            val intent = Intent(requireContext(), RequestsActivity::class.java)
            requireContext().startActivity(intent)
        }

        if(auth.currentUser != null && auth.currentUser!!.uid == userId){
            binding.sendMatchRequestBtn.visibility = View.GONE
            binding.showRequestsBtn.visibility = View.VISIBLE
            binding.sendMessage.visibility = View.GONE
            binding.redirectToMailImage.visibility = View.GONE
        } else {
            binding.sendMatchRequestBtn.visibility = View.VISIBLE
            binding.showRequestsBtn.visibility = View.GONE
            binding.signOut.visibility = View.GONE
            binding.editProfile.visibility = View.GONE
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        initPage(requireView())
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPage(view)
    }


    fun initPage(view: View){
        val db = Firebase.firestore
        val auth = Firebase.auth

        binding.contentWrapper.visibility = View.INVISIBLE

        db.collection("students").document(userId).get().addOnSuccessListener {
            val student: Student = Student.fromDocumentSnapshot(it)
            binding.progressBar.visibility = View.INVISIBLE
            binding.contentWrapper.visibility = View.VISIBLE
            binding.textViewGradName.text = student.name + " " + student.surname
            binding.sendMatchRequestBtn.setOnClickListener {
                if(auth.currentUser != null){
                    sendRequestNotification(student)
                }
                else{
                    val intent = Intent(view.context, LoginActivity::class.java)
                    view.context.startActivity(intent)
                }
            }
            Glide.with(requireView())
                .load(Uri.parse(student.profileImageUrl))
                .into(binding.imageViewProfilePhoto)

            binding.sendMessage.setOnClickListener{
                wpMessage(student.contactPhone)
            }
            binding.redirectToMailImage.setOnClickListener{
                sendEmail(student.contactEmail)
            }
        }
    }

    fun sendRequestNotification(student: Student){
        val fcmToken = student.fcmToken
        val notificationId = System.currentTimeMillis().toInt()
        val db = Firebase.firestore
        val auth = Firebase.auth

        if(auth.currentUser != null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    db.collection("students").document(auth.currentUser!!.uid).update(
                        mapOf("fcmToken" to token) as Map<String, String>
                    )
                }
            }
        }

        Log.v("TAG", student.fcmToken)

        FirebaseMessaging.getInstance().send(
            RemoteMessage.Builder(fcmToken)
                .setMessageId(notificationId.toString())
                .addData("title", "Yeni Eşleşme Talebi")
                .addData("body", "Yeni Eşleşme Talebiiii")
                .build()
        )

    }


    fun wpMessage(phoneNumber: String){
        val message = "\nMezunApp üzerinden gönderildi."
        val whatsappUrl = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(whatsappUrl)
        startActivity(intent)
    }

    fun sendEmail(email: String){
        if(email != "") {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_TEXT, "Eşleşme Talebi")
            }

            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            }
        }else{
            showErrorSnackbar(requireView(), "Kullanıcının e-postası çekilirken bir hatayla karşılaştık. Daha sonra tekrar deneyiniz.")
        }
    }
    fun showErrorSnackbar(view: View, message: String){
        val snack: Snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val view = snack.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        view.setBackgroundColor(Color.RED)
        snack.show()
    }

    fun replaceFragment(fragment: Fragment){
        val fragmentManager: FragmentManager = parentFragmentManager
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}