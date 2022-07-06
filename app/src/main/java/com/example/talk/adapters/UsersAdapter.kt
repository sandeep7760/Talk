package com.example.talk.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.talk.R
import com.example.talk.models.User
import com.example.talk.activitys.ChatActivity
import com.example.talk.databinding.RowConversationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class UsersAdapter(var context: Context, var users: ArrayList<User>) :

    RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false)
        return UsersViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val user = users[position]
        val senderId = FirebaseAuth.getInstance().uid
        val senderRoom = senderId + user.uid
        FirebaseDatabase.getInstance().reference
            .child("chats")
            .child(senderRoom)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val lastMsg = snapshot.child("lastMsg").getValue(
                            String::class.java
                        )
                        val time = snapshot.child("lastMsgTime").getValue(
                            Long::class.java
                        )!!
                        val dateFormat = SimpleDateFormat("hh:mm a")

//                        holder.binding.msgTime.setText(dateFormat.format(Date(time)))
                        holder.binding.msgTime.text = dateFormat.format(Date(time))
//                        holder.binding.lastMsg.setText(lastMsg)
                        holder.binding.lastMsg.text = lastMsg
                        holder.binding.lastMsg2.visibility = View.VISIBLE
                    } else {
                        holder.binding.lastMsg.text = "Tap to Chat"
                        holder.binding.lastMsg2.visibility = View.GONE
                        holder.binding.msgTime.text = null
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        holder.binding.username.setText(user.name)
        Glide.with(context).load(user.profileImage)
            .placeholder(R.drawable.ic_other_user)
            .into(holder.binding.profile)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("image", user.profileImage)
            intent.putExtra("uid", user.uid)
            intent.putExtra("token", user.token)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: RowConversationBinding

        init {
            binding = RowConversationBinding.bind(itemView)
        }
    }
}
