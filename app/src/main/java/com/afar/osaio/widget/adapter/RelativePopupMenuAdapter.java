package com.afar.osaio.widget.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nooie.common.utils.collection.CollectionUtil;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.widget.RelativePopupMenu;
import com.afar.osaio.widget.bean.RelativePopMenuItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RelativePopupMenuAdapter extends RecyclerView.Adapter<RelativePopupMenuAdapter.RelativePopupMenuViewHolder> {

    List<RelativePopMenuItem> mItems = new ArrayList<>();
    RelativePopupMenu.OnMenuItemClickListener mListener;

    @Override
    public RelativePopupMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_popup_menu_item, parent, false);
        return new RelativePopupMenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RelativePopupMenuViewHolder holder, final int position) {
        final RelativePopMenuItem menuItem = mItems.get(position);
        holder.ivPopupMenuItemIcon.setImageResource(menuItem.getIcon());
        holder.tvPopupMenuItemTitle.setText(menuItem.getTitle());
        if (mItems.size() > 1){
            holder.vLine.setVisibility(View.VISIBLE);
        }else{
            holder.vLine.setVisibility(View.INVISIBLE);
        }
        holder.vPopupMenuItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    public void setItems(List<RelativePopMenuItem> items) {
        if (mItems == null) {
            mItems = new ArrayList<>();
        }

        mItems.clear();
        mItems.addAll(CollectionUtil.safeFor(items));
        notifyDataSetChanged();
    }

    public void setListener(RelativePopupMenu.OnMenuItemClickListener listener) {
        mListener = listener;
    }

    public class RelativePopupMenuViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.vPopupMenuItem)
        View vPopupMenuItem;
        @BindView(R.id.ivPopupMenuItemIcon)
        ImageView ivPopupMenuItemIcon;
        @BindView(R.id.tvPopupMenuItemTitle)
        TextView tvPopupMenuItemTitle;
        @BindView(R.id.vLine)
        View vLine;

        public RelativePopupMenuViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
