package com.example.talk.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.talk.InternetConnectivity
import com.example.talk.R
import com.example.talk.databinding.ActivityCurrentLoginProfileBinding
import com.example.talk.models.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class CurrentLoginProfile : AppCompatActivity() {

    var binding: ActivityCurrentLoginProfileBinding? = null
    var auth: FirebaseAuth? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var selectedImage: Uri? = null
    var dialog: ProgressDialog? = null
    lateinit var checkInternet: InternetConnectivity

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkInternet = InternetConnectivity()
        binding = ActivityCurrentLoginProfileBinding.inflate(layoutInflater)
        val root = setContentView(binding!!.root)
        title = "TalkApp"
        dialog = ProgressDialog(this)
        dialog!!.setMessage("Updating profile...")
        dialog!!.setCancelable(false)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

//        val sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE)

//        supportActionBar!!.hide()


        existingUser()

        binding!!.imageView.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 45)
        }

        binding!!.continueBtn.setOnClickListener(View.OnClickListener {
            val name = binding!!.nameBox.text.toString()
            val regexName = Regex("^[a-zA-Z\\s]{3,}$")

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
                                            this@CurrentLoginProfile,
                                            MainActivity::class.java
                                        )
                                        Toast.makeText(
                                            this@CurrentLoginProfile,
                                            "Update Profile Successfully",
                                            Toast.LENGTH_LONG
                                        ).show()
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
                            val intent = Intent(this@CurrentLoginProfile, MainActivity::class.java)
                            Toast.makeText(
                                this@CurrentLoginProfile,
                                "Update Profile Successfully",
                                Toast.LENGTH_LONG
                            ).show()
                            startActivity(intent)
                            finish()
                        }
                }
            } else {
                dialog!!.dismiss()
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show()
            }
        })

//        binding!!.bottomNavigationView.setOnNavigationItemSelectedListener {
//
//            when (it.itemId) {
//                com.example.talk.R.id.user_profile -> return@setOnNavigationItemSelectedListener true
//                com.example.talk.R.id.chats -> {
//                    startActivity(Intent(this@CurrentLoginProfile, LoginActivity::class.java))
//                    overridePendingTransition(0, 0)
//                    return@setOnNavigationItemSelectedListener true
//                }
//            }
//            false
//
//        }

        binding!!.bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {

                com.example.talk.R.id.chats -> {
                    startActivity(Intent(this@CurrentLoginProfile, LoginActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@OnNavigationItemSelectedListener false
                }
                com.example.talk.R.id.user_profile -> return@OnNavigationItemSelectedListener true
            }
            false
        })

        return root
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence").child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence").child(currentId!!).setValue("Offline")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun existingUser() {

        dialog = ProgressDialog(this)
        dialog!!.setMessage("Please Wait...")
        dialog!!.setCancelable(false)
        dialog!!.show()


        val sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE)


        try {
            if (checkInternet.isConnected(this)) {
                database!!.reference.child("users")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (snapshot1 in snapshot.children) {
                                val user = snapshot1.getValue(User::class.java)
                                if (user!!.uid.equals(FirebaseAuth.getInstance().uid)) {
                                    binding!!.nameBox.setText(user.name)
                                    if (user.profileImage != "No Image") {
                                        Glide.with(this@CurrentLoginProfile).load(user.profileImage)
                                            .placeholder(R.drawable.ic_other_user)
                                            .into(binding!!.imageView)
                                    }

                                }
                            }
                            dialog!!.dismiss()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(
                                this@CurrentLoginProfile,
                                "No User Found",
                                Toast.LENGTH_LONG
                            )
                                .show()
                            dialog!!.dismiss()
                        }

                    })
            } else {
                Toast.makeText(
                    this@CurrentLoginProfile,
                    "No Internet Connection",
                    Toast.LENGTH_LONG
                )
                    .show()
                finish()
            }

        } catch (e: Exception) {
            Toast.makeText(this@CurrentLoginProfile, "Failed to Connect Server", Toast.LENGTH_LONG)
                .show()

            finish()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (data.data != null) {
                val uri = data.data // filepath
                val storage = FirebaseStorage.getInstance()
                val time = Date().time
//                val reference = storage.reference.child("Profiles").child(time.toString() + "")
//                reference.putFile(uri!!).addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        reference.downloadUrl.addOnSuccessListener { uri ->
//                            val filePath = uri.toString()
//                            val obj = HashMap<String, Any>()
//                            obj["image"] = filePath
//                            database!!.reference.child("users")
//                                .child(FirebaseAuth.getInstance().uid!!)
//                                .updateChildren(obj).addOnSuccessListener { }
//                        }
//                    }
//                }
                binding!!.imageView.setImageURI(data.data)
                selectedImage = data.data
            } else {
                Toast.makeText(this@CurrentLoginProfile, "DATA Not Found", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE)

        menuInflater.inflate(com.example.talk.R.menu.signout, menu)
        val signOut = menu?.findItem(com.example.talk.R.id.signOutMenu)
        signOut?.setOnMenuItemClickListener {
            auth!!.signOut()
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Signing Out", Toast.LENGTH_SHORT).show()
            true
        }


        return true
    }


}

