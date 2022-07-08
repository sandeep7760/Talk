package com.example.talk.activities

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
import com.example.talk.InternetConnectivity
import com.example.talk.databinding.ActivityProfileBinding
import com.example.talk.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ProfileActivity : AppCompatActivity() {

    var binding: ActivityProfileBinding? = null
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
                                            this@ProfileActivity,
                                            MainActivity::class.java
                                        )
                                        val editor: SharedPreferences.Editor =
                                            sharedPreferences.edit()
                                        editor.putString(
                                            "Name",
                                            binding!!.nameBox.text.toString()
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
                            editor.putString("Name", binding!!.nameBox.text.toString())
                            editor.apply()
                            startActivity(intent)
                            finish()
                        }
                }
            } else {
                dialog!!.dismiss()
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show()
            }
        })

        return root
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
                                    Toast.makeText(
                                        this@ProfileActivity,
                                        "User Found",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                                    editor.putString("Name", "User Exist")
                                    editor.apply()
                                    startActivity(
                                        Intent(
                                            this@ProfileActivity,
                                            MainActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            }
                            Toast.makeText(this@ProfileActivity, "No User Found", Toast.LENGTH_LONG)
                                .show()
                            dialog!!.dismiss()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@ProfileActivity, "No User Found", Toast.LENGTH_LONG)
                                .show()
                            dialog!!.dismiss()
                        }

                    })
            } else {
                Toast.makeText(this@ProfileActivity, "No Internet Connection", Toast.LENGTH_LONG)
                    .show()
                finish()
            }

        } catch (e: Exception) {
            Toast.makeText(this@ProfileActivity, "Failed to Connect Server", Toast.LENGTH_LONG)
                .show()

            finish()
        }


//        database.reference.child("users").addValueEventListener(object : ValueEventListener {
//            @SuppressLint("NotifyDataSetChanged")
//            override fun onDataChange(snapshot: DataSnapshot) {
//                users.clear()
//
//                var count = 0;
//                for (snapshot1 in snapshot.children) {
//                    val user = snapshot1.getValue(User::class.java)
//                    if (!user!!.uid.equals(FirebaseAuth.getInstance().uid)) {
//                        users.add(user)
//                        count++
//                    }
//
//                }
//                if (count == 0) {
//                    binding.emptyUser.visibility = View.VISIBLE
//                }
//                binding.recyclerView.hideShimmerAdapter()
//                usersAdapter.notifyDataSetChanged()
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })
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