package com.afar.osaio.smart.electrician.smartScene.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.tuya.smart.home.sdk.bean.scene.dev.TaskListBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceFuncAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<TaskListBean> mTasks = new ArrayList<>();
    private FunctionItemListener mListener;

    public void setData(List<TaskListBean> tasks) {
        mTasks.clear();
        mTasks.addAll(tasks);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_scene_function, viewGroup, false);
        return new FunctionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof FunctionViewHolder) {
            FunctionViewHolder holder = (FunctionViewHolder) viewHolder;
            final TaskListBean taskListBean = mTasks.get(i);

            holder.tvName.setText(taskListBean.getName());

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(taskListBean);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    public static class FunctionViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvName)
        TextView tvName;
        View container;

        public FunctionViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }

    public void setListener(FunctionItemListener listener) {
        mListener = listener;
    }

    public interface FunctionItemListener {
        void onItemClick(TaskListBean taskListBean);
    }
}
