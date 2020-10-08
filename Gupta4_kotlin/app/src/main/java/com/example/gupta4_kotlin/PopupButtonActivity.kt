package com.example.gupta4_kotlin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_popup_button.*

class PopupButtonActivity : AppCompatActivity() {

    init {
        instance = this
    }

    companion object {
        private var instance: PopupButtonActivity? = null
        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    private var postId: String? = ""
    private var commentId: String? = ""
    private var boardKey: String? = ""
    private var popUpMode: String? = ""
    private var writeMode: String? = ""
    val preference: SharedPreferences by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popup_button)

        postId = intent.getStringExtra("postId")
        boardKey = intent.getStringExtra("boardKey")
        popUpMode = intent.getStringExtra("popUpMode")
        writeMode = intent.getStringExtra("writeMode").toString()
        commentId = intent.getStringExtra("commentId")

        if(popUpMode == "back") {
            popUpImageView.setBackgroundResource(R.drawable.back_popup)
        }

        var myPostIdsStr: String = preference.getString(Utils.myPostIdsKey, "").toString()


        confirmButton.setOnClickListener {
            when(popUpMode) {
                "delete" -> {
                    val postRef = FirebaseDatabase.getInstance().getReference("$boardKey/Posts/$postId")
                    postRef.removeValue()

                    // preference에서 post ID를 없애줘야함.
                    myPostIdsStr = myPostIdsStr.replace("$boardKey/Posts/$postId,", "")
                    preference.edit().putString(Utils.myPostIdsKey, myPostIdsStr).apply()

                    val commentRef = FirebaseDatabase.getInstance().getReference("$boardKey/Comments/$postId")
                    commentRef.removeValue()

                    val intent: Intent? = when(boardKey) {
                        "bamboo" -> {
                            Intent(applicationContext(), CommunityActivity::class.java)
                        }
                        "career" -> {
                            Intent(applicationContext(), CommunityCareerActivity::class.java)
                        }
                        else -> {
                            Intent(applicationContext(), CommunityMySchoolActivity::class.java)
                        }
                    }

                    intent!!.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    intent.putExtra("boardKey", boardKey)
                    startActivity(intent)

                    //TODO: 게시글이 삭제될 때, 댓글도 같이 삭제되도록 구현한건데, 이 상황에서 각 디바이스에 댓글 id를 모아놓은 shared preference에서 그 댓글 id를 삭제할 방법을 찾아야한다.
                }

                "back" -> {
                    // WriteActivity에게 1을 보내서, activity 종료 시킴.
                    setResult(1)

                    // 수정 모드였을 때는 그 detail activity로 가줘야함.
                    if(writeMode == "editPost") {
                        val intent = Intent(applicationContext(), DetailActivity::class.java)

                        intent.putExtra("boardKey", boardKey)
                        intent.putExtra("postId", postId)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intent)

                    } else {
                        val intent: Intent? = when(boardKey) {
                                "bamboo" -> {
                                    Intent(applicationContext(), CommunityActivity::class.java)
                                }
                                "career" -> {
                                    Intent(applicationContext(), CommunityCareerActivity::class.java)
                                }
                                else -> {
                                    Intent(applicationContext(), CommunityMySchoolActivity::class.java)
                                }
                            }

                        intent!!.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        intent.putExtra("boardKey", boardKey)
                        startActivity(intent)
                    }

                }

                "deleteComment" -> {
                    val commentRef = FirebaseDatabase.getInstance().getReference("$boardKey/Comments/$postId/$commentId")
                    commentRef.removeValue()

                    val postRef = FirebaseDatabase.getInstance().getReference("$boardKey/Posts/$postId")

                    // post의 댓글 개수 불러와서 거기다가 1을 빼준다.
                    postRef.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {

                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            val commentNum = snapshot.child("commentCount").value as Long
                            postRef.child("commentCount").setValue(commentNum - 1)
                        }
                    })

//                    val intent = Intent(applicationContext(), DetailActivity::class.java)
//
//                    intent.putExtra("boardKey", boardKey)
//                    intent.putExtra("postId", postId)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
//                    startActivity(intent)

                }
            }
            finish()

        }

        cancelButton.setOnClickListener {
            when(popUpMode) {
                "delete" -> {
                    val intent = Intent(applicationContext(), DetailActivity::class.java)

                    intent.putExtra("boardKey", boardKey)
                    intent.putExtra("postId", postId)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                }

                "back", "deleteComment" -> {

                }
            }
            finish()
        }

    }
}