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

public class WeatherFuncAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> dpKey = new ArrayList<>();
    private List<String> dpValue = new ArrayList<>();
    private WeatherFuncItemListener mListener;

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
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_weather_function, viewGroup, false);
        return new WeatherFuncViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof WeatherFuncViewHolder) {
            final WeatherFuncViewHolder holder = (WeatherFuncViewHolder) viewHolder;

            holder.tvWeatherFunc.setText(dpValue.get(i));

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

    public static class WeatherFuncViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvWeatherFunc)
        TextView tvWeatherFunc;
        @BindView(R.id.ivCheckBox)
        ImageView ivCheckBox;
        View container;

        public WeatherFuncViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }

    public void setListener(WeatherFuncItemListener listener) {
        mListener = listener;
    }

    public interface WeatherFuncItemListener {
        void onItemClick(int position);

        void onGetSelectValue(String value);
    }

    public void changeSelected(int position) {
        if (position != mSelect) {
            mSelect = position;
            if (mListener != null) {
                mListener.onGetSelectValue(getSelectedValue(position));
            }
            notifyDataSetChanged();
        }
    }

    public String getSelectedKey(int position) {
        return dpKey.get(position);
    }

    public String getSelectedValue(int position) {
        return dpValue.get(position);
    }

    public List<String> getKeyList(){
        return dpKey;
    }

}
