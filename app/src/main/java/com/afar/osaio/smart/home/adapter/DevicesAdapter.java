package com.afar.osaio.smart.home.adapter;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.widget.SwitchImageButton;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.widget.RoundedImageView.RoundedImageView;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.smart.player.activity.BasePlayerActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.widget.NEventTextView;
import com.suke.widget.SwitchButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by victor on 2018/7/3
 * Email is victor.qiao.0604@gmail.com
 */
public class DevicesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static int DEFAULT_CAMERA_TYPE = 0;
    private static int ADD_CAMERA_TYPE = 0x02;
    private static int DRAG_CAMERA_TYPE = 0x03;
    public static int AP_DIRECT_CAMERA_TYPE = 0x04;

    private List<ListDeviceItem> mDeviceList = new ArrayList<>();
    private OnItemClickListener mItemClickListener;
    private int mDefineViewType = ConstantValue.DEFAULT_CAMERA_TYPE;
    private List<ApDeviceInfo> mApDevices = new ArrayList<>();
    private OnApDeviceClickListener mOnApDeviceClickListener;
    private OnConfigureChangeListener mOnConfigureChangeListener;

    public DevicesAdapter() {
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        LayoutInflater inflater = LayoutInflater.from(NooieApplication.mCtx);
        if (type == ConstantValue.AP_DIRECT_CAMERA_TYPE) {
            View view = inflater.inflate(R.layout.item_device, viewGroup, false);
            ApDeviceViewHolder viewHolder = new ApDeviceViewHolder(view);
            viewHolder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NooieLog.d("-->> NooieHomeActivity onClickItem 3000");
                    if (mOnApDeviceClickListener != null) {
                        ApDeviceInfo apDeviceInfo = view != null && view.getTag() != null ? (ApDeviceInfo)view.getTag() : null;
                        mOnApDeviceClickListener.onItemClick(apDeviceInfo);
                    }
                }
            });
            return viewHolder;
        } else if (mItemDragEnable) {
            View view = inflater.inflate(R.layout.item_device_drag, viewGroup, false);
            return new DeviceDragViewHolder(view);
        } else if (type == ConstantValue.ADD_CAMERA_TYPE) {
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
            View view = inflater.inflate(R.layout.item_device, viewGroup, false);
            final DeviceViewHolder viewHolder = new DeviceViewHolder(view);
            viewHolder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NooieLog.d("-->> NooieHomeActivity onClickItem 3000");
                    if (mItemClickListener != null) {
                        mItemClickListener.onClickItem((ListDeviceItem) view.getTag());
                    }
                }
            });
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof ApDeviceViewHolder) {
            final ApDeviceViewHolder deviceViewHolder = (ApDeviceViewHolder) viewHolder;
            ApDeviceInfo apDeviceInfo = mApDevices.get(i);
            BindDevice item = apDeviceInfo != null ? apDeviceInfo.getBindDevice() : null;
            if (deviceViewHolder == null) {
                return;
            }
            deviceViewHolder.container.setTag(apDeviceInfo);
            deviceViewHolder.switchSleep.setEnabled(false);
            deviceViewHolder.switchSleep.setOnCheckedChangeListener(null);
            if (item == null) {
                return;
            }
            deviceViewHolder.tvName.setText(item.getName());

            String file = BasePlayerActivity.getDevicePreviewFile(item.getUuid());
            Glide.with(NooieApplication.mCtx)
                    .load(file)
                    .apply(new RequestOptions()
                                    .dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 10))))
                                    .placeholder(R.drawable.default_preview)
                                    .format(DecodeFormat.PREFER_RGB_565).diskCacheStrategy(DiskCacheStrategy.NONE)
                    )
                    .transition(withCrossFade())
                    .into(deviceViewHolder.ivThumbnail);
            boolean isOpenCamera = item.getOpen_status() == ApiConstant.OPEN_STATUS_ON ? true : false;
            deviceViewHolder.ivThumbnailCover.setVisibility(!isOpenCamera ? View.VISIBLE : View.GONE);
            if (isOpenCamera) {
                deviceViewHolder.ivDeviceNamePoint.setImageResource(R.drawable.device_title_point_default);
                deviceViewHolder.tvOff.setVisibility(View.GONE);
                deviceViewHolder.switchSleep.setVisibility(View.GONE);
            } else {
                deviceViewHolder.ivDeviceNamePoint.setImageResource(R.drawable.device_title_point_sleep);
                deviceViewHolder.tvOff.setVisibility(View.VISIBLE);
                deviceViewHolder.switchSleep.setVisibility(View.VISIBLE);
                deviceViewHolder.switchSleep.setEnabled(true);
                if (deviceViewHolder.switchSleep.isChecked() != isOpenCamera) {
                    deviceViewHolder.switchSleep.toggleNoCallback();
                }
                deviceViewHolder.switchSleep.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                        if (mOnApDeviceClickListener != null) {
                            mOnApDeviceClickListener.onChangeSleep(apDeviceInfo, isChecked);
                        }
                    }
                });
            }
        } else if (viewHolder instanceof DeviceDragViewHolder) {
            final DeviceDragViewHolder deviceDragViewHolder = (DeviceDragViewHolder)viewHolder;
            ListDeviceItem item = mDeviceList.get(i);
            deviceDragViewHolder.tvDeviceName.setText(item.getName());

            String file = BasePlayerActivity.getDevicePreviewFile(mDeviceList.get(i).getDeviceId());
            /*
            int reqWidth = DisplayUtil.dpToPx(NooieApplication.mCtx, 333);
            int reqHeight = DisplayUtil.dpToPx(NooieApplication.mCtx, 166);
            Drawable placeHolderDrawable = BitmapUtil.getBitmapWithOption(file, reqWidth, reqHeight);
            if (placeHolderDrawable == null) {
                placeHolderDrawable = NooieApplication.mCtx.getResources().getDrawable(R.drawable.default_preview);
            }
            */
            Glide.with(NooieApplication.mCtx)
                    .load(file)
                    .apply(new RequestOptions()
                                    .dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 10))))
                                    //.placeholder(placeHolderDrawable)
                                    .placeholder(R.drawable.default_preview)
                                    .format(DecodeFormat.PREFER_RGB_565).diskCacheStrategy(DiskCacheStrategy.NONE)
                            //.error(R.drawable.default_preview)
                    )
                    .transition(withCrossFade())
                    .into(deviceDragViewHolder.ivDeviceThumb);

            /*
            deviceDragViewHolder.container.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (mItemClickListener != null) {
                            mItemClickListener.onStartDragItem(deviceDragViewHolder);
                        }
                    }
                    return false;
                }
            });
            */
        } else if (viewHolder instanceof DeviceViewHolder) {
            final DeviceViewHolder deviceViewHolder = (DeviceViewHolder) viewHolder;
            final ListDeviceItem item = mDeviceList.get(i);
            deviceViewHolder.container.setTag(item);
            deviceViewHolder.tvName.setText(item.getName());

            String file = BasePlayerActivity.getDevicePreviewFile(mDeviceList.get(i).getDeviceId());
            /*
            int reqWidth = DisplayUtil.dpToPx(NooieApplication.mCtx, 333);
            int reqHeight = DisplayUtil.dpToPx(NooieApplication.mCtx, 166);
            Drawable placeHolderDrawable = BitmapUtil.getBitmapWithOption(file, reqWidth, reqHeight);
            if (placeHolderDrawable == null) {
                placeHolderDrawable = NooieApplication.mCtx.getResources().getDrawable(R.drawable.default_preview);
            }
            */
            Glide.with(NooieApplication.mCtx)
                    .load(file)
                    .apply(new RequestOptions()
                                    .dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 10))))
                                    //.placeholder(placeHolderDrawable)
                                    .placeholder(R.drawable.default_preview)
                                    .format(DecodeFormat.PREFER_RGB_565).diskCacheStrategy(DiskCacheStrategy.NONE)
                                    //.error(R.drawable.default_preview)
                    )
                    .transition(withCrossFade())
                    .into(deviceViewHolder.ivThumbnail);

            //NooieLog.d("-->> DevicesAdapter onBindViewHolder id=" + item.getDeviceId() + " isOWner=" + (item.getBindType() == ApiConstant.BIND_TYPE_OWNER) + " openCloud=" + item.isOpenCloud());
            boolean isOpenCamera = item.getOpenStatus() == ApiConstant.OPEN_STATUS_ON ? true : false;

            deviceViewHolder.ivThumbnailCover.setVisibility(item.getOnline() == ApiConstant.ONLINE_STATUS_OFF || !isOpenCamera ? View.VISIBLE : View.GONE);

            deviceViewHolder.switchSleep.setEnabled(false);
            deviceViewHolder.switchSleep.setOnCheckedChangeListener(null);
            deviceViewHolder.switchSleep.setVisibility(View.GONE);

            deviceViewHolder.btnSwitchSleep.initBtn(R.drawable.ic_public_switch_on, R.drawable.ic_public_switch_off);
            deviceViewHolder.btnSwitchSleep.setEnabled(false);
            deviceViewHolder.btnSwitchSleep.setListener(null);
            if (item.getOnline() == ApiConstant.ONLINE_STATUS_ON) {
                deviceViewHolder.containerOffline.setVisibility(View.GONE);
                if (isOpenCamera) {
                    deviceViewHolder.ivDeviceNamePoint.setImageResource(R.drawable.online_circle);
                    deviceViewHolder.tvOff.setVisibility(View.GONE);
                    deviceViewHolder.switchSleep.setVisibility(View.GONE);
                    deviceViewHolder.btnSwitchSleep.setVisibility(View.GONE);
                } else {
                    deviceViewHolder.ivDeviceNamePoint.setImageResource(R.drawable.online_circle);
                    deviceViewHolder.tvOff.setVisibility(View.VISIBLE);
                    if (item.getBindType() == ApiConstant.BIND_TYPE_OWNER) {
                        /*
                        deviceViewHolder.switchSleep.setVisibility(View.VISIBLE);
                        deviceViewHolder.switchSleep.setEnabled(true);
                        if (deviceViewHolder.switchSleep.isChecked() != isOpenCamera) {
                            deviceViewHolder.switchSleep.toggleNoCallback();
                        }
                        deviceViewHolder.switchSleep.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                                if (mItemClickListener != null) {
                                    mItemClickListener.onChangeSleep(item, isChecked);
                                }
                            }
                        });

                         */

                        deviceViewHolder.btnSwitchSleep.setVisibility(View.VISIBLE);
                        deviceViewHolder.btnSwitchSleep.setEnabled(true);
                        if (deviceViewHolder.btnSwitchSleep.isOn() != isOpenCamera) {
                            deviceViewHolder.btnSwitchSleep.toggleNoCallback();
                        }
                        deviceViewHolder.btnSwitchSleep.setListener(new SwitchImageButton.OnStateChangeListener() {
                            @Override
                            public void onStateChange(boolean on) {
                                if (mItemClickListener != null) {
                                    mItemClickListener.onChangeSleep(item, on);
                                }
                            }
                        });
                    }
                }

            } else {

                deviceViewHolder.ivDeviceNamePoint.setImageResource(R.drawable.offline_circle);
                deviceViewHolder.tvOff.setVisibility(View.GONE);
                deviceViewHolder.switchSleep.setVisibility(View.GONE);
                deviceViewHolder.containerOffline.setVisibility(View.VISIBLE);
                deviceViewHolder.tvRefresh.setVisibility(item.isOpenCloud() ? View.VISIBLE : View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mDefineViewType == ConstantValue.AP_DIRECT_CAMERA_TYPE) {
            return CollectionUtil.size(mApDevices);
        } else if (mItemDragEnable) {
            return CollectionUtil.size(mDeviceList);
        } else {
            return CollectionUtil.size(mDeviceList);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mDefineViewType == ConstantValue.AP_DIRECT_CAMERA_TYPE) {
            return ConstantValue.AP_DIRECT_CAMERA_TYPE;
        } else if (mItemDragEnable) {
            return ConstantValue.DRAG_CAMERA_TYPE;
        } else if (CollectionUtil.isEmpty(mDeviceList)) {
            return super.getItemViewType(position);
        } else {
            return super.getItemViewType(position);
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
                    return 2;
                }
            });
        }
    }

    public void setData(List<ListDeviceItem> data) {
        if (mDeviceList == null) {
            mDeviceList = new ArrayList<>();
        }

        mDeviceList.clear();
        mDeviceList.addAll(CollectionUtil.safeFor(data));
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

    public void updateItemOpenStatus(String deviceId, int status) {
        for (int i = 0; i < CollectionUtil.safeFor(mDeviceList).size(); i++) {
            ListDeviceItem item = mDeviceList.get(i);
            if (item != null && !TextUtils.isEmpty(item.getDeviceId()) && item.getDeviceId().equalsIgnoreCase(deviceId)) {
                item.setOpenStatus(status);
                if (item.getBindDevice() != null) {
                    item.getBindDevice().setOpen_status(status);
                }
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void updateApDeviceOpenStatus(String deviceSsid, String deviceId, int status) {
        for (int i = 0; i < CollectionUtil.safeFor(mApDevices).size(); i++) {
            BindDevice item = mApDevices.get(i) != null ? mApDevices.get(i).getBindDevice() : null;
            if (item != null && !TextUtils.isEmpty(item.getUuid()) && item.getUuid().equalsIgnoreCase(deviceId)) {
                item.setOpen_status(status);
                notifyItemChanged(i);
                break;
            }
        }
        ApHelper.getInstance().updateOpenStatusInApDeviceCache(deviceSsid, status);
    }

    public void setApData(List<ApDeviceInfo> data) {
        if (mApDevices == null) {
            mApDevices = new ArrayList<>();
        }
        mApDevices.clear();
        mApDevices.addAll(CollectionUtil.safeFor(data));
        notifyDataSetChanged();
    }

    public void clearApData() {
        if (mApDevices != null) {
            mApDevices.clear();
        }
        notifyDataSetChanged();
    }

    public List<ApDeviceInfo> getApData() {
        return mApDevices;
    }

    private boolean mItemDragEnable = false;

    public void setItemDragEnable(boolean enable) {
        mItemDragEnable = enable;
        notifyDataSetChanged();
    }

    public boolean isItemDragEnable() {
        return mItemDragEnable;
    }

    public void toggleItemDrag() {
        setItemDragEnable(!mItemDragEnable);
    }

    public void setDefineViewType(int defineViewType) {
        if (mOnConfigureChangeListener != null) {
            mOnConfigureChangeListener.onDefineViewTypeChange(defineViewType);
        }
        if (mDefineViewType == defineViewType) {
            return;
        }
        mDefineViewType = defineViewType;
        notifyDataSetChanged();
    }

    public boolean isNewViewType(int viewType) {
        return viewType != mDefineViewType;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void setApDeviceClickListener(OnApDeviceClickListener listener) {
        mOnApDeviceClickListener = listener;
    }

    public void setConfigureChangeListener(OnConfigureChangeListener listener) {
        mOnConfigureChangeListener = listener;
    }

    public interface OnItemClickListener {
        void onClickItem(ListDeviceItem device);

        void onClickRefresh(ListDeviceItem device);

        void onChangeSleep(ListDeviceItem item, boolean openCamera);

        void onAddTop(ListDeviceItem device);

        void onStartDragItem(RecyclerView.ViewHolder holder);

        void onAddDevice();
    }

    public interface OnApDeviceClickListener {
        void onItemClick(ApDeviceInfo device);

        void onChangeSleep(ApDeviceInfo device, boolean openCamera);
    }

    public interface OnConfigureChangeListener {

        void onDefineViewTypeChange(int defineViewType);
    }

    public class ApDeviceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivDeviceNamePoint)
        ImageView ivDeviceNamePoint;
        @BindView(R.id.ivThumbnail)
        ImageView ivThumbnail;
        @BindView(R.id.ivThumbnailCover)
        RoundedImageView ivThumbnailCover;
        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.tvOff)
        TextView tvOff;
        @BindView(R.id.switchSleep)
        SwitchButton switchSleep;

        View container;

        public ApDeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container = itemView;
        }
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivDeviceNamePoint)
        ImageView ivDeviceNamePoint;
        @BindView(R.id.ivThumbnail)
        ImageView ivThumbnail;
        @BindView(R.id.ivThumbnailCover)
        RoundedImageView ivThumbnailCover;
        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.tvOff)
        TextView tvOff;
        @BindView(R.id.switchSleep)
        SwitchButton switchSleep;

        @BindView(R.id.containerOffline)
        ConstraintLayout containerOffline;
        @BindView(R.id.tvOfflineTip)
        TextView tvOfflineTip;
        @BindView(R.id.tvRefresh)
        TextView tvRefresh;
        @BindView(R.id.tvAddTop)
        TextView tvAddTop;
        @BindView(R.id.btnSwitchSleep)
        SwitchImageButton btnSwitchSleep;

        View container;

        public DeviceViewHolder(@NonNull View itemView) {
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

    public static class DeviceDragViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.itemDeviceDragContainer)
        View container;
        @BindView(R.id.tvDeviceName)
        TextView tvDeviceName;
        @BindView(R.id.ivDeviceThumb)
        ImageView ivDeviceThumb;
        @BindView(R.id.vDeviceShadowBottom)
        public View vDeviceShadowBottom;

        public DeviceDragViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}