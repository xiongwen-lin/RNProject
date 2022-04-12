package com.afar.osaio.adapter;

import android.content.Context;
import android.net.NetworkInfo;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.bean.WifiAccessPoint;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WifiRecyclerAdapter extends RecyclerView.Adapter<WifiRecyclerAdapter.WifiViewHolder> {
    private Context mCtx;
    private ArrayList<WifiAccessPoint> mAccessPoint;

    private OnItemClickListener mItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view);

        void onItemLongClick(View view);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public WifiRecyclerAdapter(Context ctx, ArrayList<WifiAccessPoint> data) {
        this.mCtx = ctx;
        this.mAccessPoint = data;
    }

    @NonNull
    @Override
    public WifiViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.item_wifi_network, viewGroup, false);
        WifiViewHolder holder = new WifiViewHolder(view);
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemClickListener != null) mItemClickListener.onItemClick(view);
            }
        });

        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mItemClickListener != null) mItemClickListener.onItemLongClick(view);
                return true;
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull WifiViewHolder wifiViewHolder, int i) {
        wifiViewHolder.tvName.setText(mAccessPoint.get(i).ssid);
        wifiViewHolder.container.setTag(mAccessPoint.get(i));
        if (mAccessPoint.get(i).getState() == NetworkInfo.DetailedState.CONNECTED) {
            // connected
            wifiViewHolder.container.setBackgroundColor(ContextCompat.getColor(mCtx, R.color.selectColor));
        } else {
            // other
            wifiViewHolder.container.setBackgroundResource(R.drawable.setting_item_state_list);
        }
    }

    @Override
    public int getItemCount() {
        return mAccessPoint.size();
    }

    class WifiViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.container)
        RelativeLayout container;
        @BindView(R.id.ivIcon)
        ImageView ivIcon;
        @BindView(R.id.tvName)
        TextView tvName;

        public WifiViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
