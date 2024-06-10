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
import com.example.deneme1.databinding.ActivityBeslenmetakvimBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Beslenmetakvim : AppCompatActivity() {
    private lateinit var binding: ActivityBeslenmetakvimBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBeslenmetakvimBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        val kediAdlari = mutableListOf<String>()
        val kediIdleri = mutableListOf<String>()

        binding.geridonbtn.setOnClickListener {
            val intent = Intent(this, Beslenme::class.java)
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
                    Log.e("Beslenmetakvim", "Kedi bilgileri alınamadı: ${exception.message}")
                }
        }

        var secilenKediId: String? = null
        binding.kediadiSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                secilenKediId = kediIdleri[position]
                secilenKediId?.let {
                    loadBeslenmeTakvim(it)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        loadBeslenmeTakvim(secilenKediId ?: "")
    }

    private fun loadBeslenmeTakvim(kediId: String) {
        val beslenmeTakvimLayout = binding.beslenmeTakvimLayout
        beslenmeTakvimLayout.removeAllViews()

        Log.d("Beslenmetakvim", "LoadBeslenmeTakvim metodu çağrıldı. Kedi ID: $kediId")

        db.collection("checkboxLog1")
            .whereEqualTo("kediId", kediId)
            .orderBy("tarih", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("Beslenmetakvim", "No documents found for kediId: $kediId")
                }
                for (document in documents) {
                    val besinAdi = document.getString("checkboxId") ?: ""
                    val beslenmeZamani = document.getString("tarih") ?: ""

                    Log.d("Beslenmetakvim", "Besin Adı: $besinAdi, Beslenme Zamanı: $beslenmeZamani")

                    val tableRow = TableRow(this)

                    val textViewBesinAdi = TextView(this)
                    textViewBesinAdi.text = besinAdi
                    textViewBesinAdi.setPadding(10, 10, 10, 10)

                    val textViewBeslenmeZamani = TextView(this)
                    textViewBeslenmeZamani.text = beslenmeZamani
                    textViewBeslenmeZamani.setPadding(10, 10, 10, 10)

                    tableRow.addView(textViewBesinAdi)
                    tableRow.addView(textViewBeslenmeZamani)

                    beslenmeTakvimLayout.addView(tableRow)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Beslenmetakvim", "Error loading beslenmeTakvim: ${e.message}")
            }
    }
}
