package com.example.habitchallenge

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.habitchallenge.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityRegisterBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.registerButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val repeatPassword = binding.passwordRepeatInput.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty())
                if (password == repeatPassword)
                    MainActivity.auth.createUserWithEmailAndPassword(email, password)
                else
                    Toast.makeText(this, "Неверное подтверждение пароля", Toast.LENGTH_SHORT).show()
        }
    }
}