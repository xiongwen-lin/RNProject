package com.afar.osaio.smart.electrician.smartScene.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SortAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SceneBean> scenes = new ArrayList<>();
    private SortItemListener mListener;

    public void setData(List<SceneBean> sceneBeanList) {
        scenes.clear();
        scenes.addAll(sceneBeanList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_sort, viewGroup, false);
        return new SortViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof SortViewHolder) {
            final SortViewHolder holder = (SortViewHolder) viewHolder;
            final SceneBean sceneBean = scenes.get(i);

            if (sceneBean != null) {
                holder.tvName.setText(sceneBean.getName());
            }

            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemDelete(i, sceneBean);
                    }
                }
            });

           holder.ivDrag.setOnTouchListener(new View.OnTouchListener() {
               @Override
               public boolean onTouch(View v, MotionEvent event) {
                   if (event.getAction() == MotionEvent.ACTION_DOWN) {
                       if (mListener != null) {
                           mListener.onStartDragItem(holder);
                       }
                   }
                   return false;
               }
           });

        }
    }

    @Override
    public int getItemCount() {
        return scenes.size();
    }

    public static class SortViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.ivDelete)
        ImageView ivDelete;
        @BindView(R.id.ivDrag)
        ImageView ivDrag;
        View container;

        public SortViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }

    public void setListener(SortItemListener listener) {
        mListener = listener;
    }

    public List<SceneBean> getData() {
        return scenes;
    }

    public interface SortItemListener {

        void onItemDelete(int position, SceneBean sceneBean);

        void onStartDragItem(RecyclerView.ViewHolder holder);

    }

    public void removeItem(SceneBean sceneBean){
        if (scenes.contains(sceneBean)){
            scenes.remove(sceneBean);
            notifyDataSetChanged();
        }
    }

}
