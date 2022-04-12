package com.afar.osaio.smart.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.bean.RouterDetalisInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RouterDetalisAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<RouterDetalisInfo> routerDetalisInfoList = new ArrayList<>();
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(NooieApplication.mCtx);
        View view = layoutInflater.inflate(R.layout.item_router_detalis, parent, false);
        RouterDetalisViewHolder routerDetalisViewHolder = new RouterDetalisViewHolder(view);
        return routerDetalisViewHolder;
    }

    public void setData(List<RouterDetalisInfo> list) {
        if (list == null || list.size() <= 0) {
            return;
        }
        routerDetalisInfoList.clear();
        routerDetalisInfoList.addAll(list);
        notifyDataSetChanged();
    }

    public void updataOnlineView(int onlineDevice) {
        routerDetalisInfoList.get(0).setConnectDeviceNum(onlineDevice);
        notifyDataSetChanged();
    }

    private OnClickRouterDetalisItemListener listener;

    public void setClickRouterDetalisItemListener(OnClickRouterDetalisItemListener listener) {
        this.listener = listener;
    }

    public interface OnClickRouterDetalisItemListener {
        public void clickRouterDetalisItem(int position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RouterDetalisViewHolder routerDetalisViewHolder = (RouterDetalisViewHolder)holder;
        RouterDetalisInfo detalisInfo = routerDetalisInfoList.get(position);
        routerDetalisViewHolder.container.setTag(position);

        routerDetalisViewHolder.detalisItem.setText(detalisInfo.getItemName());
        if (position == 0) {
            routerDetalisViewHolder.connect_device_state.setVisibility(View.VISIBLE);
            routerDetalisViewHolder.connect_device_num.setVisibility(View.VISIBLE);
            routerDetalisViewHolder.connect_device_state.setText(detalisInfo.isConnectDeviceState() ? NooieApplication.mCtx.getString(R.string.online) : NooieApplication.mCtx.getString(R.string.offline));
            routerDetalisViewHolder.connect_device_num.setText("(" + detalisInfo.getConnectDeviceNum() + ")");
        } else {
            routerDetalisViewHolder.connect_device_state.setVisibility(View.INVISIBLE);
            routerDetalisViewHolder.connect_device_num.setVisibility(View.INVISIBLE);
        }

        switch(position) {
            case 0:
                routerDetalisViewHolder.detalisItemIcon.setImageResource(R.drawable.connect_set_icon);
                break;
            case 1:
                routerDetalisViewHolder.detalisItemIcon.setImageResource(R.drawable.parental_set_icon);
                break;
            /*case 2:
                routerDetalisViewHolder.detalisItemIcon.setImageResource(R.drawable.security_set_icon);
                break;*/
            case 2:
                routerDetalisViewHolder.detalisItemIcon.setImageResource(R.drawable.wifi_set_icon);
                break;
            case 3:
                routerDetalisViewHolder.detalisItemIcon.setImageResource(R.drawable.guest_set_icon);
                break;
            case 4:
                routerDetalisViewHolder.detalisItemIcon.setImageResource(R.drawable.network_set_icon);
                break;
            case 5:
                routerDetalisViewHolder.detalisItemIcon.setImageResource(R.drawable.time_set_icon);
                break;
        }

        routerDetalisViewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    int position = (int)view.getTag();
                    listener.clickRouterDetalisItem(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return routerDetalisInfoList.size() > 0 ? routerDetalisInfoList.size() : 0;
    }

    public class RouterDetalisViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.detalisItemIcon)
        ImageView detalisItemIcon;
        @BindView(R.id.detalisItem)
        TextView detalisItem;
        @BindView(R.id.connect_device_state)
        TextView connect_device_state;
        @BindView(R.id.connect_device_num)
        TextView connect_device_num;

        View container;

        public RouterDetalisViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container = itemView;
        }
    }
}
