package com.afar.osaio.smart.electrician.smartScene.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceDpAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> dpKey = new ArrayList<>();
    private List<String> dpValue = new ArrayList<>();
    private DpItemListener mListener;

    private int mSelect = -1;

    public void setData(List<String> key, List<String> value) {
        dpKey.clear();
        dpValue.clear();
        dpKey.addAll(key);
        dpValue.addAll(value);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_scene_device_dp, viewGroup, false);
        return new DeviceDpViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof DeviceDpViewHolder) {
            final DeviceDpViewHolder holder = (DeviceDpViewHolder) viewHolder;

            holder.tvDp.setText(dpValue.get(i));

            if (mSelect == i) {
                holder.ivCheckBox.setSelected(true);
            } else {
                holder.ivCheckBox.setSelected(false);
            }

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(i);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return dpValue.size();
    }

    public static class DeviceDpViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvDp)
        TextView tvDp;
        @BindView(R.id.ivCheckBox)
        ImageView ivCheckBox;
        View container;

        public DeviceDpViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }

    public void setListener(DpItemListener listener) {
        mListener = listener;
    }

    public interface DpItemListener {
        void onItemClick(int position);

        void onGetSelectValue(String value);
    }

    public void changeSelected(int position) {
        if (position != mSelect) {
            mSelect = position;
            notifyDataSetChanged();
        }
    }

    public void setSelected(String value) {
        for (int i = 0; i < dpKey.size(); i++) {
            if (dpKey.get(i).equals(value)) {
                changeSelected(i);
                if (mListener != null) {
                    mListener.onGetSelectValue(getSelectedValue(i));
                }
            }
        }
    }

    public String getSelectedKey(int position) {
        return dpKey.get(position);
    }

    public String getSelectedValue(int position) {
        return dpValue.get(position);
    }
}
