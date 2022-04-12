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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AutomationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SceneBean> mScenes = new ArrayList<>();
    private Map<String, Boolean> mSelectedMap = new HashMap<>();
    private AutomationItemListener mListener;

    public void setData(List<SceneBean> sceneBeanList) {
        mScenes.clear();
        mScenes.addAll(sceneBeanList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_scene_automation, viewGroup, false);
        return new AutomationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof AutomationViewHolder) {
            final AutomationViewHolder holder = (AutomationViewHolder) viewHolder;
            final SceneBean sceneBean = mScenes.get(i);
            holder.tvAuto.setText(sceneBean.getName());

            if (mSelectedMap.containsKey(sceneBean.getId())) {
                holder.ivCheckBox.setSelected(true);
                holder.tvAble.setVisibility(View.VISIBLE);
                holder.tvAble.setText(mSelectedMap.get(sceneBean.getId()) ? R.string.enable : R.string.disable);
            } else {
                holder.ivCheckBox.setSelected(false);
                holder.tvAble.setVisibility(View.INVISIBLE);
            }

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectedMap.containsKey(sceneBean.getId())) {
                        holder.ivCheckBox.setSelected(false);
                        holder.tvAble.setVisibility(View.INVISIBLE);
                        deleteKeyOfMap(sceneBean.getId());
                    } else {
                        if (mListener != null) {
                            mListener.onItemClick(i, sceneBean, holder.ivCheckBox.isSelected());
                        }
                    }
                }
            });

        }
    }

    private void deleteKeyOfMap(String id) {
        NooieLog.e("=====删除前===== " + mSelectedMap.size());
        Iterator<String> iter = mSelectedMap.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            if (id.equals(key)) {
                iter.remove();
            }
        }
        NooieLog.e("=====删除后=====" + mSelectedMap.size());
    }

    @Override
    public int getItemCount() {
        return mScenes.size();
    }

    public List<SceneBean> getSelectedScene() {
        ArrayList<SceneBean> list = new ArrayList<>();
        Iterator<String> iter = mSelectedMap.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            for (SceneBean sceneBean : mScenes) {
                if (sceneBean.getId().equalsIgnoreCase(key)) {
                    NooieLog.d("--->> key " + key);
                    list.add(sceneBean);
                }
            }
        }
        return list;
    }

    public static class AutomationViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvAuto)
        TextView tvAuto;
        @BindView(R.id.tvAble)
        TextView tvAble;
        @BindView(R.id.ivCheckBox)
        ImageView ivCheckBox;
        View container;

        public AutomationViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }

    public void addSelectedScenes(SceneBean sceneBean, boolean enable) {
        //添加已选择的场景
        mSelectedMap.put(sceneBean.getId(), enable);
    }

    public void addScenes(int position, String sceneId, boolean enable) {
        //点击添加新的scenebean,设置scenebean的enable属性
        mScenes.get(position).setEnabled(enable);
        mSelectedMap.put(sceneId, enable);
        notifyDataSetChanged();
    }

    public void setListener(AutomationItemListener listener) {
        mListener = listener;
    }

    public interface AutomationItemListener {
        void onItemClick(int position, SceneBean sceneBean, boolean isSelect);
    }

}
