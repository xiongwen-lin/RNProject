package com.afar.osaio.smart.electrician.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceTitleAdapter extends RecyclerView.Adapter<DeviceTitleAdapter.DeviceTitleViewHolder> {

    private List<String> deviceTitles = new ArrayList<>();
    private DeviceTitleListener mListener;

    private int mSelect = -1;

    @NonNull
    @Override
    public DeviceTitleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_device_title, parent, false);
        return new DeviceTitleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceTitleViewHolder holder, int i) {
        holder.tvDeviceTitle.setText(deviceTitles.get(i));
        if (mSelect == i) {
            holder.container.setSelected(true);
            holder.tvDeviceTitle.setTextColor(NooieApplication.mCtx.getResources().getColor(R.color.theme_green));
        } else {
            holder.container.setSelected(false);
            holder.tvDeviceTitle.setTextColor(NooieApplication.mCtx.getResources().getColor(R.color.theme_text_color));
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSelected(i);
                if (mListener != null) {
                    mListener.onItemClick(i);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceTitles.size();
    }

    public void setData(List<String> titles) {
        deviceTitles.clear();
        deviceTitles.addAll(titles);
        notifyDataSetChanged();
    }

    public static class DeviceTitleViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvDeviceTitle)
        TextView tvDeviceTitle;

        View container;

        public DeviceTitleViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container = itemView;
        }
    }

    public void changeSelected(int position) {
        if (position != mSelect) {
            mSelect = position;
            notifyDataSetChanged();
        }
    }

    public interface DeviceTitleListener {
        void onItemClick(int position);
    }

    public void setListener(DeviceTitleListener deviceTitleListener) {
        this.mListener = deviceTitleListener;
    }
}
