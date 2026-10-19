package com.example.mydaily

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menyembunyikan ActionBar bawaan
        supportActionBar?.hide()

        // Menghubungkan Kotlin dengan layout XML
        setContentView(R.layout.activity_login)

        // Menghubungkan komponen dari XML
        val username = findViewById<EditText>(R.id.etUsername)
        val password = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Ketika tombol MASUK diklik
        btnLogin.setOnClickListener {

            val user = username.text.toString().trim()
            val pass = password.text.toString()

            if (user.isEmpty() || pass.isEmpty()) {

                Toast.makeText(
                    this,
                    "Username dan password wajib diisi!",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                // Pindah ke halaman Home
                val intent = Intent(
                    this,
                    MainActivity::class.java
                )

                // Mengirim username ke halaman Home
                intent.putExtra("USERNAME", user)

                startActivity(intent)
                finish()
            }
        }
    }
}
