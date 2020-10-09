package com.gooselab.gupta4_kotlin

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommunityAdapter(val context: Context, private val posts: MutableList<Post>, private val boardKey: String): RecyclerView.Adapter<CommunityViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        return CommunityViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.gupsik_post, parent, false))
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        val post = posts[position]
        holder.contentsText.text = post.message
        holder.timeTextView.text = Utils.getDiffTimeText(post.writeTime as Long)
        holder.commentCountText.text = post.commentCount.toString()
        holder.hitsCountText.text = post.hitsCount.toString()
        holder.titleTextView.text = post.title
        holder.nicknameText.text = post.nickname
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

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val hitsNum = snapshot.child("hitsCount").value as Long
                    postRef.child("hitsCount").setValue(hitsNum + 1)
                }
            })
        }
    }
}