package com.example.gupta4_kotlin

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_popup_button.*

class PopupButtonActivity : AppCompatActivity() {
    var postId: String? = "";
    var boardKey: String? = ""
    var popUpMode: String? = ""
    val preference by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popup_button)

        postId = intent.getStringExtra("postId")
        boardKey = intent.getStringExtra("boardKey")
        popUpMode = intent.getStringExtra("popUpMode")

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

                    //TODO: 각각 커뮤니티로 가도록 해줘야함
                    val intent = Intent(this@PopupButtonActivity, CommunityActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    intent.putExtra("boardKey", boardKey)
                    startActivity(intent)

                    //TODO: 게시글이 삭제될 때, 댓글도 같이 삭제되도록 구현한건데, 이 상황에서 댓글 id를 모아놓은 shared preference에서 그 댓글 id를 삭제할 방법을 찾아야한다.
                    finish()
                }

                "back" -> {
                    //TODO: 각각 커뮤니티로 가도록 해줘야함
                    val intent = Intent(this@PopupButtonActivity, CommunityActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    intent.putExtra("boardKey", boardKey)
                    startActivity(intent)

                    finish()
                }
            }

        }

        cancelButton.setOnClickListener {

            finish()
        }


    }
}