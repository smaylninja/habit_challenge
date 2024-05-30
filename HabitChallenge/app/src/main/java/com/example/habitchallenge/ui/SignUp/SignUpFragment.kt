package com.example.habitchallenge.ui.SignUp

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.habitchallenge.R
import com.example.habitchallenge.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpFragment : Fragment() {
    private lateinit var auth: FirebaseAuth;
    private lateinit var db: FirebaseFirestore;
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentSignUpBinding

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
        db = FirebaseFirestore.getInstance()
    }

    private fun registerEvents() {
        binding.goToSignUp.setOnClickListener {
            Toast.makeText(
                context,
                "Навигация!",
                Toast.LENGTH_SHORT
            ).show()
            navControl.navigate(R.id.action_signUpFragment_to_loginFragment)
        }

        binding.signUp.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val repeatPassword = binding.repeatPassword.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && repeatPassword.isNotEmpty()) {
                if (password == repeatPassword) {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val userID = auth.currentUser?.uid;
                            val user: HashMap<String, Any> = hashMapOf(
                                "username" to name
                            )
                            db.collection("users").document(userID.toString()).set(user).addOnSuccessListener {
                                Log.d(TAG, "Данные успешно добавлены в Firestore для $userID!")
                            }.addOnFailureListener { e ->
                                Log.d(TAG, "Ошибка: ${e.toString()}")
                            }
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