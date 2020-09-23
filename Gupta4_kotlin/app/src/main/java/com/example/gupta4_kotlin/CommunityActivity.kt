package com.example.gupta4_kotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_community.*
import kotlinx.android.synthetic.main.activity_community.buttonUpper

open class CommunityActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {
    val posts: MutableList<Post> = mutableListOf()
    open var boardKey = "bamboo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        // background image alpha값 조절
        recyclerView.background.alpha = 95

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
            boardKey = "bamboo"
            val intent = Intent(this@CommunityActivity, CommunityActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.putExtra("boardKey", boardKey)
            startActivity(intent)

        }
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
                val intent = Intent(this, MyPostsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item!!)
    }

}