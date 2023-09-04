package com.oznurdemir.kotlininstagramclone

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.oznurdemir.kotlininstagramclone.databinding.RecycleRowBinding
import com.squareup.picasso.Picasso

class FeedRecycleAdapter(private val postList:ArrayList<Post>): RecyclerView.Adapter<FeedRecycleAdapter.PostHolder>() {
  class PostHolder(val binding: RecycleRowBinding):RecyclerView.ViewHolder(binding.root){

  }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = RecycleRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostHolder(binding)
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.binding.recycleEmailText.text = postList.get(position).email
        holder.binding.recycleCommentText.text = postList.get(position).comment
        Picasso.get().load(postList.get(position).image).into(holder.binding.recycleImageView)
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}