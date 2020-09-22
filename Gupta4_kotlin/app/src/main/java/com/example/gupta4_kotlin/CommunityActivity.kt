package com.example.gupta4_kotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import kotlinx.android.synthetic.main.activity_community.*
import kotlinx.android.synthetic.main.activity_community.buttonUpper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.gupsik_post.view.*

class CommunityActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {
    val posts: MutableList<Post> = mutableListOf()
    var boardKey = "bamboo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        buttonUpper.setOnClickListener {
            val popup = PopupMenu(this@CommunityActivity, it)
            popup.setOnMenuItemClickListener(this@CommunityActivity)
            popup.inflate(R.menu.main)
            popup.show()
        }

        writeButton.setOnClickListener {
            val intent = Intent(this@CommunityActivity, WriteActivity::class.java)
            intent.putExtra("boardKey", boardKey)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }



        val layoutManager = LinearLayoutManager(this@CommunityActivity)

        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = MyAdapter()

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

        careerButton.setOnClickListener {
            boardKey = "career"
            val intent = Intent(this@CommunityActivity, CommunityCareerActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.putExtra("boardKey", boardKey)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)

        }

        mySchoolButton.setOnClickListener{
            boardKey = "mySchool"
            val intent = Intent(this@CommunityActivity, CommunityMySchoolActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.putExtra("boardKey", boardKey)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
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
                LayoutInflater.from(this@CommunityActivity)
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
                val intent = Intent(this@CommunityActivity, DetailActivity::class.java)
                intent.putExtra("boardKey", boardKey)
                intent.putExtra("postId", post.postId)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)

                // hits 개수 늘려주기 추가
                val id = post.postId
                val postRef = FirebaseDatabase.getInstance().getReference("$boardKey/Posts/$id")

                postRef.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        var hitsNum = snapshot.child("hitsCount").value as Long
                        postRef.child("hitsCount").setValue(hitsNum + 1)
                    }
                })
            }

        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_mealInfo ->  {
                Toast.makeText(this@CommunityActivity, "급식메뉴!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                return true
            }

            R.id.menu_board ->  {
                Toast.makeText(this@CommunityActivity, "게시판!", Toast.LENGTH_SHORT).show()
                return true
            }

            R.id.menu_myPage ->  {
                Toast.makeText(this@CommunityActivity, "마이 페이지!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MySettingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item!!)
    }

}