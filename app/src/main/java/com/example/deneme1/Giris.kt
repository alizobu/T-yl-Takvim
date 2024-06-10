package com.example.deneme1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.deneme1.databinding.ActivityGirisBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Giris : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var binding: ActivityGirisBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth= Firebase.auth
        binding=ActivityGirisBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.text1.setOnClickListener{
            val intent=Intent(this,Kayit::class.java)
            startActivity(intent)
        }
    }

    fun girisyap(view:View){
        val email=binding.emailhere.text.toString()
        val password=binding.passhere.text.toString()
        if (email.equals("")||password.equals("")){
            Toast.makeText(this,"Lütfen doğru bir şekilde password ve emailinizi giriniz.",Toast.LENGTH_LONG).show()

        }
        else{
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
                val intent= Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()

            }.addOnFailureListener{
                Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()

            }
        }
    }
}