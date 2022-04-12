package com.afar.osaio.smart.electrician.smartScene.adapter;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.StateListDrawable;
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
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.suke.widget.SwitchButton;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SceneAutomationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SceneBean> scenes = new ArrayList<>();
    private AutomationItemListener mListener;

    public void setData(List<SceneBean> sceneBeanList) {
        scenes.clear();
        scenes.addAll(sceneBeanList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_teckin_scene_automation, viewGroup, false);
        return new AutomationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof AutomationViewHolder) {
            final AutomationViewHolder holder = (AutomationViewHolder) viewHolder;
            final SceneBean sceneBean = scenes.get(i);

            if (CollectionUtil.isNotEmpty(sceneBean.getConditions())) {
                if (sceneBean.getConditions().get(0) != null) {
                    if (sceneBean.getConditions().get(0).getEntityType() == 6) {
                        holder.ivAuto1.setImageResource(R.drawable.scene_schedule);
                    } else if (sceneBean.getConditions().get(0).getEntityType() == 3) {
                        holder.ivAuto1.setImageResource(R.drawable.weather_change);
                    } else if (sceneBean.getConditions().get(0).getEntityType() == 1) {
                        if (!TextUtils.isEmpty(sceneBean.getConditions().get(0).getEntityId())) {
                            DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(sceneBean.getConditions().get(0).getEntityId());
                            DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(ConstantValue.DURATION_MILLIS).setCrossFadeEnabled(true).build();
                            if (deviceBean.getIconUrl() != null) {
                                Glide.with(NooieApplication.mCtx)
                                        .load(deviceBean.getIconUrl())
                                        .apply(new RequestOptions().placeholder(R.drawable.home_plug_icon).error(R.drawable.home_plug_icon))
                                        .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                                        .into(holder.ivAuto1);
                            }
                        }
                    }
                }
            }

            if (CollectionUtil.isNotEmpty(sceneBean.getActions())) {
                if (sceneBean.getActions().get(0) != null && !TextUtils.isEmpty(sceneBean.getActions().get(0).getActionExecutor())) {
                    if (sceneBean.getActions().get(0).getActionExecutor().equals("delay") || sceneBean.getActions().get(0).getActionExecutor().equals("dealy")) {
                        holder.ivAuto4.setImageResource(R.drawable.delay);
                    } else if (sceneBean.getActions().get(0).getActionExecutor().contains("rule")) {
                        holder.ivAuto4.setImageResource(R.drawable.select_smart);
                    } else {
                        if (!TextUtils.isEmpty(sceneBean.getActions().get(0).getEntityId())) {
                            DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(sceneBean.getActions().get(0).getEntityId());
                            DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(ConstantValue.DURATION_MILLIS).setCrossFadeEnabled(true).build();
                            if (deviceBean.getIconUrl() != null) {
                                Glide.with(NooieApplication.mCtx)
                                        .load(deviceBean.getIconUrl())
                                        .apply(new RequestOptions().placeholder(R.drawable.home_plug_icon).error(R.drawable.home_plug_icon))
                                        .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                                        .into(holder.ivAuto4);
                            }
                        }
                    }
                }
            }

            if (sceneBean != null) {
                holder.tvName.setText(sceneBean.getName());
                if (sceneBean.getActions() != null) {
                    holder.tvTask.setText(sceneBean.getActions().size() + " tasks");
                } else {
                    holder.tvTask.setText("0 tasks");
                }

                if (sceneBean.getDisplayColor() != null) {
                    String displayColor;
                    if (sceneBean.getDisplayColor().contains("#")) {
                        displayColor = sceneBean.getDisplayColor();
                    } else {
                        displayColor = "#" + sceneBean.getDisplayColor();
                    }
                    NooieLog.e("---------Automation displayColor  " + displayColor);
                    StateListDrawable background = (StateListDrawable) holder.clAutomation.getBackground();
                    background.setColorFilter(new PorterDuffColorFilter(Color.parseColor(displayColor), PorterDuff.Mode.SRC_ATOP));
                }

                boolean isEnable = sceneBean.isEnabled();
                if (holder.btnSceneSwitch.isChecked() != isEnable) {
                    holder.btnSceneSwitch.toggleNoCallback();
                }
            }

            holder.btnSceneSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                    if (mListener != null) {
                        NooieLog.e("----------btnSceneSwitch  " + isChecked);
                        mListener.onSwitchClick(sceneBean, isChecked);
                    }
                }
            });

            holder.ivModify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onModifyClick(i, sceneBean);
                    }
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return scenes.size();
    }

    public static class AutomationViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.tvTask)
        TextView tvTask;
        @BindView(R.id.ivAuto1)
        ImageView ivAuto1;
        @BindView(R.id.ivAuto4)
        ImageView ivAuto4;
        @BindView(R.id.ivModify)
        ImageView ivModify;
        @BindView(R.id.btnSceneSwitch)
        SwitchButton btnSceneSwitch;
        @BindView(R.id.clAutomation)
        View clAutomation;
        View container;

        public AutomationViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }

    public void setListener(AutomationItemListener listener) {
        mListener = listener;
    }

    public interface AutomationItemListener {
        void onModifyClick(int position, SceneBean sceneBean);

        void onSwitchClick(SceneBean sceneBean, boolean isChecked);
    }

}
