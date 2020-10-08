package com.example.gupta4_kotlin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_my_posts.*
import kotlinx.android.synthetic.main.activity_my_posts.adView
import kotlinx.android.synthetic.main.activity_my_posts.buttonUpper
import kotlinx.android.synthetic.main.activity_my_posts.recyclerView

class MyPostsActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {

    init {
        instance = this
    }

    companion object {
        private var instance: MyPostsActivity? = null
        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    val posts: MutableList<Post> = mutableListOf()
    val boardKeys: MutableList<String> = mutableListOf()
    val preference: SharedPreferences by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_posts)

        recyclerView.layoutManager?.scrollToPosition(0)

        //배너 광고 추가
        MobileAds.initialize(applicationContext(), getString(R.string.admob_app_id))
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        buttonUpper.setOnClickListener {
            val popup = PopupMenu(this@MyPostsActivity, it)
            popup.setOnMenuItemClickListener(this@MyPostsActivity)
            popup.inflate(R.menu.main)
            popup.show()
        }

        mySettingTab.setOnClickListener {
            val intent = Intent(applicationContext(), MySettingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)

            finish()
        }

        val layoutManager = LinearLayoutManager(applicationContext())

        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = MyPostsAdapter(this@MyPostsActivity, posts, boardKeys)

        val myPostIdsStr = preference.getString(Utils.myPostIdsKey, "").toString()
        val myPostIdsList = myPostIdsStr.split(",")

        // 마지막 index는 "" 이므로 쓰면 안 됨!
        for(i in 0 until myPostIdsList.size - 1) {

            // 일단 올리기에는 성공. 근데 이후에 아이템 정보 변경에 대해서는 handling 못 함.
            val postRef = FirebaseDatabase.getInstance().getReference(myPostIdsList[i])
            postRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.let {
                        val post = it.getValue(Post::class.java)
                        post?.let {
                            posts.add(it)
                            boardKeys.add(myPostIdsList[i].split("/Posts/")[0])
                            recyclerView.adapter?.notifyItemInserted(posts.size - 1)
                        }
                    }
                }

                override fun  onCancelled(error: DatabaseError) {

                }

            })

        }

    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_mealInfo ->  {
                val intent = Intent(applicationContext(), MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)

                finish()

                return true
            }

            R.id.menu_board ->  {
                val intent = Intent(applicationContext(), CommunityActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)

                finish()

                return true
            }

            R.id.menu_myPage ->  {
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