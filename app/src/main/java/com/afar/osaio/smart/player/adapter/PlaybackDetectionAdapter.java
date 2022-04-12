package com.afar.osaio.smart.player.adapter;

import android.graphics.Bitmap;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.afar.osaio.smart.device.helper.NooieCloudHelper;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.common.widget.RoundedImageView.RoundedImageView;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.bean.CloudFileBean;
import com.afar.osaio.util.ConstantValue;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class PlaybackDetectionAdapter extends RecyclerView.Adapter<PlaybackDetectionAdapter.PlaybackDetectionViewHolder> {

    private List<CloudFileBean> mDataList = new ArrayList<>();
    private PlaybackDetectionListener mListener;
    private int mDisplayType = ConstantValue.PLAY_DISPLAY_TYPE_NORMAL;

    @Override
    public PlaybackDetectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_playback_detection, parent, false);
        return new PlaybackDetectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaybackDetectionViewHolder holder, int position) {
        final CloudFileBean cloudFileBean = mDataList.get(position);

        holder.tvDetectionType.setVisibility(mDisplayType == ConstantValue.PLAY_DISPLAY_TYPE_DETAIL ? View.VISIBLE : View.GONE);
        //holder.tvDetectionTimeLen.setVisibility(mDisplayType == ConstantValue.PLAY_DISPLAY_TYPE_DETAIL ? View.VISIBLE : View.GONE);
        holder.tvDetectionTime.setVisibility(mDisplayType == ConstantValue.PLAY_DISPLAY_TYPE_DETAIL ? View.VISIBLE : View.GONE);

        if (NooieCloudHelper.isDetectionAvailable(cloudFileBean.getMotionDetectionTime())) {
            holder.tvDetectionType.setText(R.string.camera_settings_motion);
            /*
            StringBuilder timeLenSb = new StringBuilder();
            timeLenSb.append(cloudFileBean.getMotionDetectionTime());
            timeLenSb.append("s");
            holder.tvDetectionTimeLen.setText(timeLenSb.toString());
            */
        } else if (NooieCloudHelper.isDetectionAvailable(cloudFileBean.getSoundDetectionTime())) {
            holder.tvDetectionType.setText(R.string.camera_settings_sound);
            /*
            StringBuilder timeLenSb = new StringBuilder();
            timeLenSb.append(cloudFileBean.getSoundDetectionTime());
            timeLenSb.append("s");
            holder.tvDetectionTimeLen.setText(timeLenSb.toString());
            */
        } else if (NooieCloudHelper.isDetectionAvailable(cloudFileBean.getPirDetectionTime())) {
            holder.tvDetectionType.setText(R.string.camera_settings_pir);
            /*
            StringBuilder timeLenSb = new StringBuilder();
            timeLenSb.append(cloudFileBean.getPirDetectionTime());
            timeLenSb.append("s");
            holder.tvDetectionTimeLen.setText(timeLenSb.toString());
            */
        }
        holder.tvDetectionTime.setText(DateTimeUtil.getTimeHms(cloudFileBean.getStartTime()));

        /*
        if (!new File(cloudFileBean.getFileUrl()).exists()) {
            NooieLog.d("-->> PlaybackDetectionAdapter onBindViewHolder file=" + cloudFileBean.getFileUrl() + " preurl=" + cloudFileBean.getPreSignUrl());
        }
        */
        Glide.with(NooieApplication.mCtx)
                .load(cloudFileBean.getFileUrl())
                .apply(new RequestOptions()
                        .dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 5))))
                        .placeholder(R.drawable.default_preview_thumbnail)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .error(R.drawable.default_preview_thumbnail)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                )
                .transition(withCrossFade())
                .into(holder.ivDetectionThumbnail);

        holder.containerPlaybackDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClickListener(cloudFileBean);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return CollectionUtil.safeFor(mDataList).size();
    }

    public void setDataList(List<CloudFileBean> cloudFileBeans) {
        if (mDataList == null) {
            mDataList = new ArrayList<>();
        }

        mDataList.clear();
        mDataList.addAll(CollectionUtil.safeFor(cloudFileBeans));
        notifyDataSetChanged();
    }

    public void setListener(PlaybackDetectionListener listener) {
        mListener = listener;
    }

    public void setDisplayType(int displayType) {
        mDisplayType = displayType;
        notifyDataSetChanged();
    }

    public int getDisplayType() {
        return mDisplayType;
    }

    public void release() {
        if (mDataList != null) {
            mDataList.clear();
            mDataList = null;
        }
        mListener = null;
    }

    public class PlaybackDetectionViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivDetectionThumbnail)
        RoundedImageView ivDetectionThumbnail;
        @BindView(R.id.tvDetectionType)
        TextView tvDetectionType;
        @BindView(R.id.tvDetectionTimeLen)
        TextView tvDetectionTimeLen;
        @BindView(R.id.tvDetectionTime)
        TextView tvDetectionTime;
        @BindView(R.id.containerPlaybackDetection)
        View containerPlaybackDetection;

        public PlaybackDetectionViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface PlaybackDetectionListener {

        void onItemClickListener(CloudFileBean cloudFileBean);
    }
}
