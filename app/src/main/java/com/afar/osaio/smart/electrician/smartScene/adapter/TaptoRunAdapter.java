package com.afar.osaio.smart.electrician.smartScene.adapter;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TaptoRunAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SceneBean> scenes = new ArrayList<>();
    private LaunchItemListener mListener;

    public void setData(List<SceneBean> sceneBeanList) {
        scenes.clear();
        scenes.addAll(sceneBeanList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_scene_tap_to_run, viewGroup, false);
        return new LaunchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof LaunchViewHolder) {
            final LaunchViewHolder holder = (LaunchViewHolder) viewHolder;
            final SceneBean sceneBean = scenes.get(i);


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
                    NooieLog.e("---------TapToRun displayColor  " + displayColor);
                    StateListDrawable background = (StateListDrawable) holder.container.getBackground();
                    background.setColorFilter(new PorterDuffColorFilter(Color.parseColor(displayColor), PorterDuff.Mode.SRC_ATOP));
                }
            }

            holder.ivModify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onModifyClick(sceneBean);
                    }
                }
            });

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(i, sceneBean);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return scenes.size();
    }

    public static class LaunchViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.tvTask)
        TextView tvTask;
        @BindView(R.id.ivModify)
        ImageView ivModify;
        View container;

        public LaunchViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }

    public void setListener(LaunchItemListener listener) {
        mListener = listener;
    }

    public interface LaunchItemListener {
        void onItemClick(int position, SceneBean sceneBean);

        void onModifyClick(SceneBean sceneBean);
    }

}
