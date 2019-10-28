package com.slightlynonprofessionalprogrammer.foodassistant.data

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.slightlynonprofessionalprogrammer.foodassistant.R
import com.squareup.picasso.Picasso
import java.lang.NullPointerException
import java.time.Instant
import java.util.*

class ProductAdapter(context:Context, productList:List<Product>): RecyclerView.Adapter<ProductAdapter.MyViewHolder>() {
    private val context: Context
    private val productList: List<Product>
    private val itemCount: Int

    override fun getItemCount(): Int {
        return productList.size
    }

    inner class MyViewHolder(view:View):RecyclerView.ViewHolder(view) {
        var productName: TextView
        var additionalInfo: TextView
        var thumbnail:ImageView
        var viewBackground:RelativeLayout
        var viewForeground:androidx.constraintlayout.widget.ConstraintLayout
        init{
            productName = view.findViewById(R.id.product_name_product_bar)
            additionalInfo = view.findViewById(R.id.additional_info_add_product)
            thumbnail = view.findViewById(R.id.product_img_product_bar)
            viewBackground = view.findViewById(R.id.view_background)
            viewForeground = view.findViewById(R.id.view_foreground)
        }
    }

    init{
        this.context = context
        this.productList = productList
        this.itemCount = 0
    }

    override fun onCreateViewHolder(parent:ViewGroup, viewType:Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.product_list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position:Int) {
        val item = productList.get(position)

        if (item.imageUrl != "") Picasso.get().load(item.imageUrl).resize(
            300,
            300
        ).into(holder.thumbnail)
        else holder.thumbnail.visibility = View.GONE

        if (item.productName.length > 50) holder.productName.text =
            item.productName.take(47) + "..."
        else holder.productName.text = item.productName

        if (item.expiryDate != "") {
            holder.additionalInfo.visibility = View.VISIBLE
//            Log.d("Product Adapter", "${Calendar.Builder.(Date.parse(item.expiryDate))}, ${Date(Date.parse(item.expiryDate)).getTime()}, ${Calendar.getInstance().time}, ${Date.parse(Calendar.getInstance().time.toString())}")
            var remainingDays: Long? = null
                try {
                remainingDays = DateParser.daysBetweenCurrent(item.expiryDate)
                    Log.d("ProductAdapter", "Remaining days: $remainingDays")
                when (remainingDays) {
                    in 0..1 -> holder.additionalInfo.text =
                        item.amount + "Expires in: 1 day"
                    in 2..14 -> holder.additionalInfo.text =
                        item.amount + "Expires in ${remainingDays} days"
                    in 15..28 -> holder.additionalInfo.text =
                        item.amount + "Expires in ${remainingDays / 7} weeks"
                    in 29..55 -> holder.additionalInfo.text =
                        item.amount + "Expires in 1 month"
                    in 56..365 -> holder.additionalInfo.text =
                        item.amount + "Expires in ${remainingDays / 28} months"
                    in 366..Long.MAX_VALUE -> holder.additionalInfo.text =
                        item.amount + "Expires in ${remainingDays / 365} years"
                    in -1..0 -> holder.additionalInfo.text =
                        item.amount + "Expired  1 day ago"
                    in -14..-2 -> holder.additionalInfo.text =
                        item.amount + "Expired ${-remainingDays} days ago"
                    in -Long.MAX_VALUE..-15 -> holder.additionalInfo.text =
                        item.amount + "Expired ${-remainingDays / 7} weeks ago"
                    else -> holder.additionalInfo.visibility = View.GONE
                }
            }catch(e: NullPointerException) {
                   holder.additionalInfo.visibility = View.GONE
                }

        } else holder.additionalInfo.visibility = View.GONE

    }

    fun removeItem(position:Int) {
        productList.drop(position)
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position)
    }
}