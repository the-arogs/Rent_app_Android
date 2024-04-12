package com.rg.rentapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rg.rentapp.R
import com.rg.rentapp.classes.Rental

class PropertyListAdaptor (
    val removeFunction: (Int) -> Unit,
    val viewDetailsFunction: (Int) -> Unit,
    val viewDetailsFunction2: (Int) -> Unit,

    ): RecyclerView.Adapter<PropertyListAdaptor.PropertyListViewHolder>() {
    private var propertyList:MutableList<Rental> = mutableListOf()

    fun submitList(newData: List<Rental>) {
        propertyList.addAll(newData)
        notifyDataSetChanged()
    }

    inner class PropertyListViewHolder(itemView: View) : RecyclerView.ViewHolder (itemView)  {
        init {
            var btnRmvRental = itemView.findViewById<Button>(R.id.btn_rmv_rental)
            btnRmvRental.setOnClickListener { removeFunction(adapterPosition) }

            itemView.setOnClickListener{ viewDetailsFunction2(adapterPosition)}

            var btnUpdate = itemView.findViewById<Button>(R.id.btn_update_rental)
            btnUpdate.setOnClickListener{ viewDetailsFunction(adapterPosition)}
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyListViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_property_list, parent, false)
        return PropertyListViewHolder(view)

    }

    override fun getItemCount(): Int {
        return propertyList.size
    }

    override fun onBindViewHolder(holder: PropertyListViewHolder, position: Int) {

        val currProperty:Rental = propertyList.get(position)

        holder.itemView.findViewById<TextView>(R.id.tv_property_price).text = "${currProperty.price}"
        holder.itemView.findViewById<TextView>(R.id.tv_property_type).text = "${currProperty.propertyType}"
        holder.itemView.findViewById<TextView>(R.id.tv_property_specification).text = "${currProperty.specification.toString()}"
        holder.itemView.findViewById<TextView>(R.id.tv_property_address).text = "${currProperty.address}"

    }

}