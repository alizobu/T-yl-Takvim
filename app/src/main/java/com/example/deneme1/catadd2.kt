package com.example.deneme1

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class catadd2 : AppCompatActivity() {
    private lateinit var editTextKediAdi: EditText
    private lateinit var editTextKediYasi: EditText
    private lateinit var spinnerKediCinsi: Spinner
    private lateinit var spinnerKediCinsiyeti: Spinner
    private lateinit var editTextVetBilgi: EditText

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catadd2)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        editTextKediAdi = findViewById(R.id.kediadi)
        editTextKediYasi = findViewById(R.id.kediyasi)
        spinnerKediCinsi = findViewById(R.id.kedicins_spinner)
        spinnerKediCinsiyeti = findViewById(R.id.kedicinsiyet_spinner)
        editTextVetBilgi = findViewById(R.id.vetbilgi)
        val buttonKaydet = findViewById<Button>(R.id.geridonbtn)

        val kediCinsleri = arrayOf("Van Kedisi", "British Shorthair", "Sphynx", "Maine Coon", "Persian")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kediCinsleri)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerKediCinsi.adapter = adapter

        val kediCinsiyetleri = arrayOf("Erkek", "Dişi")
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, kediCinsiyetleri)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerKediCinsiyeti.adapter = adapter2

        buttonKaydet.setOnClickListener {
            val kediAdi = editTextKediAdi.text.toString()
            val kediYasi = editTextKediYasi.text.toString()
            val kediCinsi = spinnerKediCinsi.selectedItem.toString()
            val kediCinsiyeti = spinnerKediCinsiyeti.selectedItem.toString()
            val vetBilgi = editTextVetBilgi.text.toString()

            val currentUser = auth.currentUser
            val userId = currentUser?.uid

            if (userId != null) {
                val kedi = hashMapOf(
                    "adi" to kediAdi,
                    "yasi" to kediYasi,
                    "cinsi" to kediCinsi,
                    "cinsiyeti" to kediCinsiyeti,
                    "vet_bilgi" to vetBilgi,
                    "mama" to false,
                    "su" to false,
                    "yasmama" to false,
                    "malt" to false,
                    "icparazit" to false,
                    "disparazit" to false,
                    "karma" to false,
                    "losemi" to false,
                    "kuduz" to false,
                    "tirmalama" to false,
                    "zil" to false,
                    "gozkulak" to false,
                    "kum" to false,
                    "tarama" to false,
                    "kumdegisim" to false,
                )

                db.collection("users").document(userId).collection("kediBilgileri").add(kedi)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Kedi bilgileri başarıyla kaydedildi.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Hata oluştu: $e", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
