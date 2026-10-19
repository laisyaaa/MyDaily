
package com.example.mydaily

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var pageBeranda: View
    private lateinit var pageCatatan: View
    private lateinit var pageProfil: View
    private lateinit var txtJudul: TextView

    // Data catatan
    private val daftarCatatan = mutableListOf<Catatan>()
    private var idSedangDiedit: Long? = null
    private var idBerikutnya: Long = 1L

    data class Catatan(
        val id: Long,
        var judul: String,
        var isi: String,
        var done: Boolean
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        // Komponen utama
        drawerLayout = findViewById(R.id.drawerLayout)
        pageBeranda = findViewById(R.id.pageBeranda)
        pageCatatan = findViewById(R.id.pageCatatan)
        pageProfil = findViewById(R.id.pageProfil)
        txtJudul = findViewById(R.id.txtJudul)

        // Sidebar
        val btnMenu = findViewById<TextView>(R.id.btnMenu)
        val menuBeranda = findViewById<Button>(R.id.menuBeranda)
        val menuCatatan = findViewById<Button>(R.id.menuCatatan)
        val menuProfil = findViewById<Button>(R.id.menuProfil)
        val menuLogout = findViewById<Button>(R.id.menuLogout)

        // Beranda
        val txtWelcome = findViewById<TextView>(R.id.txtWelcome)
        val txtTotal = findViewById<TextView>(R.id.txtTotal)
        val txtDone = findViewById<TextView>(R.id.txtDone)
        val txtBelum = findViewById<TextView>(R.id.txtBelum)
        val txtCatatanTerakhir =
            findViewById<TextView>(R.id.txtCatatanTerakhir)

        // Form catatan
        val etJudul = findViewById<EditText>(R.id.etJudulCatatan)
        val etIsi = findViewById<EditText>(R.id.etIsiCatatan)
        val cbStatus = findViewById<CheckBox>(R.id.cbStatus)
        val btnSimpan = findViewById<Button>(R.id.btnSimpan)
        val btnBatalEdit = findViewById<Button>(R.id.btnBatalEdit)
        val txtFormJudul = findViewById<TextView>(R.id.txtFormJudul)

        val containerCatatan =
            findViewById<LinearLayout>(R.id.containerCatatan)

        // Profil
        val txtProfilNama =
            findViewById<TextView>(R.id.txtProfilNama)
        val etProfilNama =
            findViewById<EditText>(R.id.etProfilNama)
        val etProfilEmail =
            findViewById<EditText>(R.id.etProfilEmail)
        val etProfilBio =
            findViewById<EditText>(R.id.etProfilBio)
        val btnSimpanProfil =
            findViewById<Button>(R.id.btnSimpanProfil)

        // Username dari Login
        val username =
            intent.getStringExtra("USERNAME") ?: "User"

        // Penyimpanan lokal
        val prefs = getSharedPreferences(
            "MyDailyPrefs", MODE_PRIVATE
        )

        // =========================
        // PROFIL PENGGUNA
        // =========================

        val namaProfil = prefs.getString(
            "PROFIL_NAMA", username
        ) ?: username

        val emailProfil = prefs.getString(
            "PROFIL_EMAIL", ""
        ) ?: ""

        val bioProfil = prefs.getString(
            "PROFIL_BIO", ""
        ) ?: ""

        etProfilNama.setText(namaProfil)
        etProfilEmail.setText(emailProfil)
        etProfilBio.setText(bioProfil)

        txtProfilNama.text = namaProfil
        txtWelcome.text = "Halo, $namaProfil!"

        btnSimpanProfil.setOnClickListener {

            val nama = etProfilNama.text.toString().trim()
            val email = etProfilEmail.text.toString().trim()
            val bio = etProfilBio.text.toString().trim()

            if (nama.isEmpty()) {
                etProfilNama.error = "Nama wajib diisi"
                return@setOnClickListener
            }

            if (email.isNotEmpty() &&
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            ) {
                etProfilEmail.error = "Email tidak valid"
                return@setOnClickListener
            }

            prefs.edit()
                .putString("PROFIL_NAMA", nama)
                .putString("PROFIL_EMAIL", email)
                .putString("PROFIL_BIO", bio)
                .apply()

            txtProfilNama.text = nama
            txtWelcome.text = "Halo, $nama!"

            Toast.makeText(
                this,
                "Profil berhasil disimpan!",
                Toast.LENGTH_SHORT
            ).show()
        }

        // =========================
        // MENGAMBIL DAFTAR CATATAN
        // =========================

        val jsonTersimpan = prefs.getString(
            "DAFTAR_CATATAN", "[]"
        ) ?: "[]"

        try {
            val jsonArray = JSONArray(jsonTersimpan)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                val catatan = Catatan(
                    id = obj.optLong("id", i.toLong() + 1),
                    judul = obj.optString("judul", ""),
                    isi = obj.optString("isi", ""),
                    done = obj.optBoolean("done", false)
                )

                daftarCatatan.add(catatan)
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Data catatan tidak dapat dibaca",
                Toast.LENGTH_SHORT
            ).show()
        }

        // ID unik untuk catatan berikutnya
        idBerikutnya = (
                daftarCatatan.maxOfOrNull { it.id } ?: 0L
                ) + 1L

        // Menyimpan semua catatan
        fun simpanDaftar() {
            val jsonArray = JSONArray()

            for (catatan in daftarCatatan) {
                val obj = JSONObject()
                obj.put("id", catatan.id)
                obj.put("judul", catatan.judul)
                obj.put("isi", catatan.isi)
                obj.put("done", catatan.done)
                jsonArray.put(obj)
            }

            prefs.edit()
                .putString(
                    "DAFTAR_CATATAN",
                    jsonArray.toString()
                )
                .apply()
        }

        // Memperbarui ringkasan Beranda
        fun updateBeranda() {

            val total = daftarCatatan.size
            val done = daftarCatatan.count { it.done }
            val belum = total - done

            txtTotal.text = "$total\nTotal"
            txtDone.text = "$done\nDone"
            txtBelum.text = "$belum\nBelum"

            if (daftarCatatan.isEmpty()) {
                txtCatatanTerakhir.text =
                    "Belum ada catatan."
            } else {
                val terakhir = daftarCatatan.last()
                val status = if (terakhir.done) {
                    "Done"
                } else {
                    "Belum"
                }

                txtCatatanTerakhir.text =
                    "${terakhir.judul}\n\n" +
                            "${terakhir.isi}\n\n" +
                            "Status: $status"
            }
        }

        // Mengosongkan form setelah selesai
        fun resetForm() {
            idSedangDiedit = null
            etJudul.text.clear()
            etIsi.text.clear()
            cbStatus.isChecked = false

            txtFormJudul.text = "Tambah Catatan Baru"
            btnSimpan.text = "SIMPAN CATATAN"
            btnBatalEdit.visibility = View.GONE
        }

        // =========================
        // MENAMPILKAN DAFTAR CATATAN
        // =========================

        fun tampilkanDaftar() {
            containerCatatan.removeAllViews()

            if (daftarCatatan.isEmpty()) {
                val kosong = TextView(this)
                kosong.text = "Belum ada catatan. Tambahkan catatan pertamamu."
                kosong.textSize = 14f
                kosong.setTextColor(Color.parseColor("#938BA7"))
                kosong.setPadding(12, 20, 12, 20)
                containerCatatan.addView(kosong)
                return
            }

            // Dibuat salinan agar aman saat data diubah
            for (catatan in daftarCatatan.toList().asReversed()) {

                // Card catatan
                val card = LinearLayout(this)
                card.orientation = LinearLayout.VERTICAL
                card.setPadding(20, 20, 20, 20)
                card.setBackgroundColor(
                    Color.parseColor("#FFFFFF")
                )

                val cardParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                cardParams.bottomMargin = 16
                card.layoutParams = cardParams

                // Judul
                val judulText = TextView(this)
                judulText.text = catatan.judul
                judulText.textSize = 18f
                judulText.setTypeface(
                    null, Typeface.BOLD
                )
                judulText.setTextColor(
                    Color.parseColor("#55516B")
                )
                card.addView(judulText)

                // Isi
                val isiText = TextView(this)
                isiText.text = catatan.isi
                isiText.textSize = 14f
                isiText.setTextColor(
                    Color.parseColor("#77718A")
                )
                isiText.setPadding(0, 12, 0, 12)
                card.addView(isiText)

                // Status
                val statusText = TextView(this)
                statusText.text = if (catatan.done) {
                    "Status: Done"
                } else {
                    "Status: Belum"
                }
                statusText.textSize = 14f
                statusText.setTypeface(
                    null, Typeface.BOLD
                )

                statusText.setTextColor(
                    if (catatan.done) {
                        Color.parseColor("#456A51")
                    } else {
                        Color.parseColor("#BD6B91")
                    }
                )
                card.addView(statusText)

                // Baris tombol
                val barisTombol = LinearLayout(this)
                barisTombol.orientation =
                    LinearLayout.HORIZONTAL

                val btnStatus = Button(this)
                btnStatus.text = if (catatan.done) {
                    "BELUM"
                } else {
                    "DONE"
                }
                btnStatus.textSize = 11f
                btnStatus.setTextColor(
                    Color.parseColor("#42365B")
                )
                btnStatus.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(
                        Color.parseColor("#E0EEDC")
                    )

                val btnEdit = Button(this)
                btnEdit.text = "EDIT"
                btnEdit.textSize = 11f
                btnEdit.setTextColor(
                    Color.parseColor("#42365B")
                )
                btnEdit.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(
                        Color.parseColor("#D9CCF1")
                    )

                val btnHapus = Button(this)
                btnHapus.text = "HAPUS"
                btnHapus.textSize = 11f
                btnHapus.setTextColor(
                    Color.parseColor("#673A4C")
                )
                btnHapus.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(
                        Color.parseColor("#F4C7D9")
                    )

                val tombolParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                tombolParams.setMargins(2, 12, 2, 0)

                barisTombol.addView(
                    btnStatus,
                    LinearLayout.LayoutParams(tombolParams)
                )
                barisTombol.addView(
                    btnEdit,
                    LinearLayout.LayoutParams(tombolParams)
                )
                barisTombol.addView(
                    btnHapus,
                    LinearLayout.LayoutParams(tombolParams)
                )

                card.addView(barisTombol)
                containerCatatan.addView(card)

                // Mengubah status Done / Belum
                btnStatus.setOnClickListener {
                    catatan.done = !catatan.done
                    simpanDaftar()
                    updateBeranda()
                    tampilkanDaftar()
                }

                // Mengedit catatan
                btnEdit.setOnClickListener {
                    idSedangDiedit = catatan.id

                    etJudul.setText(catatan.judul)
                    etIsi.setText(catatan.isi)
                    cbStatus.isChecked = catatan.done

                    txtFormJudul.text = "Edit Catatan"
                    btnSimpan.text = "SIMPAN PERUBAHAN"
                    btnBatalEdit.visibility = View.VISIBLE

                    Toast.makeText(
                        this,
                        "Silakan ubah catatan pada form di atas",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Menghapus dengan konfirmasi
                btnHapus.setOnClickListener {

                    AlertDialog.Builder(this)
                        .setTitle("Hapus Catatan")
                        .setMessage(
                            "Yakin ingin menghapus catatan ini?"
                        )
                        .setPositiveButton("Hapus") { _, _ ->

                            daftarCatatan.removeAll {
                                it.id == catatan.id
                            }

                            if (idSedangDiedit == catatan.id) {
                                resetForm()
                            }

                            simpanDaftar()
                            updateBeranda()
                            tampilkanDaftar()

                            Toast.makeText(
                                this,
                                "Catatan berhasil dihapus",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .setNegativeButton("Batal", null)
                        .show()
                }
            }
        }

        // =========================
        // TOMBOL SIMPAN CATATAN
        // =========================

        btnSimpan.setOnClickListener {

            val judul = etJudul.text.toString().trim()
            val isi = etIsi.text.toString().trim()
            val done = cbStatus.isChecked

            if (judul.isEmpty() || isi.isEmpty()) {
                Toast.makeText(
                    this,
                    "Judul dan isi wajib diisi!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val idEdit = idSedangDiedit

            if (idEdit == null) {

                // Menambahkan catatan baru
                daftarCatatan.add(
                    Catatan(
                        id = idBerikutnya++,
                        judul = judul,
                        isi = isi,
                        done = done
                    )
                )

            } else {

                // Memperbarui catatan lama
                val catatan = daftarCatatan.find {
                    it.id == idEdit
                }

                if (catatan != null) {
                    catatan.judul = judul
                    catatan.isi = isi
                    catatan.done = done
                }
            }

            simpanDaftar()
            updateBeranda()
            tampilkanDaftar()
            resetForm()

            Toast.makeText(
                this,
                "Catatan berhasil disimpan!",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Membatalkan edit catatan
        btnBatalEdit.setOnClickListener {
            resetForm()
        }

        // =========================
        // NAVIGASI SIDEBAR
        // =========================

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(Gravity.START)
        }

        menuBeranda.setOnClickListener {
            updateBeranda()
            tampilkanHalaman("Beranda")
            drawerLayout.closeDrawers()
        }

        menuCatatan.setOnClickListener {
            tampilkanDaftar()
            tampilkanHalaman("Catatan")
            drawerLayout.closeDrawers()
        }

        menuProfil.setOnClickListener {
            tampilkanHalaman("Profil")
            drawerLayout.closeDrawers()
        }

        menuLogout.setOnClickListener {
            drawerLayout.closeDrawers()

            val intent = Intent(
                this,
                LoginActivity::class.java
            )
            startActivity(intent)
            finish()
        }

        // Tampilan awal
        updateBeranda()
        tampilkanDaftar()
        tampilkanHalaman("Beranda")
    }

    // Fungsi perpindahan halaman
    private fun tampilkanHalaman(halaman: String) {

        pageBeranda.visibility = View.GONE
        pageCatatan.visibility = View.GONE
        pageProfil.visibility = View.GONE

        when (halaman) {

            "Beranda" -> {
                pageBeranda.visibility = View.VISIBLE
                txtJudul.text = "Beranda"
            }

            "Catatan" -> {
                pageCatatan.visibility = View.VISIBLE
                txtJudul.text = "Catatan Saya"
            }

            "Profil" -> {
                pageProfil.visibility = View.VISIBLE
                txtJudul.text = "Profil"
            }
        }
    }
}
