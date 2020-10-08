package com.example.gupta4_kotlin

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.gupsik_comment.view.*

class CommentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val commentText: TextView = itemView.findViewById(R.id.comment_text)
    val commentWriteTime: TextView = itemView.dateTextView
    val commentNickname: TextView = itemView.nickname
    val deleteTextView: TextView = itemView.deleteTextView
}