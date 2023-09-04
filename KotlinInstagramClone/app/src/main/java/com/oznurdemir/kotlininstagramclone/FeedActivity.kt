package com.oznurdemir.kotlininstagramclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.oznurdemir.kotlininstagramclone.databinding.ActivityFeedBinding

class FeedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var postArrayList: ArrayList<Post>
    private lateinit var postAdapter: FeedRecycleAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        postArrayList = ArrayList<Post>()

        getData()

        binding.recycleView.layoutManager = LinearLayoutManager(this)
        postAdapter = FeedRecycleAdapter(postArrayList) // oluşturduğumuz adapter'a listemizi yolluyoruz.
        binding.recycleView.adapter = postAdapter

    }

    private fun getData(){
        db.collection("Posts").orderBy("date",Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if(error != null){
                Toast.makeText(this@FeedActivity,error.localizedMessage,Toast.LENGTH_LONG)
            }else{
                if(value != null){ //value null değilse
                    if(!value.isEmpty){ //gelen value değerinin içi boş değilse
                        val documents = value.documents //dokümanları bir liste halinde getirip değere atadık.
                        postArrayList.clear()
                        for(document in documents){
                            //casting
                            val comment = document.get("comment") as String
                            val user = document.get("user") as String
                            val image = document.get("image") as String

                            //recycleview'da gösterebilmek için model oluşturup bir arraylist'e ekliyoruz.
                            val post = Post(user,comment,image)
                            postArrayList.add(post)

                        }
                        postAdapter.notifyDataSetChanged()// yeni gelen verilerle kendini güncelle
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.options_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.add_post){
            val intent = Intent(this@FeedActivity,UploadActivity::class.java)
            startActivity(intent)
        }else if(item.itemId == R.id.log_out){
            auth.signOut() // Kullanıcı çıkışı
            val intent = Intent(this@FeedActivity,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}