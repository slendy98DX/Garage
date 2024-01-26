package com.example.garage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.garage.database.models.Car
import com.example.garage.databinding.ItemCarBinding

class CarAdapter(private val carListener: CarListener) :
    ListAdapter<Car, CarAdapter.CarViewHolder>(DiffCallback) {

    class CarViewHolder(
        private val binding: ItemCarBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            carListener: CarListener,
            car: Car
        ) {
            binding.apply {
                root.setOnClickListener {
                    carListener.onClick(car)
                }

                brand.text = car.brand
                year.text = car.year.toString()
                model.text = car.model
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Car>() {
        override fun areItemsTheSame(oldItem: Car, newItem: Car): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Car, newItem: Car): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CarViewHolder(
            ItemCarBinding.inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val car = getItem(position)
        holder.bind(carListener, car)
    }
}

class CarListener(val clickListener: (car: Car) -> Unit) {
    fun onClick(car: Car) = clickListener(car)
}
