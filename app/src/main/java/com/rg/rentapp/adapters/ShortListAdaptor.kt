package com.rg.rentapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rg.rentapp.R
import com.rg.rentapp.classes.Rental

class ShortListAdaptor(
    var propertyList:MutableList<Rental>,
    val removeFunction: (Int) -> Unit,
    val viewDetailsFunction: (Int) -> Unit
): RecyclerView.Adapter<ShortListAdaptor.ShortListViewHolder>() {
    inner class ShortListViewHolder(itemView: View) : RecyclerView.ViewHolder (itemView)  {
        init {
            var btnRmvRental = itemView.findViewById<Button>(R.id.btn_rmv_rental)
            btnRmvRental.setOnClickListener { removeFunction(adapterPosition) }

            itemView.setOnClickListener{ viewDetailsFunction(adapterPosition)}
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShortListViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_shortlist, parent, false)
        return ShortListViewHolder(view)

    }

    override fun getItemCount(): Int {
        return propertyList.size
    }

    override fun onBindViewHolder(holder: ShortListViewHolder, position: Int) {

        val currProperty:Rental = propertyList.get(position)

        val tvPropertyPrice = holder.itemView.findViewById<TextView>(R.id.tv_property_price)
        tvPropertyPrice.text = "${currProperty.price}"

        val tvPropertyType = holder.itemView.findViewById<TextView>(R.id.tv_property_type)
        tvPropertyType.text = "${currProperty.propertyType}"

        val tvPropertyAddress = holder.itemView.findViewById<TextView>(R.id.tv_property_address)
        tvPropertyAddress.text = "${currProperty.address}"

    }

}