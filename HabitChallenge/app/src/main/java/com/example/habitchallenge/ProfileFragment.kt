package com.example.habitchallenge

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.habitchallenge.databinding.FragmentHomeBinding
import com.example.habitchallenge.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth;
    private lateinit var db: FirebaseFirestore;
    private lateinit var navController: NavController;
    private lateinit var binding: FragmentProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        registerEvents()
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        navController = Navigation.findNavController(view)
    }

    private fun registerEvents() {
        binding.home.setOnClickListener{
            navController.navigate(R.id.action_profileFragment_to_navigation_home)
        }
        binding.logout.setOnClickListener{
            auth.signOut()
            navController.navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }
}