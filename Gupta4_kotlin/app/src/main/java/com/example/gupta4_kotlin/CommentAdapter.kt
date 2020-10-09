package com.example.gupta4_kotlin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class CommentAdapter(val context: Context, private val commentList: MutableList<Comment>, private val boardKey: String, private val postId: String): RecyclerView.Adapter<CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(
            LayoutInflater.from(context)
            .inflate(R.layout.gupsik_comment, parent, false))
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        comment.let {
            holder.commentText.text = comment.message
            holder.commentWriteTime.text = Utils.getDiffTimeText(comment.writeTime as Long)
            holder.commentNickname.text = comment.nickname

        }

        // 본인이 쓴 댓글이면 삭제 버튼이 보이도록 해야함.

        val commentId = comment.commentId

        if(comment.writerId == getMyId()) {
            holder.deleteTextView.visibility = View.VISIBLE
        }

        holder.deleteTextView.setOnClickListener {

            val intent = Intent(context.applicationContext, PopupButtonActivity::class.java)
            intent.putExtra("boardKey", boardKey)
            intent.putExtra("postId", postId)
            intent.putExtra("commentId", commentId)
            intent.putExtra("popUpMode", "deleteComment")
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            context.startActivity(intent)

        }

    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    @SuppressLint("HardwareIds")
    fun getMyId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

}