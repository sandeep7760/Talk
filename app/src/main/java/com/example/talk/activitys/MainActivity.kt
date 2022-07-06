package com.example.talk.activitys

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.talk.R
import com.example.talk.adapters.UsersAdapter
import com.example.talk.databinding.ActivityMainBinding
import com.example.talk.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var users: ArrayList<User>
    private lateinit var usersAdapter: UsersAdapter
    var dialog: ProgressDialog? = null

    var user: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val root = setContentView(binding.root)
        title = "Chats"
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        users = java.util.ArrayList()

        val sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE)
        val name = sharedPreferences.getString("Name", "")
        if (name!!.isEmpty()) {
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Name $name", Toast.LENGTH_SHORT).show()
        }

//        database.reference.child("users").child(FirebaseAuth.getInstance().uid!!)
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    user = snapshot.getValue(User::class.java)
//                }
//
//                override fun onCancelled(error: DatabaseError) {}
//            })


        usersAdapter = UsersAdapter(this, users)
//        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.HORIZONTAL

        binding.recyclerView.adapter = usersAdapter

        binding.recyclerView.showShimmerAdapter()

        database.reference.child("users").addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()

                var count = 0;
                for (snapshot1 in snapshot.children) {
                    val user = snapshot1.getValue(User::class.java)
                    if (!user!!.uid.equals(FirebaseAuth.getInstance().uid)) {
                        users.add(user)
                        count++
                    }

                }
                if (count == 0) {
                    binding.emptyUser.visibility = View.VISIBLE
                }
                binding.recyclerView.hideShimmerAdapter()
                usersAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

//       binding.bottomNavigationView.setOnNavigationItemSelectedListener {
//            when(it.itemId){
//                R.id.profile->startActivity(Intent(this@MainActivity,ProfileActivity::class.java))
////                R.id.chats->setCurrentFragment(secondFragment)
//
//            }
//            true
//        }

        return root

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE)

        menuInflater.inflate(R.menu.menu1, menu)
        val signOut = menu?.findItem(R.id.signout)
        val profile = menu?.findItem(R.id.profile)
        signOut?.setOnMenuItemClickListener {
            auth.signOut()
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Signing Out", Toast.LENGTH_SHORT).show()
            true
        }

        profile?.setOnMenuItemClickListener {
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(intent)
            true
        }

        return true
    }

}