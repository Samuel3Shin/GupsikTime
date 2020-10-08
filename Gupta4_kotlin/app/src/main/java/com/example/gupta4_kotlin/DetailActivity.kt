package com.example.gupta4_kotlin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.gupsik_detail.*
import kotlinx.android.synthetic.main.gupsik_detail.adView
import kotlinx.android.synthetic.main.gupsik_detail.dateTextView
import kotlinx.android.synthetic.main.gupsik_detail.shareButton

class DetailActivity : AppCompatActivity() {
    init {
        instance = this
    }

    companion object {
        private var instance: DetailActivity? = null
        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    val commentList = mutableListOf<Comment>()
    var postId: String? = ""
    var boardKey: String? = ""
    val preference: SharedPreferences by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}

    private lateinit var postReference: DatabaseReference
    private lateinit var postListener: ValueEventListener

    private lateinit var commentReference: DatabaseReference
    private lateinit var commentListener: ChildEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gupsik_detail)

        postId = intent.getStringExtra("postId")
        boardKey = intent.getStringExtra("boardKey")

        val postFullId = "$boardKey/Posts/$postId"

        deleteButton.setOnClickListener {

            val intent = Intent(applicationContext(), PopupButtonActivity::class.java)
            intent.putExtra("boardKey", boardKey)
            intent.putExtra("postId", postId)
            intent.putExtra("popUpMode", "delete")
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)

            finish()

        }

        editButton.setOnClickListener {

            val intent = Intent(applicationContext(), WriteActivity::class.java)
            intent.putExtra("boardKey", boardKey)
            intent.putExtra("writeMode", "editPost")
            intent.putExtra("postId", postId)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)

            finish()

        }

        shareButton.setOnClickListener {
            onClickShareButton()
        }

        val layoutManager = LinearLayoutManager(applicationContext())
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = CommentAdapter(this@DetailActivity, commentList, boardKey!!, postId!!)

        //배너 광고 추가
        MobileAds.initialize(applicationContext(), getString(R.string.admob_app_id))
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        postListener = object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.let {
                    val post = it.getValue(Post::class.java)
                    post?.let {
                        boardNameTextView.text = post.board
                        post_title.text = post.title
                        nickname.text = post.nickname
                        dateTextView.text = Utils.getDiffTimeText(post.writeTime as Long)
                        hitsCountText.text = post.hitsCount.toString()
                        commentCountText.text = post.commentCount.toString()
                        likesCountText.text = post.likesCount.toString()
                        contents.text = post.message

                        if(post.writerId == getMyId()) {
                            deleteButton.visibility = View.VISIBLE
                            deleteImageView.visibility = View.VISIBLE
                            editButton.visibility = View.VISIBLE
                            editImageView.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
        postReference = FirebaseDatabase.getInstance().getReference("$boardKey/Posts/$postId")
        postReference.addValueEventListener(postListener)

        commentListener = object :ChildEventListener {
            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
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
                snapshot.let { snapshot ->
                    val comment = snapshot.getValue(Comment::class.java)
                    comment?.let{ comment ->
                        val prevIndex = commentList.map{it.commentId}.indexOf(previousChildName)
                        commentList[prevIndex + 1] = comment
                        recycler_view.adapter?.notifyItemChanged(prevIndex + 1)
                    }
                }
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.let { snapshot ->
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
                snapshot.let {
                    val comment = snapshot.getValue(Comment::class.java)

                    comment?.let {comment ->
                        val existIndex = commentList.map{it.commentId}.indexOf(comment.commentId)
                        commentList.removeAt(existIndex)
                        recycler_view.adapter?.notifyItemRemoved(existIndex)
                    }
                }
            }
        }
        commentReference = FirebaseDatabase.getInstance().getReference("$boardKey/Comments/$postId")
        commentReference.addChildEventListener(commentListener)

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

            val postRef = FirebaseDatabase.getInstance().getReference("$boardKey/Posts/$postId")
            var commentIdMap: HashMap<String, String>
            var idCnt: String
            // post의 댓글 개수 불러와서 거기다가 1을 더해준다.
            postRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val commentNum = snapshot.child("commentCount").value as Long
                    postRef.child("commentCount").setValue(commentNum + 1)

                    commentIdMap = snapshot.child("commentIdMap").value as HashMap<String, String>

                    if(!commentIdMap.containsKey(getMyId())){
                        commentIdMap[getMyId()] = commentIdMap.size.toString()
                    }

                    postRef.child("commentIdMap").setValue(commentIdMap)

                    idCnt = commentIdMap[getMyId()]!!
                    comment.nickname = "익명$idCnt"
                    newRef.setValue(comment)

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

            myLikedPostIdsStr = "$myLikedPostIdsStr$postFullId,"
            preference.edit().putString(Utils.myLikedPostIdsKey, myLikedPostIdsStr).apply()

            // post의 좋아요 개수 불러와서 거기다가 1을 더해준다.
            val postRef = FirebaseDatabase.getInstance().getReference(postFullId)
            postRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val commentNum = snapshot.child("likesCount").value as Long
                    postRef.child("likesCount").setValue(commentNum + 1)
                }
            })

        }

        likeButtonPressed.setOnClickListener {
            Utils.toggleButton(likeButtonUnpressed)
            Utils.toggleButton(likeButtonPressed)

            // 댓글 id를 shared preference에서 빼줘야 함
            myLikedPostIdsStr = myLikedPostIdsStr.replace("$postFullId,", "")
            preference.edit().putString(Utils.myLikedPostIdsKey, myLikedPostIdsStr).apply()

            // post의 좋아요 개수 불러와서 거기다가 1을 빼준다.
            val postRef = FirebaseDatabase.getInstance().getReference(postFullId)
            postRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val commentNum = snapshot.child("likesCount").value as Long
                    postRef.child("likesCount").setValue(commentNum - 1)
                }
            })
        }
    }

    @SuppressLint("HardwareIds")
    fun getMyId(): String {
        return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
    }

    // Share callback function
    private fun onClickShareButton(){
        val bitmap: Bitmap = Bitmap.createBitmap(nestedScrollView.measuredWidth, nestedScrollView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        nestedScrollView.draw(canvas)

        if (bitmap == null)
            return

        val bitmapURI = Utils.getImageUri(applicationContext, bitmap, "게시글")
        //TODO: facebook은 text intent를 허용하지 않는듯?? 앱다운로드 링크를 어떻게 보낼지 생각해봐야함
        //TODO: 사진만 보내는 것은 잘 되는데, 텍스트도 같이 보내는 건 안 될때가 있다. 왜 그런지 살펴봐야함.
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, bitmapURI)
            putExtra(Intent.EXTRA_TEXT, "이것은 공유링크")
        }
        startActivity(Intent.createChooser(shareIntent, "share image and text!"))
    }

    // Called when leaving the activity
    public override fun onPause() {
        adView.pause()
        super.onPause()
    }

    // Called when returning to the activity
    public override fun onResume() {
        super.onResume()
        adView.resume()
    }

    // Called before the activity is destroyed
    public override fun onDestroy() {
        adView.destroy()
        postReference.removeEventListener(postListener)
        commentReference.removeEventListener(commentListener)
        super.onDestroy()
    }

}

