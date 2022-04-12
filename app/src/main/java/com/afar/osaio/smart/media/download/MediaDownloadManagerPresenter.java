package com.afar.osaio.smart.media.download;

import android.text.TextUtils;

import com.afar.osaio.smart.media.bean.MediaDownloadBean;
import com.afar.osaio.smart.media.bean.NEMediaType;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.downloader.Downloader;
import com.nooie.common.utils.downloader.contract.DownloadContract;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.bean.SDKConstant;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MediaDownloadManagerPresenter implements MediaDownloadManagerContract.Presenter {

    private MediaDownloadManagerContract.View mTaskView;
    private Subscription mStartDownloadMediaFileTask = null;
    private List<String> mDownloadingMediaFileKeyList = new ArrayList<>();

    public MediaDownloadManagerPresenter(MediaDownloadManagerContract.View view) {
        mTaskView = view;
        mTaskView.setPresenter(this);
        initDownloadMediaFileKeyList();
    }

    @Override
    public void destroy() {
        if (mTaskView != null) {
            mTaskView.setPresenter(null);
            mTaskView = null;
        }
    }

    public void startDownloadMediaFileTask(List<MediaDownloadBean> mediaDownloadBeanList) {
        mediaDownloadBeanList = filterExistDownloadMediaFile(mediaDownloadBeanList);
        if (CollectionUtil.isEmpty(mediaDownloadBeanList)) {
            return;
        }
        mStartDownloadMediaFileTask = Observable.from(mediaDownloadBeanList)
                .flatMap(new Func1<MediaDownloadBean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(MediaDownloadBean downloadBean) {
                        try {
                            downloadMediaFile(downloadBean);
                        } catch (Exception e) {
                            NooieLog.printStackTrace(e);
                        }
                        return Observable.just(true);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Boolean result) {
                    }
                });
    }

    public void downloadMediaFile(MediaDownloadBean downloadBean) {
        if (downloadBean == null || TextUtils.isEmpty(downloadBean.getKey()) || TextUtils.isEmpty(downloadBean.getDownloadUrl()) || TextUtils.isEmpty(downloadBean.getStoragePath()) || TextUtils.isEmpty(downloadBean.getStorageFileName())) {
            return;
        }
        addDownloadMediaFileKey(downloadBean.getKey());
        Downloader.getInstance().downloadFile(downloadBean.getDownloadUrl(), downloadBean.getStoragePath(), downloadBean.getStorageFileName(), new DownloadContract.ProcessCallback() {
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
            }

            @Override
            public void onDownloadSuccess(Response<ResponseBody> response) {
            }

            @Override
            public void onDownloadFinish(boolean result) {
                transformMediaFileAsThumbnail(result, downloadBean);
                if (result && mTaskView != null) {
                    mTaskView.onMediaDownloadResult(SDKConstant.SUCCESS, downloadBean);
                }
            }

            @Override
            public void onFailure(String message) {
            }
        });
    }

    private void transformMediaFileAsThumbnail(boolean result, MediaDownloadBean downloadBean) {
        if (!result || downloadBean == null) {
            return;
        }
        removeDownloadMediaFileKey(downloadBean.getKey());
        if (downloadBean.getMediaType() == NEMediaType.IMAGE) {
        } else if (downloadBean.getMediaType() == NEMediaType.VIDEO) {
        }
    }

    private void initDownloadMediaFileKeyList() {
        if (mDownloadingMediaFileKeyList == null) {
            mDownloadingMediaFileKeyList = new ArrayList<>();
        }
    }

    private void addDownloadMediaFileKey(String key) {
        if (CollectionUtil.isEmpty(mDownloadingMediaFileKeyList) || TextUtils.isEmpty(key) || mDownloadingMediaFileKeyList.contains(key)) {
            return;
        }
        mDownloadingMediaFileKeyList.add(key);
    }

    private void removeDownloadMediaFileKey(String key) {
        if (CollectionUtil.isEmpty(mDownloadingMediaFileKeyList) || TextUtils.isEmpty(key) || !mDownloadingMediaFileKeyList.contains(key)) {
            return;
        }
        try {
            Iterator<String> keyIterator = mDownloadingMediaFileKeyList.iterator();
            while (keyIterator.hasNext()) {
                String tmpKey = keyIterator.next();
                if (key.equals(tmpKey)) {
                    keyIterator.remove();
                    break;
                }
            }
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    private boolean checkIsDownloadMediaFileExist(String key, String path) {
        try {
            return (!TextUtils.isEmpty(key) && CollectionUtil.isNotEmpty(mDownloadingMediaFileKeyList) && mDownloadingMediaFileKeyList.contains(key))
                    || (!TextUtils.isEmpty(path) && (new File(path).exists()));
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return false;
    }

    private List<MediaDownloadBean> filterExistDownloadMediaFile(List<MediaDownloadBean> downloadBeanList) {
        if (CollectionUtil.isEmpty(downloadBeanList)) {
            return downloadBeanList;
        }
        StringBuilder keySb = new StringBuilder();
        StringBuilder pathSb = new StringBuilder();
        Iterator<MediaDownloadBean> downloadBeanIterable = downloadBeanList.iterator();
        while (downloadBeanIterable.hasNext()) {
            keySb.setLength(0);
            pathSb.setLength(0);
            MediaDownloadBean downloadBean = downloadBeanIterable.next();
            if (downloadBean != null && checkIsDownloadMediaFileExist(downloadBean.getKey(), pathSb.toString())) {
                downloadBeanIterable.remove();
            }
        }
        return downloadBeanList;
    }
}
