package com.afar.osaio.smart.media.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.media.bean.BaseCameraMediaBean;
import com.afar.osaio.smart.media.bean.CameraPhotoItemBean;
import com.afar.osaio.smart.media.listener.CameraPhotoGridListener;
import com.afar.osaio.smart.media.listener.CameraPhotoListener;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.time.DateTimeUtil;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CameraPhotoAdapter extends BaseAdapter<CameraPhotoItemBean, CameraPhotoListener, RecyclerView.ViewHolder> {

    public static final int CAMERA_PHOTO_VIEW_TYPE_LIST = 1;
    public static final int CAMERA_PHOTO_VIEW_TYPE_EMPTY = 2;
    public static final int CAMERA_PHOTO_VIEW_TYPE_LOADING = 3;
    public static final int CAMERA_PHOTO_VIEW_TYPE_NO_SD = 4;
    public static final int CAMERA_PHOTO_VIEW_TYPE_RETRY = 5;

    private int mVHType = CAMERA_PHOTO_VIEW_TYPE_EMPTY;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == CAMERA_PHOTO_VIEW_TYPE_LIST) {
            return new CameraPhotoVH(createVHView(R.layout.item_media_list, parent));
        } else {
            return new CameraPhotoOtherVH(createVHView(R.layout.item_camera_photo_other, parent), viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder == null) {
            return;
        }
        if (holder instanceof CameraPhotoVH) {
            CameraPhotoItemBean data = getDataByPosition(position);
            if (data == null) {
                return;
            }
            CameraPhotoVH cameraPhotoVH = (CameraPhotoVH) holder;
            cameraPhotoVH.tvMediaListDate.setText(getItemDateTitle(data.getDate()));
            cameraPhotoVH.rcvMediaListThumbs.setLayoutManager(new GridLayoutManager(NooieApplication.mCtx, 3));
            CameraPhotoGridAdapter photoGridAdapter = new CameraPhotoGridAdapter();
            photoGridAdapter.setListener(new CameraPhotoGridListener() {
                @Override
                public void onItemClick(BaseCameraMediaBean mediaBean) {
                    if (mListener != null) {
                        mListener.onItemClick(mediaBean);
                    }
                }
            });
            cameraPhotoVH.rcvMediaListThumbs.setAdapter(photoGridAdapter);
            photoGridAdapter.setData(data.getMedias());
        } else if (holder instanceof  CameraPhotoOtherVH) {
            CameraPhotoOtherVH cameraPhotoOtherVH = (CameraPhotoOtherVH) holder;
            cameraPhotoOtherVH.vCameraPhotoOther.setOnClickListener(null);
            if (cameraPhotoOtherVH.getViewType() == CAMERA_PHOTO_VIEW_TYPE_EMPTY) {
                cameraPhotoOtherVH.ivCameraPhotoOtherIcon.setImageResource(R.drawable.empty_picture_icon);
                cameraPhotoOtherVH.tvCameraPhotoOtherTitle.setText(NooieApplication.mCtx.getText(R.string.photo_media_empty));
            } else if (cameraPhotoOtherVH.getViewType() == CAMERA_PHOTO_VIEW_TYPE_LOADING) {
                cameraPhotoOtherVH.ivCameraPhotoOtherIcon.setImageResource(R.drawable.loading_icon);
                cameraPhotoOtherVH.tvCameraPhotoOtherTitle.setText(NooieApplication.mCtx.getText(R.string.photo_media_loading));
            } else if (cameraPhotoOtherVH.getViewType() == CAMERA_PHOTO_VIEW_TYPE_NO_SD) {
                cameraPhotoOtherVH.ivCameraPhotoOtherIcon.setImageResource(R.drawable.sd_card_icon);
                cameraPhotoOtherVH.tvCameraPhotoOtherTitle.setText(NooieApplication.mCtx.getText(R.string.photo_media_no_sd));
            } else if (cameraPhotoOtherVH.getViewType() == CAMERA_PHOTO_VIEW_TYPE_RETRY) {
                cameraPhotoOtherVH.ivCameraPhotoOtherIcon.setImageResource(R.drawable.retry_icon);
                cameraPhotoOtherVH.tvCameraPhotoOtherTitle.setText(NooieApplication.mCtx.getText(R.string.photo_media_retry));
                cameraPhotoOtherVH.vCameraPhotoOther.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onRetryClick();
                        }
                    }
                });
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mVHType == CAMERA_PHOTO_VIEW_TYPE_LIST) {
            return CollectionUtil.isEmpty(getData()) ? CAMERA_PHOTO_VIEW_TYPE_EMPTY : CAMERA_PHOTO_VIEW_TYPE_LIST;
        } else {
            return mVHType;
        }
    }

    @Override
    public int getItemCount() {
        return mVHType != CAMERA_PHOTO_VIEW_TYPE_LIST || CollectionUtil.isEmpty(mDatas) ? 1 : CollectionUtil.size(mDatas);
    }

    public void setVHType(int mVHType) {
        this.mVHType = mVHType;
        notifyDataSetChanged();
    }

    private String getItemDateTitle(long time) {
        StringBuilder titleSb = new StringBuilder();
        titleSb.append(DateTimeUtil.formatDate(NooieApplication.mCtx, time, DateTimeUtil.PATTERN_YMD));
        titleSb.append(" ");
        titleSb.append(DateTimeUtil.getUtcDisplayName(NooieApplication.mCtx, time, Calendar.DAY_OF_WEEK, Calendar.LONG));
        return titleSb.toString();
    }

    public static final class CameraPhotoVH extends RecyclerView.ViewHolder {

        @BindView(R.id.tvMediaListDate)
        TextView tvMediaListDate;
        @BindView(R.id.rcvMediaListThumbs)
        RecyclerView rcvMediaListThumbs;

        public CameraPhotoVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static final class CameraPhotoOtherVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vCameraPhotoOther)
        View vCameraPhotoOther;
        @BindView(R.id.ivCameraPhotoOtherIcon)
        ImageView ivCameraPhotoOtherIcon;
        @BindView(R.id.tvCameraPhotoOtherTitle)
        TextView tvCameraPhotoOtherTitle;

        private int viewType;

        public CameraPhotoOtherVH(View view, int viewType) {
            super(view);
            ButterKnife.bind(this, view);
            setViewType(viewType);
        }

        public int getViewType() {
            return viewType;
        }

        public void setViewType(int viewType) {
            this.viewType = viewType;
        }
    }
}
