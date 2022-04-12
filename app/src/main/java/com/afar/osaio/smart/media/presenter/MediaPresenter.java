package com.afar.osaio.smart.media.presenter;

import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.nooie.common.utils.collection.CollectionUtil;
import com.afar.osaio.smart.media.bean.BaseMediaBean;
import com.afar.osaio.smart.media.bean.ImageMediaBean;
import com.afar.osaio.smart.media.bean.MediaItemBean;
import com.afar.osaio.smart.media.bean.VideoMediaBean;
import com.afar.osaio.smart.media.view.IMediaView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.log.NooieLog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MediaPresenter implements IMediaPresenter {

    private IMediaView mMediaView;
    private List<MediaItemBean> mImageMediaItemList = new ArrayList<>();
    private List<MediaItemBean> mVideoMediaItemList = new ArrayList<>();

    public MediaPresenter(IMediaView view) {
        mMediaView = view;
    }

    @Override
    public void detachView() {
        if (mMediaView != null) {
            mMediaView = null;
        }

        if (mImageMediaItemList != null) {
            mImageMediaItemList.clear();
            mImageMediaItemList =null;
        }

        if (mVideoMediaItemList != null) {
            mVideoMediaItemList.clear();
            mVideoMediaItemList =null;
        }
    }

    @Override
    public void loadAlbum(String account, final String deviceId, final BaseMediaBean.TYPE type) {
        Observable.just(account)
                .flatMap(new Func1<String, Observable<List<MediaItemBean>>>() {
                    @Override
                    public Observable<List<MediaItemBean>> call(String account) {
                        if (type == BaseMediaBean.TYPE.IMAGE && CollectionUtil.isNotEmpty(mImageMediaItemList)) {
                            return Observable.just(mImageMediaItemList);
                        } else if (type == BaseMediaBean.TYPE.VIDEO && CollectionUtil.isNotEmpty(mVideoMediaItemList)) {
                            return Observable.just(mVideoMediaItemList);
                        }

                        List<MediaItemBean> result = type == BaseMediaBean.TYPE.IMAGE ? mImageMediaItemList : mVideoMediaItemList;
                        try {
                            File snapFolder = type == BaseMediaBean.TYPE.IMAGE ? FileUtil.getSnapshotDir(NooieApplication.mCtx, account) : FileUtil.getRecordVideoDir(NooieApplication.mCtx, account);
                            if (snapFolder != null && snapFolder.isDirectory()) {
                                File[] snapFiles = snapFolder.listFiles();
                                if (snapFiles != null && snapFiles.length > 0) {
                                    List<File> mediaFiles = new ArrayList<>();
                                    for (File snapFile : snapFiles) {
                                        NooieLog.d("-->> MediaPresenter loadAlbum path=" + snapFile.getPath() + " size=" + snapFile.length());
                                        if (TextUtils.isEmpty(deviceId) && snapFile != null && snapFile.length() > 0) {
                                            mediaFiles.add(snapFile);
                                        } else if (snapFile != null && snapFile.length() > 0 && !TextUtils.isEmpty(snapFile.getName()) && snapFile.getName().contains(deviceId)) {
                                            mediaFiles.add(snapFile);
                                        }
                                    }

                                    result.addAll(CollectionUtil.safeFor(getMediaItems(mediaFiles, type)));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return Observable.just(result);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<MediaItemBean>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(List<MediaItemBean> result) {
                        for (MediaItemBean mediaItemBean : CollectionUtil.safeFor(result)) {
                            NooieLog.d("-->> MediaPresenter loadAlbum mediaItem date=" + mediaItemBean.getDate() + " size=" + mediaItemBean.getMedias().size());
                        }
                        if (mMediaView != null) {
                            mMediaView.onLoadAlbumResult(ConstantValue.SUCCESS, type, result);
                        }
                    }
                });
    }

    private List<MediaItemBean> getMediaItems(List<File> mediaFiles, BaseMediaBean.TYPE type) {
        List<MediaItemBean> result = new ArrayList<>();

        List<BaseMediaBean> mediaBeans = new ArrayList<>();
        for (File mediaFile : CollectionUtil.safeFor(mediaFiles)) {
            //NooieLog.d("-->> MediaPresenter getMediaItems origin media path=" + mediaFile.getPath() + " lastM=" + mediaFile.lastModified());
            long mediaTime = formatTimeByMediaFileName(mediaFile.getName());
            if (mediaTime > 0) {
                if (type == BaseMediaBean.TYPE.IMAGE) {
                    ImageMediaBean imageMediaBean = new ImageMediaBean();
                    imageMediaBean.setPath(mediaFile.getPath());
                    imageMediaBean.setTime(mediaTime);
                    imageMediaBean.setId(Long.valueOf((mediaTime / 1000) / 3600).toString());
                    mediaBeans.add(imageMediaBean);
                } else if (type == BaseMediaBean.TYPE.VIDEO) {
                    VideoMediaBean videoMediaBean = new VideoMediaBean();
                    videoMediaBean.setPath(mediaFile.getPath());
                    videoMediaBean.setTime(mediaTime);
                    videoMediaBean.setId(Long.valueOf((mediaTime / 1000) / 3600).toString());
                    mediaBeans.add(videoMediaBean);
                }
            }
        }

        if (CollectionUtil.isEmpty(mediaBeans)) {
            return result;
        }

        Collections.sort(mediaBeans, new Comparator<BaseMediaBean>() {
            @Override
            public int compare(BaseMediaBean first, BaseMediaBean second) {
                long firstTime = first.getTime();
                long secondTime = second.getTime();
                return firstTime > secondTime ? -1 : 1;
            }
        });

        BaseMediaBean targetMediaBean = null;
        List<BaseMediaBean> mediaBeansOfDay = null;
        List<List<BaseMediaBean>> mediaBeansOfDayList = new ArrayList<>();

        for (int i = 0; i < mediaBeans.size(); i++) {
            BaseMediaBean mediaBean = mediaBeans.get(i);
            NooieLog.d("-->> MediaPresenter getMediaItems media path=" + mediaBean.getPath() + " time=" + mediaBean.getTime());
            if (targetMediaBean == null) {
                targetMediaBean = mediaBean;
                mediaBeansOfDay = new ArrayList<>();
                mediaBeansOfDay.add(mediaBean);
                if (i == mediaBeans.size() - 1) {
                    mediaBeansOfDayList.add(mediaBeansOfDay);
                }
            } else {
                if (targetMediaBean.getId().equalsIgnoreCase(mediaBean.getId())) {
                    mediaBeansOfDay.add(mediaBean);
                    if (i == mediaBeans.size() - 1) {
                        mediaBeansOfDayList.add(mediaBeansOfDay);
                    }
                } else {
                    mediaBeansOfDayList.add(mediaBeansOfDay);
                    targetMediaBean = mediaBean;
                    mediaBeansOfDay = new ArrayList<>();
                    mediaBeansOfDay.add(mediaBean);
                    if (i == mediaBeans.size() - 1) {
                        mediaBeansOfDayList.add(mediaBeansOfDay);
                    }
                }
            }
        }

        for (List<BaseMediaBean> mediaBeanList : CollectionUtil.safeFor(mediaBeansOfDayList)) {
            if (CollectionUtil.isNotEmpty(mediaBeanList)) {
                MediaItemBean mediaItemBean = new MediaItemBean();
                mediaItemBean.setMedias(mediaBeanList);
                result.add(mediaItemBean);
            }
        }

        return result;
    }

    private long formatTimeByMediaFileName(String name) {
        long time = 0L;
        if (TextUtils.isEmpty(name)) {
            return time;
        }
        try {
            if (name.contains(ConstantValue.AP_FUTURE_CODE_PREFIX_VICTURE)) {
                name = name.replace(ConstantValue.AP_FUTURE_CODE_PREFIX_VICTURE, ConstantValue.AP_FUTURE_CODE_PREFIX_VICTURE_REPLACE_TAG);
            }
            if (name.contains(ConstantValue.AP_FUTURE_CODE_PREFIX_GNCC_FILE_TAG)) {
                name = name.replace(ConstantValue.AP_FUTURE_CODE_PREFIX_GNCC_FILE_TAG, ConstantValue.AP_FUTURE_CODE_PREFIX_GNCC_REPLACE_TAG);
            }
            String dateStr = name.substring(name.indexOf("_") + 1, name.indexOf("."));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            time = dateFormat.parse(dateStr).getTime();
            NooieLog.d("-->> MediaPresenter formatTimeByMediaFileName dateStr=" + dateStr + " timeStamp=" + time + " hour=" + (time/1000) / 3600);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return time;
    }

    @Override
    public void removeMedia(final String account, final String deviceId, final BaseMediaBean.TYPE type) {
        Observable.just("")
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String s) {
                        removeMedia(type);
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
                        //loadAlbum(account, deviceId, type);
                        if (mMediaView != null) {
                            mMediaView.onRemoveAlbumResult(ConstantValue.SUCCESS);
                        }
                    }
                });
    }

    private void removeMedia(BaseMediaBean.TYPE type) {
        if (type == BaseMediaBean.TYPE.VIDEO && CollectionUtil.isNotEmpty(mVideoMediaItemList)) {
            Iterator<MediaItemBean> mediaItemBeanIterator = mVideoMediaItemList.iterator();
            while (mediaItemBeanIterator.hasNext()) {
                MediaItemBean mediaItemBean = mediaItemBeanIterator.next();
                Iterator<BaseMediaBean> baseMediaBeanIterator = mediaItemBean.getMedias().iterator();
                while (baseMediaBeanIterator.hasNext()) {
                    VideoMediaBean videoMediaBean = (VideoMediaBean)baseMediaBeanIterator.next();
                    NooieLog.d("-->> MediaPresenter removeMedia video select=" + videoMediaBean.isSelected());
                    if (videoMediaBean.isSelected()) {
                        baseMediaBeanIterator.remove();
                        FileUtil.deleteFile(videoMediaBean.getPath());
                    };
                }

                if (CollectionUtil.isEmpty(mediaItemBean.getMedias())) {
                    mediaItemBeanIterator.remove();
                }
            }
        } else if (type == BaseMediaBean.TYPE.IMAGE && CollectionUtil.isNotEmpty(mImageMediaItemList)) {
            Iterator<MediaItemBean> mediaItemBeanIterator = mImageMediaItemList.iterator();
            while (mediaItemBeanIterator.hasNext()) {
                MediaItemBean mediaItemBean = mediaItemBeanIterator.next();
                Iterator<BaseMediaBean> baseMediaBeanIterator = mediaItemBean.getMedias().iterator();
                while (baseMediaBeanIterator.hasNext()) {
                    ImageMediaBean imageMediaBean = (ImageMediaBean) baseMediaBeanIterator.next();
                    NooieLog.d("-->> MediaPresenter removeMedia image select=" + imageMediaBean.isSelected());
                    if (imageMediaBean.isSelected()) {
                        baseMediaBeanIterator.remove();
                        FileUtil.deleteFile(imageMediaBean.getPath());
                    };
                }

                if (CollectionUtil.isEmpty(mediaItemBean.getMedias())) {
                    mediaItemBeanIterator.remove();
                }
            }
        }
    }

    @Override
    public void removeMediaByPath(final String account, final String deviceId, final BaseMediaBean.TYPE type, final String path) {
        Observable.just("")
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String s) {
                        removeMedia(type, path);
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
                        loadAlbum(account, deviceId, type);
                    }
                });
    }

    private void removeMedia(BaseMediaBean.TYPE type, String path) {
        if (type == BaseMediaBean.TYPE.VIDEO && CollectionUtil.isNotEmpty(mVideoMediaItemList)) {
            Iterator<MediaItemBean> mediaItemBeanIterator = mVideoMediaItemList.iterator();
            while (mediaItemBeanIterator.hasNext()) {
                MediaItemBean mediaItemBean = mediaItemBeanIterator.next();
                Iterator<BaseMediaBean> baseMediaBeanIterator = mediaItemBean.getMedias().iterator();
                while (baseMediaBeanIterator.hasNext()) {
                    VideoMediaBean videoMediaBean = (VideoMediaBean)baseMediaBeanIterator.next();
                    NooieLog.d("-->> MediaPresenter removeMedia video select=" + videoMediaBean.isSelected());
                    if (path.equalsIgnoreCase(videoMediaBean.getPath())) {
                        baseMediaBeanIterator.remove();
                        FileUtil.deleteFile(videoMediaBean.getPath());
                    };
                }

                if (CollectionUtil.isEmpty(mediaItemBean.getMedias())) {
                    mediaItemBeanIterator.remove();
                }
            }
        } else if (type == BaseMediaBean.TYPE.IMAGE && CollectionUtil.isNotEmpty(mImageMediaItemList)) {
            Iterator<MediaItemBean> mediaItemBeanIterator = mImageMediaItemList.iterator();
            while (mediaItemBeanIterator.hasNext()) {
                MediaItemBean mediaItemBean = mediaItemBeanIterator.next();
                Iterator<BaseMediaBean> baseMediaBeanIterator = mediaItemBean.getMedias().iterator();
                while (baseMediaBeanIterator.hasNext()) {
                    ImageMediaBean imageMediaBean = (ImageMediaBean) baseMediaBeanIterator.next();
                    NooieLog.d("-->> MediaPresenter removeMedia image select=" + imageMediaBean.isSelected());
                    if (path.equalsIgnoreCase(imageMediaBean.getPath())) {
                        baseMediaBeanIterator.remove();
                        FileUtil.deleteFile(imageMediaBean.getPath());
                    };
                }

                if (CollectionUtil.isEmpty(mediaItemBean.getMedias())) {
                    mediaItemBeanIterator.remove();
                }
            }
        }
    }

    @Override
    public void resetMedia(String account, String deviceId, BaseMediaBean.TYPE type, boolean isSelected) {
        if (type == BaseMediaBean.TYPE.VIDEO && CollectionUtil.isNotEmpty(mVideoMediaItemList)) {
            Iterator<MediaItemBean> mediaItemBeanIterator = mVideoMediaItemList.iterator();
            while (mediaItemBeanIterator.hasNext()) {
                MediaItemBean mediaItemBean = mediaItemBeanIterator.next();
                Iterator<BaseMediaBean> baseMediaBeanIterator = mediaItemBean.getMedias().iterator();
                while (baseMediaBeanIterator.hasNext()) {
                    VideoMediaBean videoMediaBean = (VideoMediaBean)baseMediaBeanIterator.next();
                    NooieLog.d("-->> MediaPresenter removeMedia video select=" + videoMediaBean.isSelected());
                    videoMediaBean.setSelected(isSelected);
                }
            }
        } else if (type == BaseMediaBean.TYPE.IMAGE && CollectionUtil.isNotEmpty(mImageMediaItemList)) {
            Iterator<MediaItemBean> mediaItemBeanIterator = mImageMediaItemList.iterator();
            while (mediaItemBeanIterator.hasNext()) {
                MediaItemBean mediaItemBean = mediaItemBeanIterator.next();
                Iterator<BaseMediaBean> baseMediaBeanIterator = mediaItemBean.getMedias().iterator();
                while (baseMediaBeanIterator.hasNext()) {
                    ImageMediaBean imageMediaBean = (ImageMediaBean) baseMediaBeanIterator.next();
                    NooieLog.d("-->> MediaPresenter removeMedia image select=" + imageMediaBean.isSelected());
                    imageMediaBean.setSelected(isSelected);
                }
            }
        }
    }

    @Override
    public void getSelectedMedia(BaseMediaBean.TYPE type) {
        int selectedMediaCount = 0;
        if (type == BaseMediaBean.TYPE.VIDEO && CollectionUtil.isNotEmpty(mVideoMediaItemList)) {
            Iterator<MediaItemBean> mediaItemBeanIterator = mVideoMediaItemList.iterator();
            while (mediaItemBeanIterator.hasNext()) {
                MediaItemBean mediaItemBean = mediaItemBeanIterator.next();
                Iterator<BaseMediaBean> baseMediaBeanIterator = mediaItemBean.getMedias().iterator();
                while (baseMediaBeanIterator.hasNext()) {
                    VideoMediaBean videoMediaBean = (VideoMediaBean)baseMediaBeanIterator.next();
                    NooieLog.d("-->> MediaPresenter removeMedia video select=" + videoMediaBean.isSelected());
                    if (videoMediaBean.isSelected()) {
                        selectedMediaCount++;
                    }
                }
            }
        } else if (type == BaseMediaBean.TYPE.IMAGE && CollectionUtil.isNotEmpty(mImageMediaItemList)) {
            Iterator<MediaItemBean> mediaItemBeanIterator = mImageMediaItemList.iterator();
            while (mediaItemBeanIterator.hasNext()) {
                MediaItemBean mediaItemBean = mediaItemBeanIterator.next();
                Iterator<BaseMediaBean> baseMediaBeanIterator = mediaItemBean.getMedias().iterator();
                while (baseMediaBeanIterator.hasNext()) {
                    ImageMediaBean imageMediaBean = (ImageMediaBean) baseMediaBeanIterator.next();
                    NooieLog.d("-->> MediaPresenter removeMedia image select=" + imageMediaBean.isSelected());
                    if (imageMediaBean.isSelected()) {
                        selectedMediaCount++;
                    }
                }
            }
        }

        if (mMediaView != null) {
            mMediaView.onGetAlbumSelectedResult(selectedMediaCount);
        }
    }
}
