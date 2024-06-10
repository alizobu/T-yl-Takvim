package com.example.deneme1

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.deneme1.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).collection("kediBilgileri")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val linearLayout = createLinearLayout(document)
                        binding.rootLayout.addView(linearLayout)
                    }
                }
                .addOnFailureListener { exception ->
                }
        }

        binding.catadd.setOnClickListener{
            val intent = Intent(this, catadd2::class.java)
            startActivity(intent)
        }
        binding.bakim.setOnClickListener{
            val intent = Intent(this, Bakim::class.java)
            startActivity(intent)
        }
        binding.asi.setOnClickListener{
            val intent = Intent(this, Asi::class.java)
            startActivity(intent)
        }
        binding.beslenme.setOnClickListener{
            val intent = Intent(this, Beslenme::class.java)
            startActivity(intent)
        }
    }

    private fun createLinearLayout(document: com.google.firebase.firestore.DocumentSnapshot): LinearLayout {
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL

        val kediAdi = document.getString("adi")
        val kediYasi = document.getString("yasi")
        val kediCinsi = document.getString("cinsi")
        val vetBilgi = document.getString("vet_bilgi")

        val adiTextView = TextView(this)
        adiTextView.text = "Kedi Adı: $kediAdi"
        linearLayout.addView(adiTextView)

        val yasiTextView = TextView(this)
        yasiTextView.text = "Kedi Yaşı: $kediYasi"
        linearLayout.addView(yasiTextView)

        val cinsiTextView = TextView(this)
        cinsiTextView.text = "Kedi Cinsi: $kediCinsi"
        linearLayout.addView(cinsiTextView)

        val vetBilgiTextView = TextView(this)
        vetBilgiTextView.text = "Veteriner Bilgisi: $vetBilgi"
        linearLayout.addView(vetBilgiTextView)

        return linearLayout
    }
}