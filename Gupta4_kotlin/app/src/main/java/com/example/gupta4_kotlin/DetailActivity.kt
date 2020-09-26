package com.example.gupta4_kotlin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.gupsik_comment.view.*
import kotlinx.android.synthetic.main.gupsik_detail.*


class DetailActivity : AppCompatActivity() {

    val commentList = mutableListOf<Comment>()
    var postId: String? = "";
    var boardKey: String? = ""
    var schoolCode = ""
    val preference by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gupsik_detail)

        postId = intent.getStringExtra("postId")
        boardKey = intent.getStringExtra("boardKey")

        intent.getStringExtra("schoolCode")?.let {
            schoolCode = intent.getStringExtra("schoolCode")!!
            boardKey = boardKey + "/$schoolCode"
        }

        var myPostIdsStr: String = preference.getString(Utils.myPostIdsKey, "").toString()
        var postFullId = "$boardKey/Posts/$postId"

        if(myPostIdsStr.indexOf(postFullId, 0) != -1) {
            deleteButton.visibility = View.VISIBLE
            deleteImageView.visibility = View.VISIBLE

            editButton.visibility = View.VISIBLE
            editImageView.visibility = View.VISIBLE
        }

        deleteButton.setOnClickListener {

            val intent = Intent(this@DetailActivity, PopupButtonActivity::class.java)
            intent.putExtra("boardKey", boardKey)
            intent.putExtra("postId", postId)
            intent.putExtra("popUpMode", "delete")
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)

            finish()
        }

        editButton.setOnClickListener {

            val intent = Intent(this@DetailActivity, WriteActivity::class.java)
            intent.putExtra("boardKey", boardKey)
            intent.putExtra("writeMode", "editPost")
            intent.putExtra("postId", postId)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)

            finish()
    }

        val layoutManager = LinearLayoutManager(this@DetailActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = MyAdapter()

        FirebaseDatabase.getInstance().getReference("$boardKey/Posts/$postId")
            .addValueEventListener(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot?.let {
                        val post = it.getValue(Post::class.java)
                        post?.let {
                            post_title.text = post.title
                            nickname.text = post.nickname
                            contents.text = post.message
                        }
                    }
                }
            })

        FirebaseDatabase.getInstance().getReference("$boardKey/Comments/$postId").addChildEventListener(object
            :ChildEventListener {
            override fun onCancelled(error: DatabaseError) {
                error?.toException()?.printStackTrace()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot != null) {
                    val comment = snapshot.getValue(Comment::class.java)
                    comment?.let {
                        val existIndex = commentList.map{it.commentId}.indexOf(it.commentId)
                        commentList.removeAt(existIndex)

                        val prevIndex = commentList.map{it.commentId}.indexOf(previousChildName)
                        commentList.add(prevIndex+1, it)
                        recycler_view.adapter?.notifyItemInserted(prevIndex + 1)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot?.let { snapshot ->
                    val comment = snapshot.getValue(Comment::class.java)
                    comment?.let{ comment ->
                        val prevIndex = commentList.map{it.commentId}.indexOf(previousChildName)
                        commentList[prevIndex + 1] = comment
                        recycler_view.adapter?.notifyItemChanged(prevIndex + 1)
                    }
                }
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot?.let { snapshot ->
                    val comment = snapshot.getValue(Comment::class.java)
                    comment?.let{
                        // 이 부분은 책 내용과 다르게 내가 마음대로 해봄.
                        if(previousChildName == null) {
                            commentList.add(comment)
                            recycler_view.adapter?.notifyItemInserted(commentList.size - 1)
                        } else {
                            val prevIndex = commentList.map{it.commentId}.indexOf(previousChildName)
                            commentList.add(prevIndex + 1, comment)
                            recycler_view.adapter?.notifyItemInserted(prevIndex + 1)
                        }

                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                snapshot?.let {
                    val comment = snapshot.getValue(Comment::class.java)

                    comment?.let {comment ->
                        val existIndex = commentList.map{it.commentId}.indexOf(comment.commentId)
                        commentList.removeAt(existIndex)
                        recycler_view.adapter?.notifyItemRemoved(existIndex)
                    }
                }
            }

        })

        backButton.setOnClickListener {
            finish()
        }

        registerButton.setOnClickListener {
            val comment = Comment()
            val newRef = FirebaseDatabase.getInstance().getReference("$boardKey/Comments/$postId").push()

            comment.writeTime = ServerValue.TIMESTAMP
            comment.message = comments.text.toString()
            comment.writerId = getMyId()
            comment.commentId = newRef.key.toString()
            comment.postId = postId!!

            newRef.setValue(comment)

            // 댓글 id를 shared preference에 저장
            var myCommentIdsStr: String = preference.getString(Utils.myCommentIdsKey, "").toString()
            myCommentIdsStr = myCommentIdsStr + "$boardKey/Comments/$postId/" + comment.commentId + ","
            preference.edit().putString(Utils.myCommentIdsKey, myCommentIdsStr).apply()

            val postRef = FirebaseDatabase.getInstance().getReference("$boardKey/Posts/$postId")

            // post의 댓글 개수 불러와서 거기다가 1을 더해준다.
            postRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    var commentNum = snapshot.child("commentCount").value as Long
                    postRef.child("commentCount").setValue(commentNum + 1)
                }
            })

            // 댓글단 거 다시 초기화 & 키보드 아래로 내리기
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(comments.windowToken, 0)
            comments.setText("")

        }

        var myLikedPostIdsStr: String = preference.getString(Utils.myLikedPostIdsKey, "").toString()

        // 이미 like한 포스트면, likeButton을 누른 상태로 보여줘야함.
        if(myLikedPostIdsStr.indexOf(postFullId, 0) != -1) {
            likeButtonPressed.visibility = View.VISIBLE
            likeButtonUnpressed.visibility = View.INVISIBLE
        }

        likeButtonUnpressed.setOnClickListener {
            Utils.toggleButton(likeButtonUnpressed)
            Utils.toggleButton(likeButtonPressed)

            myLikedPostIdsStr = myLikedPostIdsStr + postFullId + ","
            preference.edit().putString(Utils.myLikedPostIdsKey, myLikedPostIdsStr).apply()

            // post의 좋아요 개수 불러와서 거기다가 1을 더해준다.
            val postRef = FirebaseDatabase.getInstance().getReference(postFullId)
            postRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    var commentNum = snapshot.child("likesCount").value as Long
                    postRef.child("likesCount").setValue(commentNum + 1)
                }
            })

        }

        likeButtonPressed.setOnClickListener {
            Utils.toggleButton(likeButtonUnpressed)
            Utils.toggleButton(likeButtonPressed)

            // 댓글 id를 shared preference에서 빼줘야 함
            myLikedPostIdsStr = myLikedPostIdsStr.replace(postFullId + ",", "")
            preference.edit().putString(Utils.myLikedPostIdsKey, myLikedPostIdsStr).apply()

            // post의 좋아요 개수 불러와서 거기다가 1을 빼준다.
            val postRef = FirebaseDatabase.getInstance().getReference(postFullId)
            postRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    var commentNum = snapshot.child("likesCount").value as Long
                    postRef.child("likesCount").setValue(commentNum - 1)
                }
            })

        }

        // hitsCountText 갱신해준다.
        val postRef = FirebaseDatabase.getInstance().getReference("$boardKey/Posts/$postId")

        postRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val hitsCount = snapshot.child("hitsCount").getValue()
                hitsCountText.setText(hitsCount.toString())

                val writeTime = snapshot.child("writeTime").getValue()
                val date = Utils.getDiffTimeText(writeTime as Long)
                dateTextView.setText(date)
            }
        })

    }

    fun getMyId(): String {
        return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val commentText = itemView.findViewById<TextView>(R.id.comment_text)
        val commentWriteTime = itemView.dateTextView
        val commentNickname = itemView.nickname
        val deleteTextView = itemView.deleteTextView
    }

    inner class MyAdapter: RecyclerView.Adapter<MyViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(this@DetailActivity)
                .inflate(R.layout.gupsik_comment, parent, false))
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val comment = commentList[position]
            comment?.let {
                holder.commentText.text = comment.message
                holder.commentWriteTime.text = Utils.getDiffTimeText(comment.writeTime as Long)

                //TODO: 여기에 익명1, 익명2을 id값에 따라 mapping해주는 것이 필요함.
                // holder.commentNickname.text = comment.nickname
            }

            // 본인이 쓴 댓글이면 삭제 버튼이 보이도록 해야함.
            val myCommentIdsStr: String = preference.getString(Utils.myCommentIdsKey, "").toString()
            val commentId = comment.commentId

            if(myCommentIdsStr.indexOf("$boardKey/Comments/$postId/$commentId", 0) != -1) {
                holder.deleteTextView.visibility = View.VISIBLE
            }

            holder.deleteTextView.setOnClickListener {

                val commentRef = FirebaseDatabase.getInstance().getReference("$boardKey/Comments/$postId/$commentId")
                commentRef.removeValue()

                // 댓글 id를 shared preference에서 빼줘야 함
                var myCommentIdsStr: String = preference.getString(Utils.myCommentIdsKey, "").toString()
                myCommentIdsStr = myCommentIdsStr.replace("$boardKey/Comments/$postId/$commentId,", "")
                preference.edit().putString(Utils.myCommentIdsKey, myCommentIdsStr).apply()

                val postRef = FirebaseDatabase.getInstance().getReference("$boardKey/Posts/$postId")

                // post의 댓글 개수 불러와서 거기다가 1을 빼준다.
                postRef.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        var commentNum = snapshot.child("commentCount").value as Long
                        postRef.child("commentCount").setValue(commentNum - 1)
                    }
                })
            }

        }

        override fun getItemCount(): Int {
            return commentList.size
        }

    }

    // 점점점 메뉴 옵션 넣은부분. 수정, 삭제 가능하도록 구현!
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId) {
            R.id.edit -> {
                val intent = Intent(this, WriteActivity::class.java)
                intent.putExtra("writeMode", "editPost")
                intent.putExtra("postId", postId)
                startActivity(intent)

                return true
            }

            R.id.delete -> {
                val postRef = FirebaseDatabase.getInstance().getReference("$boardKey/Posts/$postId")
                postRef.removeValue()

                val commentRef = FirebaseDatabase.getInstance().getReference("$boardKey/Comments/$postId")
                commentRef.removeValue()

                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

}

