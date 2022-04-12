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
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LaunchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SceneBean> scenes = new ArrayList<>();
    private Set<SceneBean> mSelectScene = new HashSet<>();
    private Set<SceneBean> selectedScenes = new HashSet<>();
    private LaunchItemListener mListener;

    public void setData(List<SceneBean> sceneBeanList) {
        mSelectScene.clear();
        scenes.clear();
        scenes.addAll(sceneBeanList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_scene_launch, viewGroup, false);
        return new LaunchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof LaunchViewHolder) {
            final LaunchViewHolder holder = (LaunchViewHolder) viewHolder;
            final SceneBean sceneBean = scenes.get(i);
            holder.tvLaunch.setText(sceneBean.getName());

            if (selectedScenes.contains(sceneBean)) {
                holder.ivCheckBox.setSelected(true);
                mSelectScene.add(sceneBean);
            } else {
                holder.ivCheckBox.setSelected(false);
                if (mSelectScene.contains(sceneBean)) {
                    mSelectScene.remove(sceneBean);
                }
            }

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.ivCheckBox.isSelected() && mSelectScene.contains(sceneBean)) {
                        holder.ivCheckBox.setSelected(false);
                        mSelectScene.remove(sceneBean);
                        if (selectedScenes.contains(sceneBean)){
                            selectedScenes.remove(sceneBean);
                        }
                    } else if (!holder.ivCheckBox.isSelected() && !mSelectScene.contains(sceneBean)) {
                        holder.ivCheckBox.setSelected(true);
                        mSelectScene.add(sceneBean);
                        selectedScenes.add(sceneBean);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return scenes.size();
    }

    public List<SceneBean> getSelectedScene() {
        return new ArrayList<>(mSelectScene);
    }

    public static class LaunchViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvLaunch)
        TextView tvLaunch;
        @BindView(R.id.ivCheckBox)
        ImageView ivCheckBox;
        View container;

        public LaunchViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }

    public void addSelectedScenes(SceneBean sceneBean) {
        selectedScenes.add(sceneBean);
        NooieLog.e("-----------------------select size addSelectedScenes " + selectedScenes.size());
    }

    public void setListener(LaunchItemListener listener) {
        mListener = listener;
    }

    public interface LaunchItemListener {
        void onItemClick(int position, SceneBean sceneBean);
    }

}
