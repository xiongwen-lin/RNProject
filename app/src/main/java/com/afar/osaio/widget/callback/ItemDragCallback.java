package com.afar.osaio.widget.callback;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

/**
 * ItemDragCallback
 *
 * @author Administrator
 * @date 2019/4/10
 */
abstract public class ItemDragCallback extends ItemTouchHelper.Callback {

    private boolean isLongPressDragEnable = false;
    private int mSwipeFlag = 0;
    private int mDragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(mDragFlag, mSwipeFlag);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

        onItemMove(recyclerView, viewHolder, target);
        return true;
    }

    /**
     * 左右拖动的回调
     * @param viewHolder
     * @param direction
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
    }

    @Override
    public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
        return true;
    }

    /**
     * item selected
     * @param viewHolder
     * @param actionState
     */
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
    }

    /**
     * item dismiss
     * @param recyclerView
     * @param viewHolder
     */
    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return isLongPressDragEnable;
    }

    public void setLongPressDragEnable(boolean enable) {
        isLongPressDragEnable = enable;
    }

    /**
     * 如果也监控左右方向的话,swipFlag=ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT
     * @param flag
     * @return
     */
    public void setSwipeFlag(int flag) {
        mSwipeFlag = flag;
    }

    public void setDragFlag(int flag) {
        mDragFlag = flag;
    }

    /**
     * change item and data
     * @param recyclerView
     * @param viewHolder
     * @param target
     */
    abstract public void onItemMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target);
}
