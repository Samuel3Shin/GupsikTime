package com.gooselab.gupta4_kotlin

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.gupsik_my_post.view.*

class MyPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val contentsText: TextView = itemView.contentsText
    val timeTextView: TextView = itemView.timeTextView
    val commentCountText: TextView = itemView.commentCountText
    val hitsCountText: TextView = itemView.hitsCountText
    val nicknameText: TextView = itemView.nicknameTextView
    val likesCountText: TextView = itemView.likesCountText
    val titleTextView: TextView = itemView.titleTextView
    val boardNameTextView: TextView = itemView.boardTextView

}