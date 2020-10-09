package com.gooselab.gupta4_kotlin

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class MyPostsAdapter(val context: Context, private val posts: MutableList<Post>, private val boardKeys: MutableList<String>): RecyclerView.Adapter<MyPostViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPostViewHolder {
        return MyPostViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.gupsik_my_post, parent, false))
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: MyPostViewHolder, position: Int) {
        val post = posts[position]
        holder.contentsText.text = post.message
        holder.timeTextView.text = Utils.getDiffTimeText(post.writeTime as Long)
        holder.commentCountText.text = post.commentCount.toString()
        holder.hitsCountText.text = post.hitsCount.toString()
        holder.nicknameText.text = post.nickname
        holder.likesCountText.text = post.likesCount.toString()
        holder.boardNameTextView.text = post.board
        holder.titleTextView.text = post.title

        holder.itemView.setOnClickListener {
            val intent = Intent(context.applicationContext, DetailActivity::class.java)
            val boardKey = boardKeys[position]

            intent.putExtra("boardKey", boardKey)
            intent.putExtra("postId", post.postId)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            context.startActivity(intent)

        }

    }
}