package com.example.gupta4_kotlin

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_my_posts.*
import kotlinx.android.synthetic.main.activity_my_posts.recyclerView
import kotlinx.android.synthetic.main.gupsik_post.view.*

class MyPostsActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {
    val posts: MutableList<Post> = mutableListOf()
    val boardKeys: MutableList<String> = mutableListOf()
    val preference by lazy {getSharedPreferences("mainActivity", Context.MODE_PRIVATE)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_posts)

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
//        Log.d("tkandpf", "my Post Ids length: " + myPostIdsList.size.toString())

        for(i in 0 until myPostIdsList.size - 1) {

            // 일단 올리기에는 성공. 근데 이후에 아이템 정보 변경에 대해서는 handling 못 함.
//            Log.d("tkandpf", i.toString() + ":" + myPostIdsList.get(i))
            val postRef = FirebaseDatabase.getInstance().getReference(myPostIdsList.get(i))
            postRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot?.let {
                        val post = it.getValue(Post::class.java)
                        post?.let {
                            posts.add(it)
                            boardKeys.add(myPostIdsList.get(i).split("/Posts/").get(0))
                            recyclerView.adapter?.notifyItemInserted(posts.size - 1)
//                            Log.d("tkandpf", "posts size: " + posts.size.toString())
                        }
                    }
                }

                override fun  onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        }

    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contentsText: TextView = itemView.contentsText
        val timeTextView: TextView = itemView.timeTextView
        val commentCountText: TextView = itemView.commentCountText
        val hitsCountText: TextView = itemView.hitsCountText
        val titleText: TextView = itemView.titleTextView
        val nicknameText: TextView = itemView.nicknameTextView
        val likesCountText: TextView = itemView.likesCountText
    }

    inner class MyAdapter: RecyclerView.Adapter<MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(
                LayoutInflater.from(this@MyPostsActivity)
                    .inflate(R.layout.gupsik_post, parent, false))
        }

        override fun getItemCount(): Int {
            return posts.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val post = posts[position]
            holder.contentsText.text = post.message
            holder.timeTextView.text = Utils.getDiffTimeText(post.writeTime as Long)
            holder.commentCountText.text = post.commentCount.toString()
            holder.hitsCountText.text = post.hitsCount.toString()
            holder.titleText.text = post.title
            holder.nicknameText.text = post.nickName
            holder.likesCountText.text = post.likesCount.toString()


            holder.itemView.setOnClickListener {
                val intent = Intent(this@MyPostsActivity, DetailActivity::class.java)
                val boardKey = boardKeys.get(position)

                intent.putExtra("boardKey", boardKey)
                intent.putExtra("postId", post.postId)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)

                finish()

            }

        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_mealInfo ->  {
                Toast.makeText(this@MyPostsActivity, "급식메뉴!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                return true
            }

            R.id.menu_board ->  {
                Toast.makeText(this@MyPostsActivity, "게시판!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, CommunityActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)

                return true
            }

            R.id.menu_myPage ->  {
                Toast.makeText(this@MyPostsActivity, "마이 페이지!", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item!!)
    }

}