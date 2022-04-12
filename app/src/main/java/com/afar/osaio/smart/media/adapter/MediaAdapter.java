package com.afar.osaio.smart.media.adapter;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.time.DateTimeUtil;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.media.bean.BaseMediaBean;
import com.afar.osaio.smart.media.bean.MediaItemBean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private List<MediaItemBean> mDataList = new ArrayList<>();
    private boolean mIsEdited = false;
    private MediaListener mListener;

    @Override
    public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MediaViewHolder holder, int position) {
        MediaItemBean mediaItem = mDataList.get(position);
        StringBuilder dateSb = new StringBuilder();
        dateSb.append(DateTimeUtil.formatDate(NooieApplication.mCtx, mediaItem.getDate(), DateTimeUtil.PATTERN_YMD));
        dateSb.append(" ");
        dateSb.append(DateTimeUtil.getUtcDisplayName(NooieApplication.mCtx, mediaItem.getDate(), Calendar.DAY_OF_WEEK, Calendar.LONG));
        holder.tvMediaDateTitle.setText(dateSb.toString());
        //holder.tvMediaTimeTitle.setText(DateTimeUtil.formatDate(NooieApplication.mCtx, mediaItem.getDate(), DateTimeUtil.PATTERN_HM));
        holder.tvMediaTimeTitle.setText(getHourTextByTime(mediaItem.getDate()));
        holder.rcvMediaThumbs.setLayoutManager(new GridLayoutManager(NooieApplication.mCtx, 3));
        MediaGridAdapter mediaGridAdapter = new MediaGridAdapter();
        mediaGridAdapter.setListener(new MediaGridAdapter.MediaGridListener() {
            @Override
            public void onSelected() {
                if (mListener != null) {
                    mListener.onDataChange();
                }
            }

            @Override
            public void onItemClick(BaseMediaBean mediaBean) {
                if (mListener != null) {
                    mListener.onItemClick(mediaBean);
                }
            }
        });
        mediaGridAdapter.setIsEdited(mIsEdited);
        mediaGridAdapter.setDataList(mediaItem.getMedias());
        holder.rcvMediaThumbs.setAdapter(mediaGridAdapter);
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.safeFor(mDataList).size();
    }

    public void setDataList(List<MediaItemBean> dataList) {
        if (mDataList == null) {
            mDataList = new ArrayList<>();
        }
        mDataList.clear();
        mDataList.addAll(CollectionUtil.safeFor(dataList));
        notifyDataSetChanged();
    }

    public void setIsEdited(boolean isEdited) {
        mIsEdited = isEdited;
        notifyDataSetChanged();
    }

    public void setListener(MediaListener listener) {
        mListener = listener;
    }

    private String getHourTextByTime(long time) {
        StringBuilder sb = new StringBuilder();
        sb.append(DateTimeUtil.formatDate(NooieApplication.mCtx, time, "HH"));
        sb.append(":00");
        return sb.toString();
    }

    public interface MediaListener {
        void onDataChange();

        void onItemClick(BaseMediaBean mediaBean);
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvMediaDateTitle)
        TextView tvMediaDateTitle;
        @BindView(R.id.tvMediaTimeTitle)
        TextView tvMediaTimeTitle;
        @BindView(R.id.rcvMediaThumbs)
        RecyclerView rcvMediaThumbs;

        public MediaViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
