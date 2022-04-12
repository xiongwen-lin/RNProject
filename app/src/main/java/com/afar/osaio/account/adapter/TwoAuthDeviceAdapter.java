package com.afar.osaio.account.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.widget.LabelActionItemView;
import com.nooie.sdk.api.network.base.bean.entity.TwoAuthDevice;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * TwoAuthDeviceAdapter
 *
 * @author Administrator
 * @date 2020/10/12
 */
public class TwoAuthDeviceAdapter extends BaseAdapter<TwoAuthDevice, TwoAuthDeviceListener, TwoAuthDeviceAdapter.TwoAuthDeviceViewHolder> {

    @Override
    public TwoAuthDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TwoAuthDeviceViewHolder(createVHView(R.layout.item_two_auth_device, parent));
    }

    @Override
    public void onBindViewHolder(TwoAuthDeviceViewHolder holder, int position) {
        TwoAuthDevice twoAuthDevice = mDatas.get(position);
        if (twoAuthDevice != null) {
            holder.livTwoAuthDeviceName.setLabelTitle(twoAuthDevice.getPhone_name()).displayArrow(View.VISIBLE);
            holder.vTwoAuthDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(twoAuthDevice);
                    }
                }
            });
        }
    }

    public static final class TwoAuthDeviceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.vTwoAuthDevice)
        View vTwoAuthDevice;
        @BindView(R.id.livTwoAuthDeviceName)
        LabelActionItemView livTwoAuthDeviceName;

        public TwoAuthDeviceViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}