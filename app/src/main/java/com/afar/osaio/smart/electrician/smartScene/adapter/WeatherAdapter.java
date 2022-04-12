package com.afar.osaio.smart.electrician.smartScene.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.tuya.smart.home.sdk.bean.scene.condition.ConditionListBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WeatherAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<ConditionListBean> mConditions = new ArrayList<>();
    private WeatherItemListener mListener;

    public void setData(List<ConditionListBean> conditions) {
        mConditions.clear();
        mConditions.addAll(conditions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_scene_weather, viewGroup, false);
        return new WeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof WeatherViewHolder) {
            final WeatherViewHolder holder = (WeatherViewHolder) viewHolder;
            final ConditionListBean conditionListBean = mConditions.get(i);

            holder.tvWeather.setText(conditionListBean.getName());

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(conditionListBean);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mConditions.size();
    }

    public static class WeatherViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvWeather)
        TextView tvWeather;
        View container;

        public WeatherViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }

    public void setListener(WeatherItemListener listener) {
        mListener = listener;
    }

    public interface WeatherItemListener {
        void onItemClick(ConditionListBean conditionListBean);
    }
}
