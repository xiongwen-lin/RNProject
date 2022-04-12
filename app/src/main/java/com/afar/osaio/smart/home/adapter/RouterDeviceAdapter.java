package com.afar.osaio.smart.home.adapter;

import android.text.TextUtils;
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
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.widget.RoundedImageView.RoundedImageView;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.widget.NEventTextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by victor on 2018/7/3
 * Email is victor.qiao.0604@gmail.com
 */
public class RouterDeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static int DEFAULT_CAMERA_TYPE = 0;
    private static int ADD_CAMERA_TYPE = 0x01;

    private List<ListDeviceItem> mDeviceList = new ArrayList<>();
    private OnItemClickListener mItemClickListener;
    private OnRouterDeviceClickListener mOnRouterDeviceClickListener;

    public RouterDeviceAdapter() {
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        LayoutInflater inflater = LayoutInflater.from(NooieApplication.mCtx);
        if (type == ConstantValue.ADD_CAMERA_TYPE) {
            View view = inflater.inflate(R.layout.item_add_device, viewGroup, false);
            AddCameraViewHolder footerViewHolder = new AddCameraViewHolder(view);
            footerViewHolder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onAddDevice();
                    }
                }
            });
            footerViewHolder.btnAddDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onAddDevice();
                    }
                }
            });
            return footerViewHolder;
        } else {
            View view = inflater.inflate(R.layout.item_device1, viewGroup, false);
            RouterDeviceViewHolder viewHolder = new RouterDeviceViewHolder(view);
            viewHolder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NooieLog.d("-->> NooieHomeActivity onClickItem 2000");
                    if (mOnRouterDeviceClickListener != null) {
                        //RouterDeviceInfo routerDeviceInfo = view != null && view.getTag() != null ? (RouterDeviceInfo)view.getTag() : null;
                        mOnRouterDeviceClickListener.onRouterItemClick("" + (int) view.getTag(), mDeviceList.get((int) view.getTag()).getName(),
                                mDeviceList.get((int) view.getTag()).getDeviceMac(),
                                mDeviceList.get((int) view.getTag()).getIsBind(), "" + mDeviceList.get((int) view.getTag()).getOnline());
                    }
                }
            });

            viewHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (mOnRouterDeviceClickListener != null) {
                        //RouterDeviceInfo routerDeviceInfo = view != null && view.getTag() != null ? (RouterDeviceInfo)view.getTag() : null;
                        mOnRouterDeviceClickListener.onRouterLongItemClick("" + (int) view.getTag(), mDeviceList.get((int) view.getTag()).getName(),
                                mDeviceList.get((int) view.getTag()).getDeviceMac());
                    }
                    // 长按事件与点击事件冲突(默认为点击事件，点击事件无返回值,长按有返回值,若 返回 false,则会触发点击事件,返回true则不会)
                    //return false;
                    return true;
                }
            });
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof RouterDeviceViewHolder) {
            final RouterDeviceViewHolder deviceViewHolder = (RouterDeviceViewHolder) viewHolder;
            ListDeviceItem listDeviceItem = mDeviceList.get(i);
            BindDevice item = listDeviceItem != null ? listDeviceItem.getBindDevice() : null;
            if (deviceViewHolder == null) {
                return;
            }
            deviceViewHolder.container.setTag(i);
            deviceViewHolder.tvName.setText(listDeviceItem.getName());
            /*deviceViewHolder.ivDeviceNamePoint.setBackgroundResource(listDeviceItem.getOnline() == 1 ? R.drawable.round_green : R.drawable.round); // solid_circle_red
            deviceViewHolder.isOnLine.setText(listDeviceItem.getOnline() == 1 ? R.string.online : R.string.offline);*/
            if (listDeviceItem.getOnline() == 1) {
                //deviceViewHolder.ivDeviceNamePoint.setVisibility(View.VISIBLE);
                deviceViewHolder.isOnLine.setVisibility(View.VISIBLE);
                deviceViewHolder.ivDeviceNamePoint.setBackgroundResource(R.drawable.round_green);
                deviceViewHolder.isOnLine.setText(R.string.online);
            } else if (listDeviceItem.getOnline() == 0) {
                //deviceViewHolder.ivDeviceNamePoint.setVisibility(View.VISIBLE);
                deviceViewHolder.isOnLine.setVisibility(View.VISIBLE);
                deviceViewHolder.ivDeviceNamePoint.setBackgroundResource(R.drawable.round);
                deviceViewHolder.isOnLine.setText(R.string.offline);
            } else {
                //deviceViewHolder.ivDeviceNamePoint.setVisibility(View.INVISIBLE);
                deviceViewHolder.isOnLine.setVisibility(View.INVISIBLE);

                // 手机没有连接路由器wifi,不显示文案,点击时弹窗提示
                /*deviceViewHolder.ivDeviceNamePoint.setVisibility(View.VISIBLE);
                deviceViewHolder.isOnLine.setVisibility(View.VISIBLE);*/
                deviceViewHolder.ivDeviceNamePoint.setBackgroundResource(R.drawable.round);
                deviceViewHolder.isOnLine.setText(R.string.offline);
            }

        }
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.isEmpty(mDeviceList) ? 1 : CollectionUtil.size(mDeviceList);
    }

    @Override
    public int getItemViewType(int position) {
        return CollectionUtil.isEmpty(mDeviceList) ? ConstantValue.ADD_CAMERA_TYPE : ConstantValue.AP_DIRECT_CAMERA_TYPE;
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
                    return 2;
                    /*
                    if (CollectionUtil.isEmpty(mDeviceList)) {
                        return 2;
                    } else {
                        return 1;
                    }
                     */
                }
            });
        }
    }

    public void setData(List<ListDeviceItem> data) {
        if (mDeviceList == null) {
            mDeviceList = new ArrayList<>();
        }

        mDeviceList.clear();
        if (data == null || data.size() <= 0) {
            return;
        }

        mDeviceList.addAll(CollectionUtil.safeFor(data));
        notifyDataSetChanged();
    }

    public void upData(String routerWifiMac) {
        if (mDeviceList == null) {
            mDeviceList = new ArrayList<>();
        }

        for (int i = 0; i < mDeviceList.size(); i++) {
            if (routerWifiMac.equals(mDeviceList.get(i).getDeviceMac())) {
                mDeviceList.get(i).setOnline(1);
                /*if (TextUtils.isEmpty(connectedState)) {
                    mDeviceList.get(i).setOnline(1);
                } else {
                    if ("disconnected".equals(connectedState)) {
                        mDeviceList.get(i).setOnline(0);
                    } else {
                        mDeviceList.get(i).setOnline(1);
                    }
                }*/

            } else {
                //绑定时逻辑
                //mDeviceList.get(i).setOnline(isOffline ? 0 : 1);
                mDeviceList.get(i).setOnline(0);
            }
        }
        notifyDataSetChanged();
    }

    public void removeRouterDevice(String routerWifiMac) {
        if (TextUtils.isEmpty(routerWifiMac) || CollectionUtil.isEmpty(mDeviceList)) {
            return;
        }
        Iterator<ListDeviceItem> itemIterator = mDeviceList.iterator();
        while (itemIterator.hasNext()) {
            ListDeviceItem deviceItem = itemIterator.next();
            if (deviceItem != null && routerWifiMac.equalsIgnoreCase(deviceItem.getDeviceMac())) {
                itemIterator.remove();
            }
        }
        notifyDataSetChanged();
    }

    public void addData(List<ListDeviceItem> data) {
        if (mDeviceList == null) {
            mDeviceList = new ArrayList<>();
        }

        mDeviceList.addAll(CollectionUtil.safeFor(data));
        notifyDataSetChanged();
    }

    public void clearData() {
        if (mDeviceList != null) {
            mDeviceList.clear();
        }
        notifyDataSetChanged();
    }

    public List<ListDeviceItem> getData() {
        return mDeviceList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void setRouterDeviceClickListener(OnRouterDeviceClickListener listener) {
        mOnRouterDeviceClickListener = listener;
    }

    public interface OnItemClickListener {
        void onClickItem(ListDeviceItem device);

        void onAddDevice();
    }

    public interface OnRouterDeviceClickListener {
        void onRouterItemClick(String device, String routerName, String routerMac, String isbind,
                               String isOnline);

        void onRouterLongItemClick(String device, String routerName, String routerMac);
    }

    public class RouterDeviceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivDeviceNamePoint)
        View ivDeviceNamePoint;
        @BindView(R.id.ivThumbnail)
        ImageView ivThumbnail;
        @BindView(R.id.ivThumbnailCover)
        RoundedImageView ivThumbnailCover;
        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.isOnline)
        TextView isOnLine;

        View container;

        public RouterDeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container = itemView;
        }
    }

    class AddCameraViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.vAddDeviceContainer)
        View container;
        @BindView(R.id.btnAddDevice)
        NEventTextView btnAddDevice;

        public AddCameraViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}