package com.afar.osaio.smart.setting.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.FileSettingConfigureParam;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.setting.adapter.listener.FileSettingConfigureListener;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.sdk.device.bean.NooieMediaMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileSettingConfigureAdapter extends BaseAdapter<FileSettingConfigureParam, FileSettingConfigureListener, FileSettingConfigureAdapter.FileSettingConfigureVH> {

    @Override
    public FileSettingConfigureVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FileSettingConfigureVH(createVHView(R.layout.item_file_setting_configure, parent));
    }

    @Override
    public void onBindViewHolder(FileSettingConfigureVH holder, int position) {
        FileSettingConfigureParam configure = getDataByPosition(position);
        if (holder == null || configure == null) {
            return;
        }
        if (configure.getType() == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_MODE) {
            holder.tvFileSettingConfigureValue.setText(NooieDeviceHelper.getFileSettingModeText(NooieApplication.mCtx, configure.getMode()));
        } else if (configure.getType() == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_SNAP_NUMBER) {
            holder.tvFileSettingConfigureValue.setText(String.valueOf(configure.getSnapNumber()));
        } else if (configure.getType() == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_RECORDING_TIME) {
            holder.tvFileSettingConfigureValue.setText(new StringBuilder().append(configure.getRecordingTime()).append("s"));
        } else {
            holder.tvFileSettingConfigureValue.setText(new String());
        }
        holder.ivFileSettingConfigureSelected.setVisibility(configure.isSelected() ? View.VISIBLE : View.GONE);
        holder.vFileSettingConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (configure != null) {
                    NooieMediaMode mediaMode = new NooieMediaMode(configure.getMode(), configure.getSnapNumber(), configure.getRecordingTime());
                    setMediaMode(configure.getType(), mediaMode);
                }
                if (mListener != null) {
                    mListener.onItemClick(configure);
                }
            }
        });
    }

    public void setMediaMode(int configureType, NooieMediaMode mediaMode) {
        if (mediaMode == null || CollectionUtil.isEmpty(mDatas)) {
            return;
        }
        for (int i = 0; i < CollectionUtil.size(mDatas); i++) {
            if (mDatas.get(i) == null) {
                continue;
            }
            if (configureType == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_MODE) {
                if (mDatas.get(i).getMode() == mediaMode.mode) {
                    mDatas.get(i).setSelected(true);
                } else {
                    mDatas.get(i).setSelected(false);
                }
            } else if (configureType == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_SNAP_NUMBER) {
                if (mDatas.get(i).getSnapNumber() == mediaMode.picNum) {
                    mDatas.get(i).setSelected(true);
                } else {
                    mDatas.get(i).setSelected(false);
                }
            } else if (configureType == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_RECORDING_TIME) {
                if (mDatas.get(i).getRecordingTime() == mediaMode.vidDur) {
                    mDatas.get(i).setSelected(true);
                } else {
                    mDatas.get(i).setSelected(false);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class FileSettingConfigureVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vFileSettingConfigure)
        View vFileSettingConfigure;
        @BindView(R.id.tvFileSettingConfigureValue)
        TextView tvFileSettingConfigureValue;
        @BindView(R.id.ivFileSettingConfigureSelected)
        ImageView ivFileSettingConfigureSelected;

        public FileSettingConfigureVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
