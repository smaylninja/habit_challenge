package com.example.habitchallenge.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.habitchallenge.R
import com.example.habitchallenge.databinding.FragmentLogin2Binding
import com.example.habitchallenge.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth;
    private lateinit var db: FirebaseFirestore;
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentLogin2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogin2Binding.inflate(inflater, container, false)
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
        db = FirebaseFirestore.getInstance()
    }

    private fun registerEvents() {
        binding.register.setOnClickListener{
            navControl.navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        binding.login.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {task ->
                    if (task.isSuccessful)
                        navControl.navigate(R.id.action_loginFragment_to_navigation_home)
                    else
                        Toast.makeText(context, "Неверные почта или пароль", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}