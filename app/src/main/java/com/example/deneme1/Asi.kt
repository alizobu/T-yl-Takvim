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
import com.example.deneme1.databinding.ActivityAsiBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.os.Handler
import android.os.Looper
import android.widget.Button
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Asi : AppCompatActivity() {
    private lateinit var binding: ActivityAsiBinding
    private lateinit var spinnerKediCinsiyeti: Spinner
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asi)
        binding = ActivityAsiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        spinnerKediCinsiyeti = findViewById(R.id.kediadi_spinner)
        val checkBoxIcParazit = findViewById<CheckBox>(R.id.icparazit_checkbox)
        val checkBoxDisParazit = findViewById<CheckBox>(R.id.disparazit_checkbox)
        val checkBoxKarma = findViewById<CheckBox>(R.id.karma_checkbox)
        val checkBoxlosemi = findViewById<CheckBox>(R.id.losemi_checkbox)
        val checkBoxKuduz = findViewById<CheckBox>(R.id.kuduz_checkbox)
        val asitakvimButton = findViewById<Button>(R.id.asitakvim)
        val currentUser = auth.currentUser
        val kediAdlari = mutableListOf<String>()
        val kediIdleri = mutableListOf<String>()
        binding.asitakvim.setOnClickListener {
            val intent = Intent(this, Asitakvim::class.java)
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
                                val icParazitVarMi = kediBilgileri["icparazit"] as? Boolean ?: false
                                val disParazitVarMi = kediBilgileri["disparazit"] as? Boolean ?: false
                                val karmaVarMi = kediBilgileri["karma"] as? Boolean ?: false
                                val kumlosemiVarMi = kediBilgileri["losemi"] as? Boolean ?: false
                                val kuduzVarMi = kediBilgileri["kuduz"] as? Boolean ?: false
                                checkBoxIcParazit.isChecked = icParazitVarMi
                                checkBoxDisParazit.isChecked = disParazitVarMi
                                checkBoxKarma.isChecked = karmaVarMi
                                checkBoxlosemi.isChecked = kumlosemiVarMi
                                checkBoxKuduz.isChecked = kuduzVarMi
                                val twoMonthsInMilliseconds = 2 * 30 * 24 * 60 * 60 * 1000L
                                val oneYearInMilliseconds = 365 * 24 * 60 * 60 * 1000L

                                Handler(Looper.getMainLooper()).postDelayed({
                                    secilenKediId?.let { kediId ->
                                        db.collection("users").document(currentUser.uid).collection("kediBilgileri")
                                            .document(kediId)
                                            .update(mapOf(
                                                "icparazit" to false,
                                                "disparazit" to false
                                            ))
                                    }
                                }, twoMonthsInMilliseconds)

                                Handler(Looper.getMainLooper()).postDelayed({
                                    secilenKediId?.let { kediId ->
                                        db.collection("users").document(currentUser.uid).collection("kediBilgileri")
                                            .document(kediId)
                                            .update(mapOf(
                                                "karma" to false,
                                                "losemi" to false,
                                                "kuduz" to false
                                            ))
                                    }
                                }, oneYearInMilliseconds)
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
        fun getCurrentDateTime(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDateAndTime: String = dateFormat.format(Date())
            return currentDateAndTime
        }
        checkBoxIcParazit.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                secilenKediId?.let { kediId ->
                    val tarih = getCurrentDateTime()
                    val checkboxId = "İç Parazit"

                    val yeniBelge = hashMapOf(
                        "kediId" to kediId,
                        "checkboxId" to checkboxId,
                        "tarih" to tarih
                    )

                    db.collection("checkboxLog")
                        .add(yeniBelge)
                        .addOnSuccessListener { documentReference ->
                            db.collection("users").document(currentUser!!.uid).collection("kediBilgileri")
                                .document(kediId)
                                .update("icparazit", isChecked)
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
        checkBoxDisParazit.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                secilenKediId?.let { kediId ->
                    val tarih = getCurrentDateTime()
                    val checkboxId = "Dış Parazit"

                    val yeniBelge = hashMapOf(
                        "kediId" to kediId,
                        "checkboxId" to checkboxId,
                        "tarih" to tarih
                    )

                    db.collection("checkboxLog")
                        .add(yeniBelge)
                        .addOnSuccessListener { documentReference ->
                            db.collection("users").document(currentUser!!.uid).collection("kediBilgileri")
                                .document(kediId)
                                .update("disparazit", isChecked)
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

        checkBoxKarma.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                secilenKediId?.let { kediId ->
                    val tarih = getCurrentDateTime()
                    val checkboxId = "Karma"

                    val yeniBelge = hashMapOf(
                        "kediId" to kediId,
                        "checkboxId" to checkboxId,
                        "tarih" to tarih
                    )

                    db.collection("checkboxLog")
                        .add(yeniBelge)
                        .addOnSuccessListener { documentReference ->
                            db.collection("users").document(currentUser!!.uid).collection("kediBilgileri")
                                .document(kediId)
                                .update("karma", isChecked)
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

        checkBoxlosemi.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                secilenKediId?.let { kediId ->
                    val tarih = getCurrentDateTime()
                    val checkboxId = "Lösemi"

                    val yeniBelge = hashMapOf(
                        "kediId" to kediId,
                        "checkboxId" to checkboxId,
                        "tarih" to tarih
                    )

                    db.collection("checkboxLog")
                        .add(yeniBelge)
                        .addOnSuccessListener { documentReference ->
                            db.collection("users").document(currentUser!!.uid).collection("kediBilgileri")
                                .document(kediId)
                                .update("losemi", isChecked)
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

        checkBoxKuduz.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                secilenKediId?.let { kediId ->
                    val tarih = getCurrentDateTime()
                    val checkboxId = "Kuduz"

                    val yeniBelge = hashMapOf(
                        "kediId" to kediId,
                        "checkboxId" to checkboxId,
                        "tarih" to tarih
                    )

                    db.collection("checkboxLog")
                        .add(yeniBelge)
                        .addOnSuccessListener { documentReference ->
                            db.collection("users").document(currentUser!!.uid).collection("kediBilgileri")
                                .document(kediId)
                                .update("kuduz", isChecked)
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
}
