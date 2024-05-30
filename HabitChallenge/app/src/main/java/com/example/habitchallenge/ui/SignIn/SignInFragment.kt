package com.example.habitchallenge.ui.SignIn

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.habitchallenge.R
import com.example.habitchallenge.databinding.FragmentSignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class SignInFragment : Fragment() {
    private lateinit var auth: FirebaseAuth;
    private lateinit var navControl: NavController;
    private lateinit var binding: FragmentSignUpBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        registerEvents()
    }

    private fun init(view: View) {
        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
    }

    private fun registerEvents() {

        binding.signUp.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val repeatPassword = binding.repeatPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && repeatPassword.isNotEmpty()) {
                if (password == repeatPassword) {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                context,
                                "Регистрация прошла успешно!",
                                Toast.LENGTH_SHORT
                            ).show()
                            val userID = auth.currentUser?.uid;
                            navControl.navigate(R.id.action_signUpFragment_to_navigation_home)
                        } else {
                            Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
    }

}