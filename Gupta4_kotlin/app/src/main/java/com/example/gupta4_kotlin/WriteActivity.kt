package com.example.gupta4_kotlin

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_write.*

class WriteActivity : AppCompatActivity() {

    var mode = "post"
    var postId = ""
    var boardName = ""
    var boardKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)

        backButton.setOnClickListener {
            finish()
        }

        intent.getStringExtra("boardKey")?.let {
            boardKey = intent.getStringExtra("boardKey")!!
        }
        intent.getStringExtra("postId")?.let {
            postId = intent.getStringExtra("postId")!!
        }
        mode = intent.getStringExtra("mode").toString()

        when(boardKey) {
            "bamboo" -> {
                boardName = "대나무숲"
            }

            "career" -> {
                boardName = "진로고민"
            }

            else -> {
                boardName = "우리학교"
            }
        }

        boardNameTextView.setText(boardName)
        Toast.makeText(applicationContext, boardName, Toast.LENGTH_SHORT).show()

        if(mode=="editPost") {

            val postRef = FirebaseDatabase.getInstance().getReference("$boardKey/Posts/$postId")

            postRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val title = snapshot.child("title").getValue()
                    titleTextView_write.setText(title.toString())

                    val nickname = snapshot.child("nickname").getValue()
                    nicknameTextView_write.setText(nickname.toString())

                    val text = snapshot.child("message").getValue()
                    input.setText(text.toString())
                }
            })
        }

        registerButton.setOnClickListener {
            if(TextUtils.isEmpty(input.text)) {
                Toast.makeText(applicationContext, "메시지를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(mode=="post") {
                val post = Post()
                val newRef = FirebaseDatabase.getInstance().getReference("$boardKey/Posts").push()
                post.writeTime = ServerValue.TIMESTAMP
                post.message = input.text.toString()
                post.writerId = getMyId()
                post.postId = newRef.key.toString()
                post.title = titleTextView_write.text.toString()
                post.nickname = nicknameTextView_write.text.toString()
                newRef.setValue(post)

                // post아이디를 shared preference에 저장. ','를 구분자로 저장함.
                val preference by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}
                var myPostIdsStr: String = preference.getString(Utils.myPostIdsKey, "").toString()
                myPostIdsStr = myPostIdsStr + "$boardKey/Posts/" + post.postId + ","
                preference.edit().putString(Utils.myPostIdsKey, myPostIdsStr).apply()
                Toast.makeText(applicationContext, "공유 되었습니다.", Toast.LENGTH_SHORT).show()
                finish()

            } else if (mode=="editPost") {
                val postRef = FirebaseDatabase.getInstance().getReference("$boardKey/Posts/$postId")
                postRef.child("title").setValue(titleTextView_write.text.toString())
                postRef.child("nickname").setValue(nicknameTextView_write.text.toString())
                postRef.child("message").setValue(input.text.toString())

                finish()

            }

        }

    }

    fun getMyId(): String {
        return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
    }

}