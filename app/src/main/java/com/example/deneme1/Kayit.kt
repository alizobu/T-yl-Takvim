package com.example.deneme1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deneme1.databinding.ActivityKayitBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Kayit : AppCompatActivity() {
    private lateinit var binding: ActivityKayitBinding

    private lateinit var auth:FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKayitBinding.inflate(layoutInflater)
        val view= binding.root
        setContentView(view)

        auth= Firebase.auth

    }

    fun kayitol(view: View){
        val email=binding.emailhere.text.toString()
        val password=binding.passhere.text.toString()
        if(email.equals("")||password.equals("")){
            Toast.makeText(this,"Doğru bir şekilde email ve şifreyi giriniz.",Toast.LENGTH_LONG).show()

        }
        else{
            auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener {
                val intent= Intent(this,Giris::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener{
                Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }

    }
}