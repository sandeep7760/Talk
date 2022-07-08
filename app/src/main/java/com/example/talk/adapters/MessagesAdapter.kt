package com.example.talk.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.talk.R
import com.example.talk.databinding.ItemReceiveBinding
import com.example.talk.databinding.ItemSentBinding
import com.example.talk.models.Message
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(
    var context: Context,
    var messages: java.util.ArrayList<com.example.talk.models.Message>,

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //    context: Context
//    var messages: java.util.ArrayList<Message> = ArrayList()
    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2
//    var senderRoom: String = ""
//    var receiverRoom: String = ""


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == ITEM_SENT) {
            val view: View = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false)
            SentViewHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false)
            ReceiverViewHolder(view)
        }
    }


    override fun getItemViewType(position: Int): Int {

        val message: Message = messages[position]

        return if (FirebaseAuth.getInstance().uid == message.senderId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message: Message = messages[position]
        val dateFormat = SimpleDateFormat("hh:mm a")
//        val dateFormat = SimpleDateFormat("hh:mm a")

        if (holder.javaClass == SentViewHolder::class.java) {
            val viewHolder = holder as SentViewHolder
            viewHolder.binding.message.text = message.message
            viewHolder.binding.msgTime.text = dateFormat.format(Date(message.timestamp!!))

//            dateFormat.format(Date(time))
        } else {
            val viewHolder = holder as ReceiverViewHolder
            viewHolder.binding.message.text = message.message
            viewHolder.binding.msgTime.text = dateFormat.format(Date(message.timestamp!!))
        }

    }

    override fun getItemCount(): Int {
        return messages.size
    }

    public class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemSentBinding

        init {
            binding = ItemSentBinding.bind(itemView)
        }
    }

    public class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemReceiveBinding

        init {
            binding = ItemReceiveBinding.bind(itemView)
        }
    }
}


