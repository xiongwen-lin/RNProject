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
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.scene.SceneTask;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SceneTask> mTasks = new ArrayList<>();
    private TaskItemListener mListener;

    public void setData(List<SceneTask> tasks) {
        mTasks.clear();
        mTasks.addAll(tasks);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_scene_condition_task, viewGroup, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof TaskViewHolder) {
            final TaskViewHolder holder = (TaskViewHolder) viewHolder;

            final SceneTask sceneTask = mTasks.get(i);
            if (sceneTask != null && !TextUtils.isEmpty(sceneTask.getActionExecutor())) {
                if (sceneTask.getActionExecutor().equals("delay") || sceneTask.getActionExecutor().equals("dealy")) {
                    holder.ivIcon.setImageResource(R.drawable.delay);
                    holder.tvName.setText(R.string.delay);
                    holder.tvOffline.setVisibility(View.INVISIBLE);
                    if (sceneTask.getExecutorProperty() != null) {
                        if (Integer.valueOf(sceneTask.getExecutorProperty().get("seconds").toString()) == 0) {
                            holder.tvFunc.setText(sceneTask.getExecutorProperty().get("minutes") + "min");
                        } else if (Integer.valueOf(sceneTask.getExecutorProperty().get("minutes").toString()) == 0) {
                            holder.tvFunc.setText(sceneTask.getExecutorProperty().get("seconds") + "s");
                        } else {
                            holder.tvFunc.setText(sceneTask.getExecutorProperty().get("minutes") + "min" + sceneTask.getExecutorProperty().get("seconds") + "s");
                        }
                    }
                } else if (sceneTask.getActionExecutor().contains("rule")) {
                    holder.tvName.setText(sceneTask.getEntityName());

                    if (sceneTask.getActionExecutor().equals("ruleTrigger")) {
                        holder.tvFunc.setText(R.string.tap_start);
                        if (TextUtils.isEmpty(sceneTask.getEntityName())) {
                            holder.tvName.setText("Tap to Run");
                        }
                    } else if (sceneTask.getActionExecutor().equals("ruleDisable")) {
                        if (TextUtils.isEmpty(sceneTask.getEntityName())) {
                            holder.tvName.setText("Automation");
                        }
                        holder.tvFunc.setText(R.string.auto_disable);
                    } else if (sceneTask.getActionExecutor().equals("ruleEnable")) {
                        if (TextUtils.isEmpty(sceneTask.getEntityName())) {
                            holder.tvName.setText("Automation");
                        }
                        holder.tvFunc.setText(R.string.auto_enable);
                    }
                    holder.tvOffline.setVisibility(View.INVISIBLE);
                    holder.ivIcon.setImageResource(R.drawable.select_smart);
                } else if (sceneTask.getActionExecutor().equals("dpIssue")) {
                    if (!TextUtils.isEmpty(sceneTask.getEntityId())) {
                        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(sceneTask.getEntityId());
                        holder.tvName.setText(deviceBean.getName());
                        if (sceneTask.getActionDisplayNew() != null) {
                            List<String> list = new ArrayList<>();
                            list.clear();
                            for (Map.Entry<String, List<String>> entry : sceneTask.getActionDisplayNew().entrySet()) {
                                NooieLog.e("taskAdapter entry  " + entry.getKey() + " value " + entry.getValue());
                                list.addAll(entry.getValue());
                                for (String value : entry.getValue()) {
                                    NooieLog.e("taskAdapter value " + value);
                                }
                            }
                            holder.tvFunc.setText(list.get(0) + ":" + list.get(1));
                        } else {
                            holder.tvFunc.setText(sceneTask.getEntityName());
                        }
                       /* if (!deviceBean.getIsOnline()) {
                            holder.tvOffline.setVisibility(View.VISIBLE);
                        }*/
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
                        mListener.onItemClick(i, sceneTask);
                    }
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.tvFunc)
        TextView tvFunc;
        @BindView(R.id.ivIcon)
        ImageView ivIcon;
        @BindView(R.id.tvOffline)
        TextView tvOffline;
        View container;

        public TaskViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }

    public void setListener(TaskItemListener listener) {
        mListener = listener;
    }

    public interface TaskItemListener {
        void onItemClick(int i, SceneTask sceneTask);
    }
}

