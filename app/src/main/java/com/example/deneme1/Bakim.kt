package com.example.deneme1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deneme1.databinding.ActivityBakimBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Bakim : AppCompatActivity() {
    private lateinit var binding: ActivityBakimBinding
    private lateinit var spinnerKediCinsiyeti: Spinner
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bakim)
        binding = ActivityBakimBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        spinnerKediCinsiyeti = findViewById(R.id.kediadi_spinner)
        val checkBoxTirmalama = findViewById<CheckBox>(R.id.scratch_checkbox)
        val checkBoxZil = findViewById<CheckBox>(R.id.bell_checkbox)
        val checkBoxGozKulak = findViewById<CheckBox>(R.id.gozkulaktemizlik_checkbox)
        val checkBoxKumTemizlik = findViewById<CheckBox>(R.id.kumtemizlik_checkbox)
        val checkBoxTarama = findViewById<CheckBox>(R.id.tarama_checkbox)
        val checkBoxKumDegisim = findViewById<CheckBox>(R.id.kumdegisim_checkbox)
        val currentUser = auth.currentUser
        val kediAdlari = mutableListOf<String>()
        val kediIdleri = mutableListOf<String>()
        binding.bakimtakvim.setOnClickListener {
            val intent = Intent(this, Bakimtakvim::class.java)
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
                        kediAdi?.let { kediAdlari.add(it) }
                    }
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kediAdlari)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerKediCinsiyeti.adapter = adapter
                }
                .addOnFailureListener { exception ->

                }
        }
        var secilenKediId: String? = null
        spinnerKediCinsiyeti.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val secilenKediIsmi = kediAdlari[position]
                secilenKediId = kediIdleri[position]
                if (secilenKediId != null) {
                    db.collection("users").document(currentUser!!.uid).collection("kediBilgileri")
                        .document(secilenKediId!!)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            val kediBilgileri = documentSnapshot.data
                            if (kediBilgileri != null) {
                                val tirmalamaVarMi = kediBilgileri["tirmalama"] as? Boolean ?: false
                                val zilVarMi = kediBilgileri["zil"] as? Boolean ?: false
                                val gozkulakVarMi = kediBilgileri["gozkulak"] as? Boolean ?: false
                                val kumVarMi = kediBilgileri["kum"] as? Boolean ?: false
                                val taramaVarMi = kediBilgileri["tarama"] as? Boolean ?: false
                                val kumdegisimVarMi = kediBilgileri["kumdegisim"] as? Boolean ?: false
                                checkBoxTirmalama.isChecked = tirmalamaVarMi
                                checkBoxZil.isChecked = zilVarMi
                                checkBoxGozKulak.isChecked = gozkulakVarMi
                                checkBoxKumTemizlik.isChecked = kumVarMi
                                checkBoxTarama.isChecked = taramaVarMi
                                checkBoxKumDegisim.isChecked = kumdegisimVarMi


                                val oneDayInMillis = 24 * 60 * 60 * 1000L
                                val oneWeekInMillis = 7 * oneDayInMillis
                                val handler = Handler(Looper.getMainLooper())


                                handler.postDelayed({
                                    updateCheckbox(currentUser.uid, secilenKediId!!, "tirmalama", false)
                                    updateCheckbox(currentUser.uid, secilenKediId!!, "zil", false)
                                }, oneDayInMillis)


                                handler.postDelayed({
                                    updateCheckbox(currentUser.uid, secilenKediId!!, "gozkulak", false)
                                    updateCheckbox(currentUser.uid, secilenKediId!!, "kum", false)
                                }, oneDayInMillis)


                                handler.postDelayed({
                                    updateCheckbox(currentUser.uid, secilenKediId!!, "kumdegisim", false)
                                    updateCheckbox(currentUser.uid, secilenKediId!!, "tarama", false)
                                }, oneWeekInMillis)
                            }
                        }
                        .addOnFailureListener { e ->
                        }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkBoxTirmalama.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                secilenKediId?.let { kediId ->
                    val tarih = getCurrentDateTime()
                    val checkboxId = "Tırmalama Tahtası"

                    val yeniBelge = hashMapOf(
                        "kediId" to kediId,
                        "checkboxId" to checkboxId,
                        "tarih" to tarih
                    )

                    db.collection("checkboxLog2")
                        .add(yeniBelge)
                        .addOnSuccessListener { documentReference ->
                            db.collection("users").document(currentUser!!.uid).collection("kediBilgileri")
                                .document(kediId)
                                .update("tirmalama", isChecked)
                                .addOnSuccessListener {
                                }
                                .addOnFailureListener { e ->
                                }
                        }
                        .addOnFailureListener { e ->
                        }
                }
            }
        }

        checkBoxZil.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                secilenKediId?.let { kediId ->
                    val tarih = getCurrentDateTime()
                    val checkboxId = "Zil ile Oynatma"

                    val yeniBelge = hashMapOf(
                        "kediId" to kediId,
                        "checkboxId" to checkboxId,
                        "tarih" to tarih
                    )

                    db.collection("checkboxLog2")
                        .add(yeniBelge)
                        .addOnSuccessListener { documentReference ->
                            db.collection("users").document(currentUser!!.uid).collection("kediBilgileri")
                                .document(kediId)
                                .update("zil", isChecked)
                                .addOnSuccessListener {
                                }
                                .addOnFailureListener { e ->
                                }
                        }
                        .addOnFailureListener { e ->
                        }
                }
            }
        }

        checkBoxGozKulak.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                secilenKediId?.let { kediId ->
                    val tarih = getCurrentDateTime()
                    val checkboxId = "Göz Kulak Temizliği"

                    val yeniBelge = hashMapOf(
                        "kediId" to kediId,
                        "checkboxId" to checkboxId,
                        "tarih" to tarih
                    )

                    db.collection("checkboxLog2")
                        .add(yeniBelge)
                        .addOnSuccessListener { documentReference ->
                            db.collection("users").document(currentUser!!.uid).collection("kediBilgileri")
                                .document(kediId)
                                .update("gozkulak", isChecked)
                                .addOnSuccessListener {
                                }
                                .addOnFailureListener { e ->
                                }
                        }
                        .addOnFailureListener { e ->
                        }
                }
            }
        }

        checkBoxKumTemizlik.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                secilenKediId?.let { kediId ->
                    val tarih = getCurrentDateTime()
                    val checkboxId = "Kum Temizliği"

                    val yeniBelge = hashMapOf(
                        "kediId" to kediId,
                        "checkboxId" to checkboxId,
                        "tarih" to tarih
                    )

                    db.collection("checkboxLog2")
                        .add(yeniBelge)
                        .addOnSuccessListener { documentReference ->
                            db.collection("users").document(currentUser!!.uid).collection("kediBilgileri")
                                .document(kediId)
                                .update("kum", isChecked)
                                .addOnSuccessListener {
                                }
                                .addOnFailureListener { e ->
                                }
                        }
                        .addOnFailureListener { e ->
                        }
                }
            }
        }

        checkBoxTarama.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                secilenKediId?.let { kediId ->
                    val tarih = getCurrentDateTime()
                    val checkboxId = "Tüyleri Tarama"

                    val yeniBelge = hashMapOf(
                        "kediId" to kediId,
                        "checkboxId" to checkboxId,
                        "tarih" to tarih
                    )

                    db.collection("checkboxLog2")
                        .add(yeniBelge)
                        .addOnSuccessListener { documentReference ->
                            db.collection("users").document(currentUser!!.uid).collection("kediBilgileri")
                                .document(kediId)
                                .update("tarama", isChecked)
                                .addOnSuccessListener {
                                }
                                .addOnFailureListener { e ->
                                }
                        }
                        .addOnFailureListener { e ->
                        }
                }
            }
        }

        checkBoxKumDegisim.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                secilenKediId?.let { kediId ->
                    val tarih = getCurrentDateTime()
                    val checkboxId = "Kum Değişimi"

                    val yeniBelge = hashMapOf(
                        "kediId" to kediId,
                        "checkboxId" to checkboxId,
                        "tarih" to tarih
                    )

                    db.collection("checkboxLog2")
                        .add(yeniBelge)
                        .addOnSuccessListener { documentReference ->
                            db.collection("users").document(currentUser!!.uid).collection("kediBilgileri")
                                .document(kediId)
                                .update("kumdegisim", isChecked)
                                .addOnSuccessListener {
                                }
                                .addOnFailureListener { e ->
                                }
                        }
                        .addOnFailureListener { e ->
                        }
                }
            }
        }
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun updateCheckbox(userId: String, kediId: String, field: String, value: Boolean) {
        db.collection("users").document(userId).collection("kediBilgileri")
            .document(kediId)
            .update(field, value)
            .addOnSuccessListener {
            }
            .addOnFailureListener { e ->
            }
    }
}
