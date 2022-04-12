package com.afar.osaio.smart.setting.adapter;

import android.graphics.Bitmap;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.afar.osaio.smart.device.helper.DeviceSettingHelper;
import com.afar.osaio.smart.setting.adapter.listener.PresetPointListener;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.entity.PresetPointConfigure;

import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class PresetPointAdapter extends BaseAdapter<PresetPointConfigure, PresetPointListener, RecyclerView.ViewHolder> {

    private String mAccount;
    private String mDeviceId;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_PRESET_POINT_ADD) {
            return new PresetPointAddVH(createVHView(R.layout.item_preset_point_add, parent));
        } else {
            return new PresetPointVH(createVHView(R.layout.item_preset_point, parent));
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder == null) {
            return;
        }
        if (holder instanceof PresetPointVH){
            PresetPointVH presetPointVH = (PresetPointVH)holder;
            PresetPointConfigure presetPointConfigure = getDataByPosition(position);
            if (presetPointVH == null || presetPointConfigure == null) {
                return;
            }

            presetPointVH.vPresetPointItemContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (presetPointVH == null) {
                        return;
                    }
                    if (mListener != null) {
                        mListener.onItemClick(presetPointVH.getAdapterPosition(), presetPointConfigure);
                    }
                }
            });

            /*
            presetPointVH.vPresetPointItemContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (presetPointVH == null || !checkDragEnable(presetPointVH.getAdapterPosition())) {
                        return false;
                    }
                    if (mListener != null) {
                        mListener.onStartDragItem(presetPointVH);
                    }
                    return false;
                }
            });
            */

            presetPointVH.btnPresetPointDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NooieLog.d("-->> debug PresetPointAdapter onClick: delete");
                    if (presetPointVH == null) {
                        return;
                    }
                    if (mListener != null) {
                        mListener.onItemDeleteClick(presetPointVH.getAdapterPosition(), presetPointConfigure);
                    }
                }
            });

            presetPointVH.btnPresetPointEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NooieLog.d("-->> debug PresetPointAdapter onClick: edit");
                    if (presetPointVH == null) {
                        return;
                    }
                    if (mListener != null) {
                        mListener.onItemEditClick(presetPointVH.getAdapterPosition(), presetPointConfigure);
                    }
                }
            });

            presetPointVH.tvPresetPointName.setText(presetPointConfigure.getName());
            NooieLog.d("-->> debug PresetPointAdapter onBindViewHolder: iconpath=" + FileUtil.getPresetPointThumbnail(NooieApplication.mCtx, getAccount(), getDeviceId(), presetPointConfigure.getPosition()));
            Glide.with(NooieApplication.mCtx)
                    .load(FileUtil.getPresetPointThumbnail(NooieApplication.mCtx, getAccount(), getDeviceId(), presetPointConfigure.getPosition()))
                    .apply(new RequestOptions()
                            .dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 5))))
                            .placeholder(R.drawable.default_preview_thumbnail)
                            .format(DecodeFormat.PREFER_RGB_565)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                    )
                    .transition(withCrossFade())
                    .into(presetPointVH.ivPresetPointIcon);
        } else if (holder instanceof PresetPointAddVH) {
            PresetPointAddVH presetPointAddVH = (PresetPointAddVH)holder;
            if (presetPointAddVH == null) {
                return;
            }
            presetPointAddVH.vPresetPointAddItemContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NooieLog.d("-->> debug PresetPointAdapter onClick: add");
                    if (mListener != null) {
                        mListener.onItemAddClick(CollectionUtil.size(mDatas) + 1);
                    }
                }
            });
        }
    }

    private static final int PRESET_POINT_MAX_SIZE = 3;
    private static final int VIEW_TYPE_PRESET_POINT_ADD = 1;
    private static final int VIEW_TYPE_PRESET_POINT = 2;
    @Override
    public int getItemViewType(int position) {
        return CollectionUtil.size(getData()) < PRESET_POINT_MAX_SIZE && position == getItemCount() - 1 ? VIEW_TYPE_PRESET_POINT_ADD : VIEW_TYPE_PRESET_POINT;
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.size(getData()) < PRESET_POINT_MAX_SIZE ? CollectionUtil.size(getData()) + 1 : CollectionUtil.size(getData());
    }

    public PresetPointConfigure getDataByPosition(int position) {
        if (CollectionUtil.isEmpty(mDatas) || position < 0 || position >= CollectionUtil.size(mDatas)) {
            return null;
        }
        return mDatas.get(position);
    }

    public void setAccountAndDeviceId(String account, String deviceId) {
        mAccount = account;
        mDeviceId = deviceId;
    }

    public void updateItemName(PresetPointConfigure presetPointConfigure) {
        if (presetPointConfigure == null || TextUtils.isEmpty(presetPointConfigure.getName())) {
            return;
        }
        for (int i = 0; i < CollectionUtil.size(mDatas); i++) {
            if (mDatas.get(i) !=null && mDatas.get(i).getPosition() == presetPointConfigure.getPosition()) {
                mDatas.get(i).setName(presetPointConfigure.getName());
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void deleteItemByPosition(PresetPointConfigure presetPointConfigure) {
        if (presetPointConfigure == null || CollectionUtil.isEmpty(mDatas)) {
            return;
        }
        Iterator<PresetPointConfigure> iterator = mDatas.iterator();
        while (iterator.hasNext()) {
            PresetPointConfigure configure = iterator.next();
            if (configure != null && configure.getPosition() == presetPointConfigure.getPosition()) {
                iterator.remove();
                notifyDataSetChanged();
                break;
            }
        }
        setData(DeviceSettingHelper.sortPresetPointConfigureList(mDatas));
    }

    private String getAccount() {
        return mAccount;
    }

    private String getDeviceId() {
        return mDeviceId;
    }

    private boolean checkDragEnable(int position) {
        boolean isDragEnable = CollectionUtil.size(mDatas) > 1 && position >= 0 && position < CollectionUtil.size(mDatas);
        return isDragEnable;
    }

    public static class PresetPointVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vPresetPointItemContainer)
        View vPresetPointItemContainer;
        @BindView(R.id.ivPresetPointIcon)
        ImageView ivPresetPointIcon;
        @BindView(R.id.btnPresetPointDelete)
        ImageView btnPresetPointDelete;
        @BindView(R.id.tvPresetPointName)
        TextView tvPresetPointName;
        @BindView(R.id.btnPresetPointEdit)
        ImageView btnPresetPointEdit;

        public PresetPointVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class PresetPointAddVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vPresetPointAddItemContainer)
        View vPresetPointAddItemContainer;
        @BindView(R.id.ivPresetPointItemAddBg)
        ImageView ivPresetPointItemAddBg;
        @BindView(R.id.ivPresetPointItemAdd)
        ImageView ivPresetPointItemAdd;

        public PresetPointAddVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
