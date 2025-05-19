package com.kanahia.stockbrokingplatform.ui.details.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.kanahia.stockbrokingplatform.R
import com.kanahia.stockbrokingplatform.domain.model.NewsItem

class NewsAdapter(private val newsList: List<NewsItem>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bannerImageView: ImageView = itemView.findViewById(R.id.bannerImageView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currentItem = newsList[position]

        // Set title with ellipsize
        holder.titleTextView.text = currentItem.title

        // Set description with ellipsize
        holder.descriptionTextView.text = currentItem.description

        // Load and cache image using Glide
        Glide.with(holder.itemView.context)
            .load(currentItem.bannerImage)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .placeholder(R.drawable.placeholder_image) // Replace with your placeholder image
            .error(R.drawable.error_image) // Replace with your error image
            .into(holder.bannerImageView)

        // Set click listener to open webpage
        holder.itemView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentItem.url))
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = newsList.size
}