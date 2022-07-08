package com.example.talk.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnLayoutChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.talk.R
import com.example.talk.adapters.MessagesAdapter
import com.example.talk.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MessagesAdapter
    private lateinit var messages: ArrayList<com.example.talk.models.Message>
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    var senderUid: String? = null
    var receiverUid: String? = null

    var senderRoom: String? = null
    var receiverRoom: String? = null

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        val root = setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        messages = ArrayList()

        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")


        //Toast.makeText(this, token, Toast.LENGTH_SHORT).show();
        binding.name.text = name
        Glide.with(this@ChatActivity).load(profile)
            .placeholder(R.drawable.ic_other_user)
            .into(binding.profile)

        binding.previousActivity.setOnClickListener {
            finish()
        }

        receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().uid

        database.reference.child("presence").child(receiverUid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        if (!status!!.isEmpty()) {
                            if (status == "Offline") {
                                binding.status.visibility = View.GONE
                            } else {
                                binding.status.text = status
                                binding.status.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid

        adapter = MessagesAdapter(this, messages)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        if (Build.VERSION.SDK_INT >= 11) {
            binding.recyclerView.addOnLayoutChangeListener(OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                if (bottom < oldBottom) {
                    binding.recyclerView.postDelayed(Runnable {
                        binding.recyclerView.smoothScrollToPosition(
                            binding.recyclerView.adapter?.itemCount!! - 1
                        )
                    }, 100)
                }
            })
        }


        database.reference.child("chats")
            .child(senderRoom!!)
            .child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for (snapshot1 in snapshot.children) {
                        val message =
                            snapshot1.getValue(com.example.talk.models.Message::class.java)
                        message!!.messageId
                        messages.add(message)
                    }
                    binding.recyclerView.scrollToPosition(messages.size-1)
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        binding.sendBtn.setOnClickListener {

            val messageText = binding.messageBox.text.toString()

            if (messageText.isEmpty()) {
                return@setOnClickListener
            }

            val date = Date()
            val message = com.example.talk.models.Message(
                "", messageText, senderUid,
                "", date.time
            )
            binding.messageBox.text = null

            val randomKey = database.reference.push().key

            val lastMsgObj = HashMap<String, Any>()
            lastMsgObj["lastMsg"] = message.message!!
            lastMsgObj["lastMsgTime"] = date.time


            database.reference.child("chats").child(senderRoom!!).updateChildren(lastMsgObj)
            database.reference.child("chats").child(receiverRoom!!).updateChildren(lastMsgObj)

            database.reference.child("chats")
                .child(senderRoom!!)
                .child("messages")
                .child(randomKey!!)
                .setValue(message)
                .addOnSuccessListener {
                    database.reference.child("chats")
                        .child(receiverRoom!!)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message)
                }

        }

        val handler = Handler()
        binding.messageBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                database.reference.child("presence").child(senderUid!!).setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }

            var userStoppedTyping =
                Runnable {
                    database.reference.child("presence").child(senderUid!!).setValue("Online")
                }
        })


        return root
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("presence").child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("presence").child(currentId!!).setValue("Offline")
    }
}