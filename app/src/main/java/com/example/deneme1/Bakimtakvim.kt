package com.example.deneme1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.deneme1.databinding.ActivityBakimtakvimBinding

class Bakimtakvim : AppCompatActivity() {
    private lateinit var binding: ActivityBakimtakvimBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBakimtakvimBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        val kediAdlari = mutableListOf<String>()
        val kediIdleri = mutableListOf<String>()

        binding.geridonbtn.setOnClickListener {
            val intent = Intent(this, Bakim::class.java)
            startActivity(intent)
        }

        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).collection("kediBilgileri")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val kediId = document.id
                        val kediAdi = document.getString("adi")
                        kediIdleri.add(kediId)
                        kediAdi?.let {
                            kediAdlari.add(it)
                        }
                    }
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kediAdlari)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.kediadiSpinner.adapter = adapter
                }
                .addOnFailureListener { exception ->
                    Log.e("Bakimtakvim", "Kedi bilgileri alınamadı: ${exception.message}")
                }
        }

        var secilenKediId: String? = null
        binding.kediadiSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                secilenKediId = kediIdleri[position]
                Log.d("Bakimtakvim", "Seçilen kedi ID: $secilenKediId")
                secilenKediId?.let {
                    loadBakimTakvim(it)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        loadBakimTakvim(secilenKediId ?: "")
    }

    private fun loadBakimTakvim(kediId: String) {
        val bakimTakvimLayout = binding.bakimTakvimLayout
        bakimTakvimLayout.removeAllViews()

        Log.d("Bakimtakvim", "LoadBakimTakvim metodu çağrıldı. Kedi ID: $kediId")

        db.collection("checkboxLog2")
            .whereEqualTo("kediId", kediId)
            .orderBy("tarih", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("Bakimtakvim", "No documents found for kediId: $kediId")
                }
                for (document in documents) {
                    val bakimTuru = document.getString("checkboxId") ?: ""
                    val tarih = document.getString("tarih") ?: ""

                    Log.d("Bakimtakvim", "Bakım Türü: $bakimTuru, Tarih: $tarih")

                    val tableRow = TableRow(this)

                    val textViewBakimTuru = TextView(this)
                    textViewBakimTuru.text = bakimTuru
                    textViewBakimTuru.setPadding(10, 10, 10, 10)

                    val textViewTarih = TextView(this)
                    textViewTarih.text = tarih
                    textViewTarih.setPadding(10, 10, 10, 10)

                    tableRow.addView(textViewBakimTuru)
                    tableRow.addView(textViewTarih)

                    bakimTakvimLayout.addView(tableRow)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Bakimtakvim", "Error loading bakimTakvim: ${e.message}")
            }
    }
}
