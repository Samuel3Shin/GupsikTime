package com.example.gupta4_kotlin

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.gupsik_post.view.*

class CommunityAdapter(val context: Context, val posts: MutableList<Post>, val boardKey: String): RecyclerView.Adapter<MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.gupsik_post, parent, false))
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val post = posts[position]
        holder.contentsText.text = post.message
        holder.timeTextView.text = Utils.getDiffTimeText(post.writeTime as Long)
        holder.commentCountText.text = post.commentCount.toString()
        holder.hitsCountText.text = post.hitsCount.toString()
        holder.nicknameText.text = post.nickName
        holder.likesCountText.text = post.likesCount.toString()


        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("boardKey", boardKey)
            intent.putExtra("postId", post.postId)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            context.startActivity(intent)

            // hits 개수 늘려주기 추가
            val id = post.postId
            val postRef = FirebaseDatabase.getInstance().getReference("$boardKey/Posts/$id")

            postRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    var hitsNum = snapshot.child("hitsCount").value as Long
                    postRef.child("hitsCount").setValue(hitsNum + 1)
                }
            })
        }

    }


}

class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val contentsText: TextView = itemView.contentsText
    val timeTextView: TextView = itemView.timeTextView
    val commentCountText: TextView = itemView.commentCountText
    val hitsCountText: TextView = itemView.hitsCountText
    val nicknameText: TextView = itemView.nicknameTextView
    val likesCountText: TextView = itemView.likesCountText
}