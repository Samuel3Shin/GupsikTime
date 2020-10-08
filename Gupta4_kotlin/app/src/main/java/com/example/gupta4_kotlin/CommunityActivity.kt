package com.example.gupta4_kotlin

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_community.*
import kotlinx.android.synthetic.main.activity_community.adView
import kotlinx.android.synthetic.main.activity_community.buttonUpper

open class CommunityActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {

    lateinit var context: Context

    init {
        instance = this
    }

    companion object {
        private var instance: CommunityActivity? = null
        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    val posts: MutableList<Post> = mutableListOf()
    open var boardKey = "bamboo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        //배너 광고 추가
        MobileAds.initialize(this, getString(R.string.admob_app_id))
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // 게시판 설명 부분변경
        boardDescribeTextView.text = "전국 학생들과 익명으로 소통하세요"
        leftImageView.setImageResource(R.drawable.ic_bamboo_left_icon)
        rightImageView.setImageResource(R.drawable.ic_bamboo_right_icon)

        recyclerView.layoutManager?.scrollToPosition(0)

        buttonUpper.setOnClickListener {
            val popup = PopupMenu(this@CommunityActivity, it)
            popup.setOnMenuItemClickListener(this@CommunityActivity)
            popup.inflate(R.menu.main)
            popup.show()

        }

        writeButton.setOnClickListener {
            val intent = Intent(CommunityActivity.applicationContext(), WriteActivity::class.java)
            intent.putExtra("boardKey", boardKey)
            intent.putExtra("writeMode", "post")
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)

        }

        val layoutManager = LinearLayoutManager(CommunityActivity.applicationContext())

        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = CommunityAdapter(this@CommunityActivity, posts, boardKey)

        FirebaseDatabase.getInstance().getReference("$boardKey/Posts")
            .orderByChild("writeTime").addChildEventListener(object: ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot?.let { snapshot ->
                        val post = snapshot.getValue(Post::class.java)
                        post?.let {
                            if(previousChildName == null) {
                                posts.add(it)
                                recyclerView.adapter?.notifyItemInserted(posts.size - 1)
                            } else {
                                val prevIndex = posts.map {it.postId}.indexOf(previousChildName)
                                posts.add(prevIndex + 1, post)
                                recyclerView.adapter?.notifyItemInserted(prevIndex + 1)
                            }
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot?.let{snapshot ->
                        val post = snapshot.getValue(Post::class.java)
                        post?.let { post ->
                            val prevIndex = posts.map{it.postId}.indexOf(previousChildName)
                            posts[prevIndex + 1] = post
                            recyclerView.adapter?.notifyItemChanged(prevIndex + 1)
                        }
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot?.let {
                        val post = snapshot.getValue(Post::class.java)

                        post?.let {post ->
                            val existIndex = posts.map {it.postId}.indexOf(post.postId)
                            posts.removeAt(existIndex)
                            recyclerView.adapter?.notifyItemRemoved(existIndex)

                            if (previousChildName == null) {
                                posts.add(post)
                                recyclerView.adapter?.notifyItemChanged(posts.size-1)
                            } else {
                                val prevIndex = posts.map{it.postId}.indexOf(previousChildName)
                                posts.add(prevIndex + 1, post)
                                recyclerView.adapter?.notifyItemChanged(prevIndex + 1)
                            }
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    snapshot?.let {
                        val post = snapshot.getValue(Post::class.java)

                        post?.let {post ->
                            val existIndex = posts.map {it.postId}.indexOf(post.postId)
                            posts.removeAt(existIndex)
                            recyclerView.adapter?.notifyItemRemoved(existIndex)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    error?.toException()?.printStackTrace()
                }
            })

        bambooButton.setOnClickListener {
            val intent = Intent(CommunityActivity.applicationContext(), CommunityActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)

            finish()

        }

        careerButton.setOnClickListener {
            val intent = Intent(CommunityActivity.applicationContext(), CommunityCareerActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)

            finish()

        }

        mySchoolButton.setOnClickListener{
            val intent = Intent(CommunityActivity.applicationContext(), CommunityMySchoolActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)

            finish()

        }


    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_mealInfo ->  {
                val intent = Intent(CommunityActivity.applicationContext(), MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)

                finish()

                return true
            }

            R.id.menu_board ->  {
                return true
            }

            R.id.menu_myPage ->  {
                val intent = Intent(CommunityActivity.applicationContext(), MyPostsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)

                finish()

                return true
            }
        }
        return super.onOptionsItemSelected(item!!)
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
        super.onDestroy()
    }

}