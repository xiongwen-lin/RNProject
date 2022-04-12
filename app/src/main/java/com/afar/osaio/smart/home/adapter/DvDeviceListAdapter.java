package com.afar.osaio.smart.home.adapter;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.widget.SwitchImageButton;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.smart.home.adapter.listener.DvDeviceListListener;
import com.afar.osaio.smart.player.activity.BasePlayerActivity;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.widget.RoundedImageView.RoundedImageView;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.suke.widget.SwitchButton;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class DvDeviceListAdapter extends BaseAdapter<ApDeviceInfo, DvDeviceListListener, DvDeviceListAdapter.DvDeviceListViewHolder> {

    @Override
    public DvDeviceListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DvDeviceListViewHolder(createVHView(R.layout.item_ap_device, parent));
    }

    @Override
    public void onBindViewHolder(DvDeviceListViewHolder holder, int position) {
        if (holder == null) {
            return;
        }
        ApDeviceInfo apDeviceInfo = getDataByPosition(position);
        if (apDeviceInfo == null || apDeviceInfo.getBindDevice() == null) {
            holder.container.setOnClickListener(null);
            holder.switchSleep.setEnabled(false);
            holder.switchSleep.setOnCheckedChangeListener(null);
            return;
        }
        String name = apDeviceInfo.getBindDevice().getName();
        holder.ivThumbnailCover.setVisibility(View.GONE);
        holder.tvName.setText(name);

        BindDevice device = apDeviceInfo.getBindDevice();
        holder.switchSleep.setEnabled(false);
        holder.switchSleep.setOnCheckedChangeListener(null);
        holder.switchSleep.setVisibility(View.GONE);

        holder.btnSwitchSleep.initBtn(R.drawable.ic_public_switch_on, R.drawable.ic_public_switch_off);
        holder.btnSwitchSleep.setEnabled(false);
        holder.btnSwitchSleep.setListener(null);
        holder.tvName.setText(device.getName());

        String file = BasePlayerActivity.getDevicePreviewFile(device.getUuid());
        Glide.with(NooieApplication.mCtx)
                .load(file)
                .apply(new RequestOptions()
                        .dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 10))))
                        .placeholder(R.drawable.default_preview)
                        .format(DecodeFormat.PREFER_RGB_565).diskCacheStrategy(DiskCacheStrategy.NONE)
                )
                .transition(withCrossFade())
                .into(holder.ivThumbnail);
        boolean isOpenCamera = device.getOpen_status() == ApiConstant.OPEN_STATUS_ON ? true : false;
        holder.ivThumbnailCover.setVisibility(!isOpenCamera ? View.VISIBLE : View.GONE);
        if (isOpenCamera) {
            holder.ivDeviceNamePoint.setImageResource(R.drawable.online_circle);
            holder.tvOff.setVisibility(View.GONE);
            holder.switchSleep.setVisibility(View.GONE);
        } else {
            holder.ivDeviceNamePoint.setImageResource(R.drawable.online_circle);
            holder.tvOff.setVisibility(View.VISIBLE);
            /*
            holder.switchSleep.setVisibility(View.VISIBLE);
            holder.switchSleep.setEnabled(true);
            if (holder.switchSleep.isChecked() != isOpenCamera) {
                holder.switchSleep.toggleNoCallback();
            }
            holder.switchSleep.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                    if (mListener != null) {
                        mListener.onChangeSleep(apDeviceInfo, isChecked);
                    }
                }
            });

             */

            holder.btnSwitchSleep.setVisibility(View.VISIBLE);
            holder.btnSwitchSleep.setEnabled(true);
            if (holder.btnSwitchSleep.isOn() != isOpenCamera) {
                holder.btnSwitchSleep.toggleNoCallback();
            }
            holder.btnSwitchSleep.setListener(new SwitchImageButton.OnStateChangeListener() {
                @Override
                public void onStateChange(boolean on) {
                    if (mListener != null) {
                        mListener.onChangeSleep(apDeviceInfo, on);
                    }
                }
            });
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(apDeviceInfo);
                }
            }
        });
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

    public void updateDvDeviceOpenStatus(String deviceId, int status) {
        for (int i = 0; i < CollectionUtil.safeFor(mDatas).size(); i++) {
            BindDevice item = mDatas.get(i) != null ? mDatas.get(i).getBindDevice() : null;
            if (item != null && !TextUtils.isEmpty(item.getUuid()) && item.getUuid().equalsIgnoreCase(deviceId)) {
                item.setOpen_status(status);
                notifyItemChanged(i);
                break;
            }
        }
        ApHelper.getInstance().updateCurrentApDeviceInfoOfOpenStatus(deviceId, status);
    }

    public static final class DvDeviceListViewHolder extends RecyclerView.ViewHolder {

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
        @BindView(R.id.btnSwitchSleep)
        SwitchImageButton btnSwitchSleep;

        @BindView(R.id.container)
        View container;

        public DvDeviceListViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
