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
import com.example.deneme1.databinding.ActivityBeslenmeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Beslenme : AppCompatActivity() {
    private lateinit var binding: ActivityBeslenmeBinding
    private lateinit var spinnerKediCinsiyeti: Spinner
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBeslenmeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.beslenmetakvim.setOnClickListener {
            val intent = Intent(this, Beslenmetakvim::class.java)
            startActivity(intent)
        }
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        spinnerKediCinsiyeti = findViewById(R.id.kediadi_spinner)
        val checkBoxSu = findViewById<CheckBox>(R.id.su_checkbox)
        val checkBoxMama = findViewById<CheckBox>(R.id.mama_checkbox)
        val checkBoxMalt = findViewById<CheckBox>(R.id.malt_checkbox)
        val checkBoxYasMama = findViewById<CheckBox>(R.id.yasmama_checkbox)
        val currentUser = auth.currentUser
        val kediAdlari = mutableListOf<String>()
        val kediIdleri = mutableListOf<String>()

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
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                secilenKediId = kediIdleri[position]
                if (secilenKediId != null) {
                    db.collection("users").document(currentUser!!.uid).collection("kediBilgileri")
                        .document(secilenKediId!!)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            val kediBilgileri = documentSnapshot.data
                            if (kediBilgileri != null) {
                                val mamaVarMi = kediBilgileri["mama"] as? Boolean ?: false
                                val suVarMi = kediBilgileri["su"] as? Boolean ?: false
                                val maltVarMi = kediBilgileri["malt"] as? Boolean ?: false
                                val yasMamaVarMi = kediBilgileri["yasmama"] as? Boolean ?: false
                                checkBoxSu.isChecked = suVarMi
                                checkBoxMama.isChecked = mamaVarMi
                                checkBoxMalt.isChecked = maltVarMi
                                checkBoxYasMama.isChecked = yasMamaVarMi

                                val sixHoursInMillis = 6 * 60 * 60 * 1000L
                                val twelveHoursInMillis = 12 * 60 * 60 * 1000L
                                val oneDayInMillis = 24 * 60 * 60 * 1000L
                                val twoDaysInMillis = 2 * oneDayInMillis
                                val handler = Handler(Looper.getMainLooper())

                                handler.postDelayed({
                                    updateCheckbox(currentUser.uid, secilenKediId!!, "su", false)
                                }, sixHoursInMillis)

                                handler.postDelayed({
                                    updateCheckbox(currentUser.uid, secilenKediId!!, "mama", false)
                                }, twelveHoursInMillis)

                                handler.postDelayed({
                                    updateCheckbox(currentUser.uid, secilenKediId!!, "malt", false)
                                }, oneDayInMillis)

                                handler.postDelayed({
                                    updateCheckbox(currentUser.uid, secilenKediId!!, "yasmama", false)
                                }, twoDaysInMillis)
                            }
                        }
                        .addOnFailureListener { e ->
                        }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkBoxSu.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                secilenKediId?.let { kediId ->
                    val tarih = getCurrentDateTime()
                    val checkboxId = "Su"

                    val yeniBelge = hashMapOf(
                        "kediId" to kediId,
                        "checkboxId" to checkboxId,
                        "tarih" to tarih
                    )

                    db.collection("checkboxLog1")
                        .add(yeniBelge)
                        .addOnSuccessListener { documentReference ->
                            updateCheckbox(currentUser!!.uid, kediId, "su", isChecked)
                        }
                        .addOnFailureListener { e ->
                        }
                }
            } else {
                secilenKediId?.let { kediId ->
                    updateCheckbox(currentUser!!.uid, kediId, "su", isChecked)
                }
            }
        }

        checkBoxMama.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                secilenKediId?.let { kediId ->
                    val tarih = getCurrentDateTime()
                    val checkboxId = "Mama"

                    val yeniBelge = hashMapOf(
                        "kediId" to kediId,
                        "checkboxId" to checkboxId,
                        "tarih" to tarih
                    )

                    db.collection("checkboxLog1")
                        .add(yeniBelge)
                        .addOnSuccessListener { documentReference ->
                            updateCheckbox(currentUser!!.uid, kediId, "mama", isChecked)
                        }
                        .addOnFailureListener { e ->
                        }
                }
            } else {
                secilenKediId?.let { kediId ->
                    updateCheckbox(currentUser!!.uid, kediId, "mama", isChecked)
                }
            }
        }

        checkBoxMalt.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                secilenKediId?.let { kediId ->
                    val tarih = getCurrentDateTime()
                    val checkboxId = "Malt"

                    val yeniBelge = hashMapOf(
                        "kediId" to kediId,
                        "checkboxId" to checkboxId,
                        "tarih" to tarih
                    )

                    db.collection("checkboxLog1")
                        .add(yeniBelge)
                        .addOnSuccessListener { documentReference ->
                            updateCheckbox(currentUser!!.uid, kediId, "malt", isChecked)
                        }
                        .addOnFailureListener { e ->
                        }
                }
            } else {
                secilenKediId?.let { kediId ->
                    updateCheckbox(currentUser!!.uid, kediId, "malt", isChecked)
                }
            }
        }

        checkBoxYasMama.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                secilenKediId?.let { kediId ->
                    val tarih = getCurrentDateTime()
                    val checkboxId = "Yaş Mama"

                    val yeniBelge = hashMapOf(
                        "kediId" to kediId,
                        "checkboxId" to checkboxId,
                        "tarih" to tarih
                    )

                    db.collection("checkboxLog1")
                        .add(yeniBelge)
                        .addOnSuccessListener { documentReference ->
                            updateCheckbox(currentUser!!.uid, kediId, "yasmama", isChecked)
                        }
                        .addOnFailureListener { e ->
                        }
                }
            } else {
                secilenKediId?.let { kediId ->
                    updateCheckbox(currentUser!!.uid, kediId, "yasmama", isChecked)
                }
            }
        }
    }

    private fun updateCheckbox(userId: String, kediId: String, field: String, value: Boolean) {
        db.collection("users").document(userId).collection("kediBilgileri").document(kediId)
            .update(field, value)
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->

            }
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
}
