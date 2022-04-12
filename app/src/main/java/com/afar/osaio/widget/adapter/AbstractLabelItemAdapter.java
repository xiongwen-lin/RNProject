package com.afar.osaio.widget.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.bean.LabelItemBean;
import com.afar.osaio.widget.listener.LabelItemListener;

import butterknife.BindView;
import butterknife.ButterKnife;

abstract public class AbstractLabelItemAdapter extends BaseAdapter<LabelItemBean, LabelItemListener, AbstractLabelItemAdapter.LabelItemVH> {

    @Override
    public LabelItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LabelItemVH(createVHView(getLayoutId(), parent));
    }

    @Override
    public void onBindViewHolder(LabelItemVH holder, int position) {
        if (holder == null) {
            return;
        }
        LabelItemBean data = getDataByPosition(position);
        bindViewHolder(holder, data);
    }

    abstract public int getLayoutId();

    abstract public void bindViewHolder(LabelItemVH holder, LabelItemBean data);

    public class LabelItemVH extends RecyclerView.ViewHolder {

        @BindView(R.id.containerLabelItem)
        View container;
        @BindView(R.id.ivLabelItemIcon)
        ImageView ivLabelItemIcon;
        @BindView(R.id.tvLabelItemTitle)
        TextView tvLabelItemTitle;
        @BindView(R.id.ivLabelItemArrow)
        ImageView ivLabelItemArrow;
        @BindView(R.id.vLabelBottomLine)
        View vLabelBottomLine;

        public LabelItemVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
