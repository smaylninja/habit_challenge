package com.example.habitchallenge

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging


class SplashFragment : Fragment() {
    private lateinit var auth: FirebaseAuth;
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)

        val isLogin: Boolean = auth.currentUser != null

        val handler = Handler(Looper.myLooper()!!)

        handler.postDelayed({
            if (isLogin)
                navController.navigate(R.id.action_splashFragment_to_navigation_home)
            else
                navController.navigate(R.id.action_splashFragment_to_loginFragment)
        }, 1500)
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(view)

        // Получение токена устройства

    }
}