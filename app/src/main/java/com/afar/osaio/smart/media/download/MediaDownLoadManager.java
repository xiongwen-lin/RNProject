package com.afar.osaio.smart.media.download;

import androidx.annotation.NonNull;

import com.afar.osaio.smart.media.bean.MediaDownloadBean;

import java.util.List;

public class MediaDownLoadManager implements MediaDownloadManagerContract.View {

    private static final class MediaDownLoadManagerHolder {
        public static final MediaDownLoadManager INSTANCE = new MediaDownLoadManager();
    }

    public static MediaDownLoadManager getInstance() {
        return MediaDownLoadManagerHolder.INSTANCE;
    }

    private MediaDownloadManagerContract.Presenter mPresenter;
    private MediaDownloadManagerContract.MediaDownloadManagerListener mListener;

    private MediaDownLoadManager() {
        new MediaDownloadManagerPresenter(this);
    }

    @Override
    public void setPresenter(@NonNull MediaDownloadManagerContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onMediaDownloadResult(int state, MediaDownloadBean downloadBean) {
        if (mListener != null) {
            mListener.onMediaDownloadResult(state, downloadBean);
        }
    }

    public void startDownloadMediaFileTask(List<MediaDownloadBean> mediaDownloadBeanList) {
        if (mPresenter != null) {
            mPresenter.startDownloadMediaFileTask(mediaDownloadBeanList);
        }
    }

    public void setListener(MediaDownloadManagerContract.MediaDownloadManagerListener listener) {
        this.mListener = listener;
    }

}
