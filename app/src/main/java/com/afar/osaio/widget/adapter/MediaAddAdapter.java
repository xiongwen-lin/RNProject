package com.afar.osaio.widget.adapter;

import android.content.Context;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afar.osaio.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MediaAddAdapter extends RecyclerView.Adapter<MediaAddAdapter.MediaViewHolder> {

    public static final int ITEM_ADD = 1;
    public static final int MAX_ADD = 3;
    private Context mContext;
    private List<MediaInfo> mMediaInfos = new ArrayList<>();
    private MediaAddListener mListener;

    public MediaAddAdapter(Context context) {
        mContext = context;
    }

    public void init() {
        MediaInfo mediaInfo = buildMediaInfo();
        mediaInfo.setName("Last");
        mediaInfo.setPath("");
        mMediaInfos.clear();
        mMediaInfos.add(mediaInfo);
    }

    @Override
    public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_media_add, parent, false);
        return new MediaViewHolder(viewType, view);
    }

    @Override
    public void onBindViewHolder(MediaViewHolder holder, int position) {
        if (mMediaInfos == null) {
            return;
        }
        final int id = position;
        final MediaInfo mediaInfo = mMediaInfos.get(id);
        if (holder.viewType != ITEM_ADD) {
            holder.ivMediaDeleteIcon.setVisibility(View.VISIBLE);
            holder.ivMediaAddIcon.setVisibility(View.GONE);
            holder.ivMediaDeleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onMediaDelete(mediaInfo.getPath());
                    removeData(id);
                }
            });
            Glide.with(mContext).load(mediaInfo.getPath()).apply(new RequestOptions().centerCrop()).into(holder.ivMediaIcon);
        } else {
            if (mMediaInfos.size() < MAX_ADD + 1) {
                holder.container.setVisibility(View.VISIBLE);
            } else {
                holder.container.setVisibility(View.GONE);
            }
            holder.ivMediaDeleteIcon.setVisibility(View.GONE);
            holder.ivMediaAddIcon.setVisibility(View.VISIBLE);
            holder.ivMediaAddIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onMediaAdd();
                    }
                }
            });
            holder.ivMediaIcon.setImageDrawable(null);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mMediaInfos.size() > 0 && position == mMediaInfos.size() - 1 ? ITEM_ADD : 0;
    }

    @Override
    public int getItemCount() {
        return mMediaInfos.size();
    }

    public void setData(List<MediaInfo> mediaInfos) {
        if (mMediaInfos == null || mediaInfos == null) {
            return;
        }
        mMediaInfos.clear();
        mMediaInfos.addAll(mediaInfos);
    }

    public void addData(MediaInfo mediaInfo) {
        if (mMediaInfos == null || (mMediaInfos.size() > MAX_ADD) || mediaInfo == null) {
            return;
        }
        if (mMediaInfos.size() == 0) {
            init();
        }
        mMediaInfos.add(mMediaInfos.size() - 1, mediaInfo);
        notifyDataSetChanged();
    }

    public void removeData(int index) {
        if (mMediaInfos == null || index >= mMediaInfos.size() - 1) {
            return;
        }
        mMediaInfos.remove(index);
        if (mMediaInfos.size() == 1) {
            mMediaInfos.clear();
        }
        notifyDataSetChanged();
    }

    public void setListener(MediaAddListener listener) {
        mListener = listener;
    }

    public static MediaInfo buildMediaInfo() {
        return new MediaInfo();
    }

    public static GridLayoutManager createGridLayoutManager(Context context, int column) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, column);
        return gridLayoutManager;
    }

    public static class MediaViewHolder extends RecyclerView.ViewHolder {
        int viewType;
        @BindView(R.id.ivMediaIcon)
        ImageView ivMediaIcon;
        @BindView(R.id.ivMediaAddIcon)
        ImageView ivMediaAddIcon;
        @BindView(R.id.ivMediaDeleteIcon)
        ImageView ivMediaDeleteIcon;
        View container;
        public MediaViewHolder(int viewType, View view) {
            super(view);
            container = view;
            this.viewType = viewType;
            ButterKnife.bind(this, view);
        }
    }

    public static class MediaInfo {
        private String name;
        private String path;
        private int resId;

        public MediaInfo() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public int getResId() {
            return resId;
        }

        public void setResId(int resId) {
            this.resId = resId;
        }
    }

    public interface MediaAddListener {
        void onMediaAdd();
        void onMediaDelete(String path);
    }
}
