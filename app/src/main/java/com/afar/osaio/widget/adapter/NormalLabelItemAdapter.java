package com.afar.osaio.widget.adapter;

import android.view.View;

import com.afar.osaio.R;
import com.afar.osaio.bean.LabelItemBean;

public class NormalLabelItemAdapter extends AbstractLabelItemAdapter {

    @Override
    public int getLayoutId() {
        return R.layout.item_label_text;
    }

    @Override
    public void bindViewHolder(LabelItemVH holder, LabelItemBean data) {
        if (holder == null) {
            return;
        }
        if (data == null) {
            holder.container.setOnClickListener(null);
            return;
        }
        holder.ivLabelItemIcon.setImageResource(data.getIconRes());
        holder.tvLabelItemTitle.setText(data.getTitle());
        holder.container.setBackgroundResource(R.drawable.btn_press_bg_gray_state_list);
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(data.getId(), data.getParam());
                }
            }
        });
    }

}