//class MessagesAdapter(
//    context: Context,
//    messages: java.util.ArrayList<Message>,
//    senderRoom: String,
//    receiverRoom: String
//) :
//    RecyclerView.Adapter<Any?>() {
//    var context: Context
//    var messages: java.util.ArrayList<Message>
//    val ITEM_SENT = 1
//    val ITEM_RECEIVE = 2
//    var senderRoom: String
//    var receiverRoom: String
//    var remoteConfig: FirebaseRemoteConfig
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return if (viewType == ITEM_SENT) {
//            val view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false)
//            SentViewHolder(view)
//        } else {
//            val view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false)
//            ReceiverViewHolder(view)
//        }
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        val message = messages[position]
//        return if (FirebaseAuth.getInstance().uid == message.getSenderId()) {
//            ITEM_SENT
//        } else {
//            ITEM_RECEIVE
//        }
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val message = messages[position]
//        val reactions = intArrayOf(
//            R.drawable.ic_fb_like,
//            R.drawable.ic_fb_love,
//            R.drawable.ic_fb_laugh,
//            R.drawable.ic_fb_wow,
//            R.drawable.ic_fb_sad,
//            R.drawable.ic_fb_angry
//        )
//        val config: ReactionsConfig = ReactionsConfigBuilder(context)
//            .withReactions(reactions)
//            .build()
//        val popup = ReactionPopup(context, config) label@{ pos ->
//            if (pos < 0) return@label false
//            if (holder.javaClass == SentViewHolder::class.java) {
//                val viewHolder = holder as SentViewHolder
//                viewHolder.binding.feeling.setImageResource(reactions[pos])
//                viewHolder.binding.feeling.setVisibility(View.VISIBLE)
//            } else {
//                val viewHolder = holder as ReceiverViewHolder
//                viewHolder.binding.feeling.setImageResource(reactions[pos])
//                viewHolder.binding.feeling.setVisibility(View.VISIBLE)
//            }
//            message.setFeeling(pos)
//            FirebaseDatabase.getInstance().reference
//                .child("chats")
//                .child(senderRoom)
//                .child("messages")
//                .child(message.getMessageId()).setValue(message)
//            FirebaseDatabase.getInstance().reference
//                .child("chats")
//                .child(receiverRoom)
//                .child("messages")
//                .child(message.getMessageId()).setValue(message)
//            true // true is closing popup, false is requesting a new selection
//        }
//        if (holder.javaClass == SentViewHolder::class.java) {
//            val viewHolder = holder as SentViewHolder
//            if (message.getMessage().equals("photo")) {
//                viewHolder.binding.image.setVisibility(View.VISIBLE)
//                viewHolder.binding.message.visibility = View.GONE
//                Glide.with(context)
//                    .load(message.getImageUrl())
//                    .placeholder(R.drawable.placeholder)
//                    .into<Target<Drawable>>(viewHolder.binding.image)
//            }
//            viewHolder.binding.message.setText(message.getMessage())
//            if (message.getFeeling() >= 0) {
//                viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()])
//                viewHolder.binding.feeling.setVisibility(View.VISIBLE)
//            } else {
//                viewHolder.binding.feeling.setVisibility(View.GONE)
//            }
//            viewHolder.binding.message.setOnTouchListener { v, event ->
//                val isFeelingsEnabled: Boolean = remoteConfig.getBoolean("isFeelingsEnabled")
//                if (isFeelingsEnabled) popup.onTouch(v, event) else Toast.makeText(
//                    context,
//                    "This feature is disabled temporarily.",
//                    Toast.LENGTH_SHORT
//                ).show()
//                false
//            }
//            viewHolder.binding.image.setOnTouchListener(OnTouchListener { v, event ->
//                popup.onTouch(v, event)
//                false
//            })
//            viewHolder.itemView.setOnLongClickListener {
//                val view: View =
//                    LayoutInflater.from(context).inflate(R.layout.delete_dialog, null)
//                val binding: DeleteDialogBinding = DeleteDialogBinding.bind(view)
//                val dialog: AlertDialog = AlertDialog.Builder(context)
//                    .setTitle("Delete Message")
//                    .setView(binding.getRoot())
//                    .create()
//                if (remoteConfig.getBoolean("isEveryoneDeletionEnabled")) {
//                    binding.everyone.setVisibility(View.VISIBLE)
//                } else {
//                    binding.everyone.setVisibility(View.GONE)
//                }
//                binding.everyone.setOnClickListener(View.OnClickListener {
//                    message.setMessage("This message is removed.")
//                    message.setFeeling(-1)
//                    FirebaseDatabase.getInstance().reference
//                        .child("chats")
//                        .child(senderRoom)
//                        .child("messages")
//                        .child(message.getMessageId()).setValue(message)
//                    FirebaseDatabase.getInstance().reference
//                        .child("chats")
//                        .child(receiverRoom)
//                        .child("messages")
//                        .child(message.getMessageId()).setValue(message)
//                    dialog.dismiss()
//                })
//                binding.delete.setOnClickListener(View.OnClickListener {
//                    FirebaseDatabase.getInstance().reference
//                        .child("chats")
//                        .child(senderRoom)
//                        .child("messages")
//                        .child(message.getMessageId()).setValue(null)
//                    dialog.dismiss()
//                })
//                binding.cancel.setOnClickListener(View.OnClickListener { dialog.dismiss() })
//                dialog.show()
//                false
//            }
//        } else {
//            val viewHolder = holder as ReceiverViewHolder
//            if (message.getMessage().equals("photo")) {
//                viewHolder.binding.image.setVisibility(View.VISIBLE)
//                viewHolder.binding.message.visibility = View.GONE
//                Glide.with(context)
//                    .load(message.getImageUrl())
//                    .placeholder(R.drawable.placeholder)
//                    .into<Target<Drawable>>(viewHolder.binding.image)
//            }
//            viewHolder.binding.message.setText(message.getMessage())
//            if (message.getFeeling() >= 0) {
//                //message.setFeeling(reactions[message.getFeeling()]);
//                viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()])
//                viewHolder.binding.feeling.setVisibility(View.VISIBLE)
//            } else {
//                viewHolder.binding.feeling.setVisibility(View.GONE)
//            }
//            viewHolder.binding.message.setOnTouchListener { v, event ->
//                popup.onTouch(v, event)
//                false
//            }
//            viewHolder.binding.image.setOnTouchListener(OnTouchListener { v, event ->
//                popup.onTouch(v, event)
//                false
//            })
//            viewHolder.itemView.setOnLongClickListener {
//                val view: View =
//                    LayoutInflater.from(context).inflate(R.layout.delete_dialog, null)
//                val binding: DeleteDialogBinding = DeleteDialogBinding.bind(view)
//                val dialog: AlertDialog = AlertDialog.Builder(context)
//                    .setTitle("Delete Message")
//                    .setView(binding.getRoot())
//                    .create()
//                binding.everyone.setOnClickListener(View.OnClickListener {
//                    message.setMessage("This message is removed.")
//                    message.setFeeling(-1)
//                    FirebaseDatabase.getInstance().reference
//                        .child("chats")
//                        .child(senderRoom)
//                        .child("messages")
//                        .child(message.getMessageId()).setValue(message)
//                    FirebaseDatabase.getInstance().reference
//                        .child("chats")
//                        .child(receiverRoom)
//                        .child("messages")
//                        .child(message.getMessageId()).setValue(message)
//                    dialog.dismiss()
//                })
//                binding.delete.setOnClickListener(View.OnClickListener {
//                    FirebaseDatabase.getInstance().reference
//                        .child("chats")
//                        .child(senderRoom)
//                        .child("messages")
//                        .child(message.getMessageId()).setValue(null)
//                    dialog.dismiss()
//                })
//                binding.cancel.setOnClickListener(View.OnClickListener { dialog.dismiss() })
//                dialog.show()
//                false
//            }
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return messages.size
//    }
//
//    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        var binding: ItemSentBinding
//
//        init {
//            binding = ItemSentBinding.bind(itemView)
//        }
//    }
//
//    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        var binding: ItemReceiveBinding
//
//        init {
//            binding = ItemReceiveBinding.bind(itemView)
//        }
//    }
//
//    init {
//        remoteConfig = FirebaseRemoteConfig.getInstance()
//        this.context = context
//        this.messages = messages
//        this.senderRoom = senderRoom
//        this.receiverRoom = receiverRoom
//    }
//}
//
