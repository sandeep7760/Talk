package com.example.talk

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.talk.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ProfileActivity : AppCompatActivity() {

    var binding: ActivityProfileBinding? = null
    var auth: FirebaseAuth? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var selectedImage: Uri? = null
    var dialog: ProgressDialog? = null
    lateinit var checkInternet : InternetConnectivity

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkInternet = InternetConnectivity()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val root = setContentView(binding!!.root)

        dialog = ProgressDialog(this)
        dialog!!.setMessage("Updating profile...")
        dialog!!.setCancelable(false)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        val sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE)

        supportActionBar!!.hide()

        binding!!.imageView.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 45)
        }

        binding!!.continueBtn.setOnClickListener(View.OnClickListener {
            val name = binding!!.nameBox.text.toString()
            val regexName = "^[a-zA-Z\\s]{3,}$"

            if (name.isEmpty()) {
                binding!!.nameBox.error = "Please Enter your Name"
                return@OnClickListener
            }
            dialog!!.show()

            if (checkInternet.isConnected(this)) {
                if (selectedImage != null) {
                    val reference = storage!!.reference.child("Profiles").child(
                        auth!!.uid!!
                    )

                    reference.putFile(selectedImage!!).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            reference.downloadUrl.addOnSuccessListener { uri ->
                                val imageUrl = uri.toString()
                                val uid = auth!!.uid
                                val phone = auth!!.currentUser!!.phoneNumber
                                val name = binding!!.nameBox.text.toString()
                                val user = User(uid, name, phone, imageUrl)
                                database!!.reference
                                    .child("users")
                                    .child(uid!!)
                                    .setValue(user)
                                    .addOnSuccessListener {
                                        dialog!!.dismiss()
                                        val intent = Intent(
                                            this@ProfileActivity,
                                            MainActivity::class.java
                                        )
                                        val editor: SharedPreferences.Editor =
                                            sharedPreferences.edit()
                                        editor.putString(
                                            "Name",
                                            binding!!.nameBox.getText().toString()
                                        )
                                        editor.apply()
                                        startActivity(intent)
                                        finish()
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Failed Try Again...", Toast.LENGTH_LONG).show()
                            dialog!!.dismiss()
                        }
                    }

                } else {
                    val uid = auth!!.uid
                    val phone = auth!!.currentUser!!.phoneNumber
                    val user = User(uid, name, phone, "No Image")
                    database!!.reference
                        .child("users")
                        .child(uid!!)
                        .setValue(user)
                        .addOnSuccessListener {
                            dialog!!.dismiss()
                            val intent = Intent(this@ProfileActivity, MainActivity::class.java)
                            val editor: SharedPreferences.Editor = sharedPreferences.edit()
                            editor.putString("Name", binding!!.nameBox.getText().toString())
                            editor.apply()
                            startActivity(intent)
                            finish()
                        }
                }
            }
            else{
                dialog!!.dismiss()
                Toast.makeText(this,"No Internet Connection",Toast.LENGTH_LONG).show()
            }
        })

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (data.data != null) {
                val uri = data.data // filepath
                val storage = FirebaseStorage.getInstance()
                val time = Date().time
                val reference = storage.reference.child("Profiles").child(time.toString() + "")
                reference.putFile(uri!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnSuccessListener { uri ->
                            val filePath = uri.toString()
                            val obj = HashMap<String, Any>()
                            obj["image"] = filePath
                            database!!.reference.child("users")
                                .child(FirebaseAuth.getInstance().uid!!)
                                .updateChildren(obj).addOnSuccessListener { }
                        }
                    }
                }
                binding!!.imageView.setImageURI(data.data)
                selectedImage = data.data
            }
        }
    }
}