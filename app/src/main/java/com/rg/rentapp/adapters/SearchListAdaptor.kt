package com.rg.rentapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rg.rentapp.R
import com.rg.rentapp.classes.Rental

class SearchListAdaptor (
    var propertyList:MutableList<Rental>,
    val addToShortlistFunction:(Int) -> Unit
) : RecyclerView.Adapter<SearchListAdaptor.SearchListViewHolder>() {
    inner class SearchListViewHolder(itemView: View) : RecyclerView.ViewHolder (itemView)  {
        init {
            var btnAddToShortlist = itemView.findViewById<Button>(R.id.btn_add_to_shortlist)
            btnAddToShortlist.setOnClickListener { addToShortlistFunction(adapterPosition) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchListViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_layout_search, parent, false)
        return SearchListViewHolder(view)

    }

    override fun getItemCount(): Int {
        return propertyList.size
    }

    override fun onBindViewHolder(holder: SearchListViewHolder, position: Int) {
        val currProperty: Rental = propertyList.get(position)

        val tvPropertyPrice = holder.itemView.findViewById<TextView>(R.id.tv_property_price)
        tvPropertyPrice.text = "${currProperty.price}"

        val tvPropertyType = holder.itemView.findViewById<TextView>(R.id.tv_property_type)
        tvPropertyType.text = "${currProperty.propertyType}"

//        val tvPropertySpecification = holder.itemView.findViewById<TextView>(R.id.tv_property_specification)
//        tvPropertySpecification.text = "${currProperty.specification}"

        val tvPropertyAddress = holder.itemView.findViewById<TextView>(R.id.tv_property_address)
        tvPropertyAddress.text = "${currProperty.address}"

    }
}