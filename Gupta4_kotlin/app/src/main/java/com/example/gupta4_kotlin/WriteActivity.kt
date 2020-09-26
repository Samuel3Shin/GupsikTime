package com.example.gupta4_kotlin

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_write.*

class WriteActivity : AppCompatActivity() {

    var writeMode = "post"
    var postId = ""
    var boardName = ""
    var boardKey = ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == 1) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)

        intent.getStringExtra("boardKey")?.let {
            boardKey = intent.getStringExtra("boardKey")!!
        }
        intent.getStringExtra("postId")?.let {
            postId = intent.getStringExtra("postId")!!
        }
        writeMode = intent.getStringExtra("writeMode").toString()

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

        backButton.setOnClickListener {
            val intent = Intent(this@WriteActivity, PopupButtonActivity::class.java)
            intent.putExtra("boardKey", boardKey)
            intent.putExtra("postId", postId)
            intent.putExtra("writeMode", writeMode)
            intent.putExtra("popUpMode", "back")
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

            // PopupButtonActivity로 부터 1을 받으면 activity 종료시킨다.
            startActivityForResult(intent, 1)
        }

        if(writeMode=="editPost") {

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
                return@setOnClickListener
            }
            if(writeMode=="post") {
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

                // 여기서 새로 쓰는 글의 postId와 수정을 위해서 가져온 postId는 다르다는 걸 숙지하고 있어야한다.
                val intent = Intent(this@WriteActivity, DetailActivity::class.java)

                intent.putExtra("boardKey", boardKey)
                intent.putExtra("postId", post.postId)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)

            } else if (writeMode=="editPost") {
                val postRef = FirebaseDatabase.getInstance().getReference("$boardKey/Posts/$postId")
                postRef.child("title").setValue(titleTextView_write.text.toString())
                postRef.child("nickname").setValue(nicknameTextView_write.text.toString())
                postRef.child("message").setValue(input.text.toString())

                val intent = Intent(this@WriteActivity, DetailActivity::class.java)

                intent.putExtra("boardKey", boardKey)
                intent.putExtra("postId", postId)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }

            finish()
        }

    }

    fun getMyId(): String {
        return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
    }

}