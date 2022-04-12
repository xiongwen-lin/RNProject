package com.afar.osaio.smart.setting.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.FlashLightMode;
import com.afar.osaio.smart.device.helper.CopyWritingHelper;
import com.afar.osaio.smart.setting.adapter.listener.FlashLightModeListener;
import com.nooie.common.utils.collection.CollectionUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FlashLightModeAdapter extends BaseAdapter<FlashLightMode, FlashLightModeListener, FlashLightModeAdapter.FlashLightModeViewHolder> {

    @Override
    public FlashLightModeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FlashLightModeViewHolder(createVHView(R.layout.layout_label_select_tag_item, parent));
    }

    @Override
    public void onBindViewHolder(FlashLightModeViewHolder holder, int position) {

        if (holder == null) {
            return;
        }
        FlashLightMode flashLightMode = getDataByPosition(position);
        if (flashLightMode == null) {
            holder.vLabelSelectContainer.setOnClickListener(null);
            return;
        }
        holder.tvLabelSelectTitle.setText(CopyWritingHelper.convertFlashLightModeTitle(NooieApplication.mCtx, flashLightMode.getMode()));
        holder.tvLabelSelectTag.setText(CopyWritingHelper.convertFlashLightModeTag(NooieApplication.mCtx, flashLightMode.getMode()));
        if (flashLightMode.isSelected()) {
            holder.ivLabelSelectTagIcon.setImageResource(R.drawable.ic_options_selected_on_on);
        } else {
            holder.ivLabelSelectTagIcon.setImageResource(R.drawable.ic_options_selected_on_off);
        }
        holder.vLabelSelectContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flashLightMode != null) {
                    updateSelectMode(flashLightMode.getMode());
                }
                if (mListener != null) {
                    mListener.onItemClick(flashLightMode);
                }
            }
        });
    }

    public void updateSelectMode(int mode) {
        if (CollectionUtil.isEmpty(mDatas)) {
            return;
        }
        for (int i = 0; i < CollectionUtil.size(mDatas); i++) {
            if (getDataByPosition(i) != null && getDataByPosition(i).getMode() == mode) {
                getDataByPosition(i).setSelected(true);
            } else if (getDataByPosition(i) != null){
                getDataByPosition(i).setSelected(false);
            }
        }
        notifyDataSetChanged();
    }

    public static final class FlashLightModeViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.vLabelSelectContainer)
        View vLabelSelectContainer;
        @BindView(R.id.tvLabelSelectTitle)
        TextView tvLabelSelectTitle;
        @BindView(R.id.tvLabelSelectTag)
        TextView tvLabelSelectTag;
        @BindView(R.id.ivLabelSelectTagIcon)
        ImageView ivLabelSelectTagIcon;

        public FlashLightModeViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
