package com.afar.osaio.smart.electrician.smartScene.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.util.ConstantValue;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.scene.SceneCondition;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConditionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SceneCondition> mConditions = new ArrayList<>();
    private ConditionItemListener mListener;

    public void setData(List<SceneCondition> conditions) {
        mConditions.clear();
        if (CollectionUtil.isNotEmpty(conditions)) {
            mConditions.addAll(conditions);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_scene_condition_task, viewGroup, false);
        return new ConditionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof ConditionViewHolder) {
            final ConditionViewHolder holder = (ConditionViewHolder) viewHolder;

            if (CollectionUtil.isEmpty(mConditions)) {
                holder.ivIcon.setImageResource(R.drawable.launch);
                holder.tvName.setVisibility(View.GONE);
                holder.tvFunc.setVisibility(View.GONE);
                holder.tvLaunch.setVisibility(View.VISIBLE);
            } else {
                final SceneCondition sceneCondition = mConditions.get(position);

                if (sceneCondition != null) {
                    if (sceneCondition.getEntityType() == 6) {
                        if (CollectionUtil.isNotEmpty(sceneCondition.getExpr())) {
                            for (Object o : sceneCondition.getExpr()) {
                                String linkedMap = new Gson().toJson(o);
                                LinkedTreeMap linkedTreeMap = new Gson().fromJson(linkedMap, new TypeToken<LinkedTreeMap>() {
                                }.getType());
                                Iterator it = linkedTreeMap.keySet().iterator();
                                while (it.hasNext()) {
                                    String key = (String) it.next();
                                    String value = (String) linkedTreeMap.get(key);
                                    if (key.equals("time")) {
                                        holder.tvName.setText("Schedule:" + linkedTreeMap.get(key));
                                    } else if (key.equals("loops")) {
                                        String loops = (String) linkedTreeMap.get(key);
                                        StringBuffer buffer = new StringBuffer();
                                        if (!TextUtils.isEmpty(loops) && loops.length() == 7) {
                                            char[] chars = loops.toCharArray();
                                            for (int i = 0; i < chars.length; i++) {
                                                if (i == 0 && chars[i] == '1') {
                                                    buffer.append(NooieApplication.mCtx.getResources().getString(R.string.sun) + " ");
                                                }

                                                if (i == 1 && chars[i] == '1') {
                                                    buffer.append(NooieApplication.mCtx.getResources().getString(R.string.mon) + " ");
                                                }

                                                if (i == 2 && chars[i] == '1') {
                                                    buffer.append(NooieApplication.mCtx.getResources().getString(R.string.tues) + " ");
                                                }

                                                if (i == 3 && chars[i] == '1') {
                                                    buffer.append(NooieApplication.mCtx.getResources().getString(R.string.wed) + " ");
                                                }

                                                if (i == 4 && chars[i] == '1') {
                                                    buffer.append(NooieApplication.mCtx.getResources().getString(R.string.thurs) + " ");
                                                }

                                                if (i == 5 && chars[i] == '1') {
                                                    buffer.append(NooieApplication.mCtx.getResources().getString(R.string.fri) + " ");
                                                }

                                                if (i == 6 && chars[i] == '1') {
                                                    buffer.append(NooieApplication.mCtx.getResources().getString(R.string.sat));
                                                }
                                            }
                                        }
                                        NooieLog.e("loops " + buffer);
                                        if (TextUtils.isEmpty(buffer)) {
                                            holder.tvFunc.setText(R.string.once);
                                        } else {
                                            holder.tvFunc.setText(buffer);
                                        }
                                    }
                                    NooieLog.e("key=" + key + " value=" + value);
                                }
                            }
                        }
                        holder.ivIcon.setImageResource(R.drawable.scene_schedule);
                    } else if (sceneCondition.getEntityType() == 3) {
                        holder.tvName.setText(sceneCondition.getExprDisplay());
                        holder.tvFunc.setText(sceneCondition.getEntityName());
                        holder.ivIcon.setImageResource(R.drawable.weather_change);
                    } else if (sceneCondition.getEntityType() == 1) {

                        holder.tvName.setText(sceneCondition.getEntityName());
                        holder.tvFunc.setText(sceneCondition.getExprDisplay());

                        if (!TextUtils.isEmpty(sceneCondition.getEntityId())) {
                            DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(sceneCondition.getEntityId());
                            DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(ConstantValue.DURATION_MILLIS).setCrossFadeEnabled(true).build();
                            Glide.with(NooieApplication.mCtx)
                                    .load(deviceBean.getIconUrl())
                                    .apply(new RequestOptions().placeholder(R.drawable.home_plug_icon).error(R.drawable.home_plug_icon))
                                    .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                                    .into(holder.ivIcon);
                        }
                    }
                }

                holder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onItemClick(position, sceneCondition);
                        }
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.isEmpty(mConditions) ? 1 : mConditions.size();
    }

    public static class ConditionViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.tvFunc)
        TextView tvFunc;
        @BindView(R.id.tvLaunch)
        TextView tvLaunch;
        @BindView(R.id.ivIcon)
        ImageView ivIcon;
        View container;

        public ConditionViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }

    public void setListener(ConditionItemListener listener) {
        mListener = listener;
    }

    public interface ConditionItemListener {
        void onItemClick(int i, SceneCondition sceneCondition);
    }

}
