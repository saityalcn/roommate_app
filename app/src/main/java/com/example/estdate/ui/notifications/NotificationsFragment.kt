package com.example.estdate.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.example.estdate.*
import com.example.estdate.databinding.FragmentNotificationsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    val profileFragment = ProfileFragment()
    val nonAuthenticatedFragment = ProfileNotAuthenticatedFragment()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val auth = Firebase.auth

        if(auth.currentUser != null){
            val bundle = Bundle()
            bundle.putString("uid", auth.currentUser!!.uid)
            profileFragment.arguments = bundle
            replaceFragment(profileFragment)
        }


        else
            replaceFragment(nonAuthenticatedFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        val auth = Firebase.auth

        if(auth.currentUser != null){
            val bundle = Bundle()
            bundle.putString("uid", auth.currentUser!!.uid)
            profileFragment.arguments = bundle
            replaceFragment(profileFragment)
        }

        else
            replaceFragment(nonAuthenticatedFragment)

    }

    fun replaceFragment(fragment: Fragment){
        val fragmentManager: FragmentManager = parentFragmentManager
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment)
            .commit()
    }

}