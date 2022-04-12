package com.afar.osaio.smart.media.adapter;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.media.bean.BaseMediaBean;
import com.afar.osaio.smart.media.bean.CameraPhotoMediaBean;
import com.afar.osaio.smart.media.listener.CameraPhotoGridListener;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.time.DateTimeUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class CameraPhotoGridAdapter extends BaseAdapter<CameraPhotoMediaBean, CameraPhotoGridListener, CameraPhotoGridAdapter.CameraPhotoGridVH> {

    @Override
    public CameraPhotoGridVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CameraPhotoGridVH(createVHView(R.layout.item_camera_photo_grid, parent));
    }

    @Override
    public void onBindViewHolder(CameraPhotoGridVH holder, int position) {
        CameraPhotoMediaBean data = getDataByPosition(position);
        if (data == null) {
            return;
        }
        if (data.getType() == BaseMediaBean.TYPE.IMAGE) {
            //CameraPhotoMediaBean photoMediaBean = (CameraPhotoMediaBean)data;
            Glide.with(NooieApplication.mCtx)
                    .load(data.getThumbnailPath())
                    .apply(new RequestOptions()
                            .dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 5))))
                            .format(DecodeFormat.PREFER_RGB_565)
                            .placeholder(R.drawable.default_preview_thumbnail)
                    )
                    .transition(withCrossFade())
                    .into(holder.ivCameraPhotoGirdThumb);
            holder.tvCameraPhotoGirdTime.setText(DateTimeUtil.formatDate(NooieApplication.mCtx, data.getTime(), DateTimeUtil.PATTERN_HMS));
            holder.vCameraPhotoGridContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(data);
                    }
                }
            });
        } else {
            holder.vCameraPhotoGridContainer.setOnClickListener(null);
        }
    }

    public static final class CameraPhotoGridVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vCameraPhotoGridContainer)
        View vCameraPhotoGridContainer;
        @BindView(R.id.ivCameraPhotoGirdThumb)
        ImageView ivCameraPhotoGirdThumb;
        @BindView(R.id.tvCameraPhotoGirdTime)
        TextView tvCameraPhotoGirdTime;

        public CameraPhotoGridVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
