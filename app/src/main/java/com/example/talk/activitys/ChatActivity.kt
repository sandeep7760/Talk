package com.example.talk.activitys

import android.os.Bundle
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.talk.R
import com.example.talk.adapters.MessagesAdapter
import com.example.talk.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MessagesAdapter
    private lateinit var messages: ArrayList<Message>
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