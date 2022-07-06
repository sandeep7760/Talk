package com.example.talk.activitys

import android.os.Bundle
import android.os.Message
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

        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid

        adapter = MessagesAdapter(this, messages, senderRoom!!, receiverRoom!!)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        database.reference.child("chats")
            .child(senderRoom!!)
            .child("messages")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for (snapshot1 in snapshot.children) {
                        messages.clear()
                        val message = snapshot1.getValue(com.example.talk.models.Message::class.java)
                        message!!.messageId
                        messages.add(message)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

//        database.reference.child("chats")
//            .child(senderRoom!!)
//            .child("messages")
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    messages.clear()
//                    for (snapshot1 in snapshot.children) {
//                        val message = snapshot1.getValue(Message::class.java)
//                        message.setMessageId(snapshot1.key)
//                        messages.add(message!!)
//                    }
//                    adapter.notifyDataSetChanged()
//                }
//
//                override fun onCancelled(error: DatabaseError) {}
//            })

        binding.sendBtn.setOnClickListener {

            val messageText = binding.messageBox.text.toString()

            if (messageText.isEmpty()) {
                return@setOnClickListener
            }

            val date = Date()
            val message = com.example.talk.models.Message(
                "", messageText, senderUid,
                "",date.time
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


        return root
    }
}