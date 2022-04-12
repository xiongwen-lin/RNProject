package com.afar.osaio.smart.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.bean.RouterDeviceConnectInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OnlineDeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static int ITEM_TITLE_TYPE = 0;
    private static int ITEM_DEVICE_TYPE = 1;

    List<RouterDeviceConnectInfo> mConnectLists = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(NooieApplication.mCtx);
        if (viewType == ITEM_DEVICE_TYPE) {
            View view = layoutInflater.inflate(R.layout.item_router_connect, parent,false);
            OnlineDeviceViewHolder onlineDeviceViewHolder = new OnlineDeviceViewHolder(view);
            return onlineDeviceViewHolder;
        } else {
            View view = layoutInflater.inflate(R.layout.item_router_connect_title, parent,false);
            TitleViewHolder titleViewHolder = new TitleViewHolder(view);
            return titleViewHolder;
        }
    }

    public void setData(List<RouterDeviceConnectInfo> list) {
        if (list == null || list.size() <= 0) {
            return;
        }

        mConnectLists.clear();
        mConnectLists.addAll(list);
        notifyDataSetChanged();
    }

    public void updata(int position, String deviceName,String isWhite) {
        if (position < 0 || mConnectLists.size() <= 0) {
           return;
        }
        mConnectLists.get(position).setDeviceName(deviceName);
        mConnectLists.get(position).setIsWhite(isWhite);
        notifyDataSetChanged();
    }

    private int getDeviceNum(boolean isOnline) {
        if (mConnectLists == null || mConnectLists.size() <= 0) {
            return -1;
        }

        int cnt = 0;
        // 在线设备数量
        if (isOnline) {
            for (int i = 0; i < mConnectLists.size(); i++) {
                if (mConnectLists.get(i).isOnline()) {
                    cnt = cnt + 1;
                }
            }
        } else {
            for (int i = 0; i < mConnectLists.size(); i++) {
                if (!mConnectLists.get(i).isOnline() && mConnectLists.get(i).getTitle().equals("")) {
                    cnt = cnt + 1;
                }
            }
        }
        return cnt;
    }

    private OnRouterDevicesClickListener listener;
    public void setRouterDevicesClickListener(OnRouterDevicesClickListener listener) {
        this.listener = listener;
    }

    public interface OnRouterDevicesClickListener{
        public void routerDeviceClick(int position, RouterDeviceConnectInfo device);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof OnlineDeviceViewHolder) {
            OnlineDeviceViewHolder onlineDeviceViewHolder = (OnlineDeviceViewHolder) holder;
            RouterDeviceConnectInfo routerDeviceConnectInfo = mConnectLists.get(position);
            onlineDeviceViewHolder.container.setTag(position);

            onlineDeviceViewHolder.deviceName.setText(routerDeviceConnectInfo.getDeviceName());
            onlineDeviceViewHolder.intenetType.setText(routerDeviceConnectInfo.getConnectWifiType());
            onlineDeviceViewHolder.minSpeed.setText(routerDeviceConnectInfo.getMinSpeed());
            onlineDeviceViewHolder.maxSpeed.setText(routerDeviceConnectInfo.getMaxSpeed());
            onlineDeviceViewHolder.lastConnectTime.setText(routerDeviceConnectInfo.getLastConnectTime());

            onlineDeviceViewHolder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = (int)view.getTag();
                        listener.routerDeviceClick(position, routerDeviceConnectInfo);
                    }
                }
            });
        } else {
            TitleViewHolder titleViewHolder = (TitleViewHolder) holder;
            RouterDeviceConnectInfo routerDeviceConnectInfo = mConnectLists.get(position);
            titleViewHolder.container.setTag(position);

            // online
            if (getDeviceNum(true) > 0) {
                titleViewHolder.tvTitle.setText(R.string.online);
                titleViewHolder.deviceNum.setText("(" + getDeviceNum(true) + ")");
            } else {
                titleViewHolder.tvTitle.setVisibility(View.INVISIBLE);
                titleViewHolder.deviceNum.setVisibility(View.INVISIBLE);
            }
            /*if (routerDeviceConnectInfo.getTitle().equals("Online Devices")) {
                if (getDeviceNum(true) > 0) {
                    titleViewHolder.tvTitle.setText(routerDeviceConnectInfo.getTitle());
                    titleViewHolder.deviceNum.setText("(" + getDeviceNum(true) + ")");
                } else {
                    titleViewHolder.tvTitle.setVisibility(View.INVISIBLE);
                    titleViewHolder.deviceNum.setVisibility(View.INVISIBLE);
                }
            } else {
                if (getDeviceNum(false) > 0) {
                    titleViewHolder.tvTitle.setText(routerDeviceConnectInfo.getTitle());
                    titleViewHolder.deviceNum.setText("(" + getDeviceNum(false) + ")");
                } else {
                    titleViewHolder.tvTitle.setVisibility(View.INVISIBLE);
                    titleViewHolder.deviceNum.setVisibility(View.INVISIBLE);
                }
            }*/
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (mConnectLists.get(position).getTitle().equals("")) {
                        return 1;
                    } else {
                        return 2;
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mConnectLists.size() > 0 ? mConnectLists.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (mConnectLists.get(position).getTitle().equals("")) {
            return ITEM_DEVICE_TYPE;
        } else {
            return ITEM_TITLE_TYPE;
        }
    }

    public class OnlineDeviceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.connectItemIcon)
        ImageView connectItemIcon;
        @BindView(R.id.deviceName)
        TextView deviceName;
        @BindView(R.id.intenetType)
        TextView intenetType;
        @BindView(R.id.minSpeed)
        TextView minSpeed;
        @BindView(R.id.maxSpeed)
        TextView maxSpeed;
        @BindView(R.id.lastConnectTime)
        TextView lastConnectTime;

        View container;

        public OnlineDeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container = itemView;
        }
    }

    public class TitleViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.deviceNum)
        TextView deviceNum;

        private View container;

        public TitleViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container = itemView;
        }
    }
}
