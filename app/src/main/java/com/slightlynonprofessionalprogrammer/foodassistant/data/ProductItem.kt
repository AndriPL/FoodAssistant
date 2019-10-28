package com.slightlynonprofessionalprogrammer.foodassistant.data

import android.view.View
import com.slightlynonprofessionalprogrammer.foodassistant.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.product_bar.view.*

class ProductItem (val product: Product): Item<GroupieViewHolder>()
{
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        if(product.imageUrl != "")
        Picasso.get().load(product.imageUrl).resize(300, 300).into(viewHolder.itemView.product_img_product_bar)
        else
        viewHolder.itemView.product_img_product_bar.visibility = View.GONE
        viewHolder.itemView.product_name_product_bar.text = product.productName.take(47) + "..."
        if(product.expiryDate != "")
            viewHolder.itemView.additional_info_add_product.text = product.amount + "Expires in: ${product.expiryDate}"
        else
            viewHolder.itemView.additional_info_add_product.text = product.amount
    }

    override fun getLayout(): Int {
        return R.layout.product_bar
    }
}