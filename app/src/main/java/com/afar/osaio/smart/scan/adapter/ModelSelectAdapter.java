package com.afar.osaio.smart.scan.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.base.NooieApplication;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.bean.SelectDeviceBean;
import com.afar.osaio.smart.scan.adapter.listener.ModelSelectListener;
import com.afar.osaio.widget.FixTextIconView;
import com.afar.osaio.widget.helper.ResHelper;
import com.nooie.common.utils.collection.CollectionUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ModelSelectAdapter extends BaseAdapter<SelectDeviceBean, ModelSelectListener, ModelSelectAdapter.ModelSelectVH> {

    @Override
    public ModelSelectVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_model_select, parent, false);
        return new ModelSelectVH(view);
    }

    @Override
    public void onBindViewHolder(ModelSelectVH holder, int position) {
        SelectDeviceBean selectDevice = mDatas.get(position);
        holder.ftivModel.setTextIcon(ResHelper.getInstance().getDeviceSmallIconByType(IpcType.getIpcType(selectDevice.getType()).getType()));
        holder.ftivModel.setTextIconBg(0);
        holder.ftivModel.setTextTitle(selectDevice.getName());
        holder.ftivModel.setTextTitleColor(R.color.gray_616161);
        holder.vModelSelectContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = holder.getAdapterPosition();
                //updateSelectedItem(id);
                if (mListener != null) {
                    mListener.onItemClick(getItemByPosition(id));
                }
            }
        });
    }

    public void updateSelectedItem(int position) {
        if (position < 0) {
            return;
        }
        if (mDatas != null && position < mDatas.size()) {
            for (int i = 0; i < mDatas.size(); i++) {
                if (i == position && mDatas.get(i) != null) {
                    mDatas.get(i).setSelected(true);
                } else if (mDatas.get(i) != null) {
                    mDatas.get(i).setSelected(false);
                }
            }
            notifyDataSetChanged();
        }
    }

    public SelectDeviceBean getItemByPosition(int position) {
        if (CollectionUtil.isEmpty(mDatas) || position >= mDatas.size()) {
            return null;
        }
        return mDatas.get(position);
    }

    public static class ModelSelectVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vModelSelectContainer)
        View vModelSelectContainer;
        @BindView(R.id.ftivModel)
        FixTextIconView ftivModel;

        public ModelSelectVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
