package com.afar.osaio.smart.media.adapter;

import android.graphics.Bitmap;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.media.bean.BaseMediaBean;
import com.afar.osaio.smart.media.bean.ImageMediaBean;
import com.afar.osaio.smart.media.bean.VideoMediaBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class MediaGridAdapter extends RecyclerView.Adapter<MediaGridAdapter.MediaGridViewHolder> {

    List<BaseMediaBean> mDataList = new ArrayList<>();
    private boolean mIsEdited = false;
    private MediaGridListener mListener;

    @Override
    public MediaGridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_media_grid, parent, false);
        return new MediaGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MediaGridViewHolder holder, int position) {
        if (mDataList.get(position) == null) {
            return;
        }

        if (mDataList.get(position).getType() == BaseMediaBean.TYPE.IMAGE) {
            final ImageMediaBean imageMediaBean = (ImageMediaBean)mDataList.get(position);
            Glide.with(NooieApplication.mCtx)
                    .load(imageMediaBean.getPath())
                    .apply(new RequestOptions()
                                    .dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 2))))
                                    .format(DecodeFormat.PREFER_RGB_565)
                    )
                    .transition(withCrossFade())
                    .into(holder.ivMediaThumb);
            holder.ivMediaVideoIcon.setVisibility(View.GONE);
            holder.btnMediaSelect.setImageResource(imageMediaBean.isSelected() ? R.drawable.edit_selected_icon : R.drawable.edit_select_icon);
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = holder.getAdapterPosition();
                    if (mIsEdited) {
                        ((ImageMediaBean)mDataList.get(id)).setSelected(!((ImageMediaBean)mDataList.get(id)).isSelected());
                        holder.btnMediaSelect.setImageResource(((ImageMediaBean)mDataList.get(id)).isSelected() ? R.drawable.edit_selected_icon : R.drawable.edit_select_icon);
                        if (mListener != null) {
                            mListener.onSelected();
                        }
                    } else if (mListener != null) {
                        mListener.onItemClick(mDataList.get(id));
                    }
                }
            });
        } else if (mDataList.get(position).getType() == BaseMediaBean.TYPE.VIDEO) {
            VideoMediaBean videoMediaBean = (VideoMediaBean)mDataList.get(position);
            Glide.with(NooieApplication.mCtx)
                    .load(Uri.fromFile(new File(videoMediaBean.getPath())))
                    .apply(new RequestOptions()
                            .dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 2))))
                            .format(DecodeFormat.PREFER_RGB_565)
                    )
                    .transition(withCrossFade())
                    .into(holder.ivMediaThumb);
            holder.ivMediaVideoIcon.setVisibility(View.VISIBLE);
            holder.btnMediaSelect.setImageResource(videoMediaBean.isSelected() ? R.drawable.edit_selected_icon : R.drawable.edit_select_icon);
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = holder.getAdapterPosition();

                    if (mIsEdited) {
                        ((VideoMediaBean)mDataList.get(id)).setSelected(!((VideoMediaBean)mDataList.get(id)).isSelected());
                        holder.btnMediaSelect.setImageResource(((VideoMediaBean)mDataList.get(id)).isSelected() ? R.drawable.edit_selected_icon : R.drawable.edit_select_icon);
                        if (mListener != null) {
                            mListener.onSelected();
                        }
                    } else if (mListener != null) {
                        mListener.onItemClick(mDataList.get(id));
                    }
                }
            });
        }
        holder.btnMediaSelect.setVisibility(mIsEdited ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.safeFor(mDataList).size();
    }

    public void setDataList(List<BaseMediaBean> dataList) {
        if (mDataList == null) {
            mDataList = new ArrayList<>();
        }
        mDataList.clear();
        mDataList.addAll(CollectionUtil.safeFor(dataList));
        notifyDataSetChanged();
    }

    public void setIsEdited(boolean isEdited) {
        mIsEdited = isEdited;
    }

    public void setListener(MediaGridListener listener) {
        mListener = listener;
    }

    public interface MediaGridListener {
        void onSelected();

        void onItemClick(BaseMediaBean mediaBean);
    }

    public class MediaGridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivMediaThumb)
        ImageView ivMediaThumb;
        @BindView(R.id.ivMediaVideoIcon)
        ImageView ivMediaVideoIcon;
        @BindView(R.id.btnMediaSelect)
        ImageView btnMediaSelect;
        View container;

        public MediaGridViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }
}
