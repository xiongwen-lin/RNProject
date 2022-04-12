package com.afar.osaio.smart.electrician.widget;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.bean.ConnectModePopMenuItem;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConnectModePopupMenuAdapter extends RecyclerView.Adapter<ConnectModePopupMenuAdapter.RelativePopupMenuViewHolder> {

    List<ConnectModePopMenuItem> mItems = new ArrayList<>();
    ConnectModePopupWindows.OnMenuItemClickListener mListener;
    private   int  modePosition = 0 ;//选中高亮那行
    @Override
    public RelativePopupMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_popup_connect_item, parent, false);
        return new RelativePopupMenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RelativePopupMenuViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final ConnectModePopMenuItem menuItem = mItems.get(position);
        holder.tvPopupMenuItemTitle.setText(menuItem.getTitle());
        if (modePosition == position){
            holder.tvPopupMenuItemTitle.setTextColor(holder.tvPopupMenuItemTitle.getResources().getColor(R.color.theme_green_subtext_color));
        }else{
            holder.tvPopupMenuItemTitle.setTextColor(holder.tvPopupMenuItemTitle.getResources().getColor(R.color.theme_text_color));
        }
        holder.vPopupMenuItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modePosition = position;
                notifyDataSetChanged();
                if (mListener != null) {
                    mListener.onMenuItemClick(menuItem.getId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.safeFor(mItems).size();
    }

    public void setItems(List<ConnectModePopMenuItem> items) {
        if (mItems == null) {
            mItems = new ArrayList<>();
        }

        mItems.clear();
        mItems.addAll(CollectionUtil.safeFor(items));
        notifyDataSetChanged();
    }

    public void setListener(ConnectModePopupWindows.OnMenuItemClickListener listener) {
        mListener = listener;
    }

    public class RelativePopupMenuViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.vPopupMenuItem)
        View vPopupMenuItem;
        @BindView(R.id.tvPopupMenuItemTitle)
        TextView tvPopupMenuItemTitle;

        public RelativePopupMenuViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}

