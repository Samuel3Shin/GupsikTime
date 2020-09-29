package com.example.gupta4_kotlin

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_community.*
import kotlinx.android.synthetic.main.activity_my_posts.*
import kotlinx.android.synthetic.main.activity_my_posts.adView
import kotlinx.android.synthetic.main.activity_my_posts.buttonUpper
import kotlinx.android.synthetic.main.activity_my_posts.recyclerView

class MyPostsActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {
    val posts: MutableList<Post> = mutableListOf()
    val boardKeys: MutableList<String> = mutableListOf()
    val preference by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_posts)

        recyclerView.layoutManager?.scrollToPosition(0)

        //배너 광고 추가
        MobileAds.initialize(this, getString(R.string.admob_app_id))
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        buttonUpper.setOnClickListener {
            val popup = PopupMenu(this@MyPostsActivity, it)
            popup.setOnMenuItemClickListener(this@MyPostsActivity)
            popup.inflate(R.menu.main)
            popup.show()
        }

        mySettingTab.setOnClickListener {
            val intent = Intent(this, MySettingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)

        }

        val layoutManager = LinearLayoutManager(this@MyPostsActivity)

        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = MyAdapter()

        val myPostIdsStr = preference.getString(Utils.myPostIdsKey, "").toString()
        var myPostIdsList = myPostIdsStr.split(",")

        for(i in 0 until myPostIdsList.size - 1) {

            // 일단 올리기에는 성공. 근데 이후에 아이템 정보 변경에 대해서는 handling 못 함.
            val postRef = FirebaseDatabase.getInstance().getReference(myPostIdsList.get(i))
            postRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot?.let {
                        val post = it.getValue(Post::class.java)
                        post?.let {
                            posts.add(it)
                            boardKeys.add(myPostIdsList.get(i).split("/Posts/").get(0))
                            recyclerView.adapter?.notifyItemInserted(posts.size - 1)
                        }
                    }
                }

                override fun  onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        }

    }

    inner class MyAdapter: RecyclerView.Adapter<CommunityViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
            return CommunityViewHolder(
                LayoutInflater.from(this@MyPostsActivity)
                    .inflate(R.layout.gupsik_post, parent, false))
        }

        override fun getItemCount(): Int {
            return posts.size
        }

        override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
            val post = posts[position]
            holder.contentsText.text = post.message
            holder.timeTextView.text = Utils.getDiffTimeText(post.writeTime as Long)
            holder.commentCountText.text = post.commentCount.toString()
            holder.hitsCountText.text = post.hitsCount.toString()
            holder.nicknameText.text = post.nickname
            holder.likesCountText.text = post.likesCount.toString()


            holder.itemView.setOnClickListener {
                val intent = Intent(this@MyPostsActivity, DetailActivity::class.java)
                var boardKey = boardKeys.get(position)

                intent.putExtra("boardKey", boardKey)
                intent.putExtra("postId", post.postId)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)

            }

        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_mealInfo ->  {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)

                return true
            }

            R.id.menu_board ->  {
                val intent = Intent(this, CommunityActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)

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