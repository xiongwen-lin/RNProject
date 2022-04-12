package com.afar.osaio.smart.scan.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.bean.ConnectionModeBean;
import com.afar.osaio.smart.scan.adapter.listener.ConnectionModeListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConnectionModeAdapter extends BaseAdapter<ConnectionModeBean, ConnectionModeListener, ConnectionModeAdapter.ConnectionModeVH> {

    @Override
    public ConnectionModeVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ConnectionModeVH(createVHView(R.layout.item_connection_mode, parent));
    }

    @Override
    public void onBindViewHolder(ConnectionModeVH holder, int position) {
        ConnectionModeBean connectionModeBean = mDatas.get(position);
        if (connectionModeBean != null) {
            holder.tvConnModeTitle.setText(connectionModeBean.getTitle());
            holder.tvConnModeContent.setText(connectionModeBean.getContent());
            holder.ivConnModeIcon.setImageResource(connectionModeBean.resId);
        }
        holder.containerConnectionMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onItemClick(connectionModeBean);
                }
            }
        });
    }

    public static class ConnectionModeVH extends RecyclerView.ViewHolder {

        @BindView(R.id.containerConnectionMode)
        public View containerConnectionMode;
        @BindView(R.id.tvConnModeTitle)
        public TextView tvConnModeTitle;
        @BindView(R.id.tvConnModeContent)
        public TextView tvConnModeContent;
        @BindView(R.id.ivConnModeIcon)
        public ImageView ivConnModeIcon;

        public ConnectionModeVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
