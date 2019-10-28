package com.slightlynonprofessionalprogrammer.foodassistant.ui.main

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.slightlynonprofessionalprogrammer.foodassistant.data.ProductAdapter

class RecyclerItemTouchHelper(dragDirs:Int, swipeDirs:Int, listener:RecyclerItemTouchHelperListener):
    ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
    private val listener: RecyclerItemTouchHelperListener
    init{
        this.listener = listener
    }
    override fun onMove(recyclerView:RecyclerView, viewHolder:RecyclerView.ViewHolder, target: RecyclerView.ViewHolder):Boolean {
        return true
    }
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState:Int) {
        if (viewHolder != null)
        {
            val foregroundView = (viewHolder as ProductAdapter.MyViewHolder).viewForeground
            getDefaultUIUtil().onSelected(foregroundView)
        }
    }
    override fun onChildDrawOver(c:Canvas, recyclerView:RecyclerView,
                        viewHolder:RecyclerView.ViewHolder, dX:Float, dY:Float,
                        actionState:Int, isCurrentlyActive:Boolean) {
        val foregroundView = (viewHolder as ProductAdapter.MyViewHolder).viewForeground
        getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
            actionState, isCurrentlyActive)
    }
    override fun clearView(recyclerView:RecyclerView, viewHolder:RecyclerView.ViewHolder) {
        val foregroundView = (viewHolder as ProductAdapter.MyViewHolder).viewForeground
        getDefaultUIUtil().clearView(foregroundView)
    }
    override fun onChildDraw(c:Canvas, recyclerView:RecyclerView,
                    viewHolder:RecyclerView.ViewHolder, dX:Float, dY:Float,
                    actionState:Int, isCurrentlyActive:Boolean) {
        val foregroundView = (viewHolder as ProductAdapter.MyViewHolder).viewForeground
        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
            actionState, isCurrentlyActive)
    }
    override fun onSwiped(viewHolder:RecyclerView.ViewHolder, direction:Int) {
        listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition())
    }
    override fun convertToAbsoluteDirection(flags:Int, layoutDirection:Int):Int {
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }
    interface RecyclerItemTouchHelperListener {
        fun onSwiped(viewHolder:RecyclerView.ViewHolder, direction:Int, position:Int)
    }
}