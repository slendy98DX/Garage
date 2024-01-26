package com.example.garage.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.garage.R
import com.example.garage.database.models.Logo
import java.util.*

class LogoAdapter(
    private var logos: List<Logo>? = null,
    private val logoClickListener: (Logo) -> Unit
) : RecyclerView.Adapter<LogoAdapter.ViewHolder>() {

    private var filteredLogos: List<Logo> = emptyList()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val logoImageView: ImageView = itemView.findViewById(R.id.logoImageView)
        val logoNameTextView: TextView = itemView.findViewById(R.id.logoNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_logo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val logo = filteredLogos[position]

        holder.logoNameTextView.text = logo.name

        holder.logoImageView.load(logo.image.source) {
            placeholder(R.drawable.loading_animation)
            error(R.drawable.ic_connection_error)
            crossfade(true)
        }

        holder.itemView.setOnClickListener {
            logoClickListener.invoke(logo)
        }
    }

    override fun getItemCount(): Int {
        return filteredLogos.size
    }

    fun updateLogos(newLogos: List<Logo>?) {
        logos = newLogos
        filter("") // Reset filter when updating logos
    }

    fun filter(query: String) {
        val searchQuery = query.lowercase(Locale.getDefault()).trim()

        filteredLogos = if (searchQuery.isEmpty()) {
            logos ?: emptyList()
        } else {
            logos?.filter { logo ->
                logo.name.lowercase(Locale.getDefault()).startsWith(searchQuery)
            } ?: emptyList()
        }

        notifyDataSetChanged()
    }
}
