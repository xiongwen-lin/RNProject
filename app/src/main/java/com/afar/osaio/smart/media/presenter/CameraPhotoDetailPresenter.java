package com.afar.osaio.smart.media.presenter;

import android.os.Build;
import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.media.contract.CameraPhotoDetailContract;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.file.MediaStoreUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.bean.SDKConstant;

import java.io.File;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class CameraPhotoDetailPresenter implements CameraPhotoDetailContract.Presenter {

    private CameraPhotoDetailContract.View mTaskView;

    public CameraPhotoDetailPresenter(CameraPhotoDetailContract.View view) {
        mTaskView = view;
        mTaskView.setPresenter(this);
    }

    @Override
    public void destroy() {
        if (mTaskView != null) {
            mTaskView.setPresenter(null);
            mTaskView = null;
        }
    }

    @Override
    public void updateFileToMediaStore(String account, String deviceId, String path, String mediaType) {
        Observable.just(mediaType)
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String type) {
                        try {
                            if (TextUtils.isEmpty(type) || !new File(path).exists()) {
                                return Observable.just("");
                            }
                            StringBuilder relativeSubFolderSb = new StringBuilder();
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                relativeSubFolderSb.append(FileUtil.getLocalRootSavePathDir(NooieApplication.mCtx, "DeviceMedia"));
                                if (MediaStoreUtil.MEDIA_TYPE_IMAGE_JPEG.equalsIgnoreCase(type)) {
                                    relativeSubFolderSb.append(File.separator).append(deviceId).append(File.separator).append("Photo");
                                    FileUtil.copyFile(path, relativeSubFolderSb.toString());
                                } else if (MediaStoreUtil.MEDIA_TYPE_VIDEO_MP4.equalsIgnoreCase(type)) {
                                    //relativeSubFolderSb.append(File.separator).append(FileUtil.VideoDir);
                                    //MediaStoreUtil.createMediaStoreFileForVideo(NooieApplication.mCtx, path, relativeSubFolderSb.toString(), null, null, type, null);
                                }
                            } else {
                                //StringBuilder relativeSubFolderSb = new StringBuilder();
                                relativeSubFolderSb.append(MediaStoreUtil.DEFAULT_RELATIVE_SUB_FOLDER);
                                if (MediaStoreUtil.MEDIA_TYPE_IMAGE_JPEG.equalsIgnoreCase(type)) {
                                    relativeSubFolderSb.append(File.separator).append("DeviceMedia").append(File.separator).append(deviceId).append(File.separator).append("Photo");
                                    MediaStoreUtil.createMediaStoreFileForImage(NooieApplication.mCtx, path, relativeSubFolderSb.toString(), null, null, type, null);
                                } else if (MediaStoreUtil.MEDIA_TYPE_VIDEO_MP4.equalsIgnoreCase(type)) {
                                    //relativeSubFolderSb.append(File.separator).append(FileUtil.VideoDir);
                                    //MediaStoreUtil.createMediaStoreFileForVideo(NooieApplication.mCtx, path, relativeSubFolderSb.toString(), null, null, type, null);
                                }
                            }
                            String localSavePath = relativeSubFolderSb.append(File.separator).append(path.substring(path.lastIndexOf("/") + 1)).toString();
                            return Observable.just(localSavePath);
                        } catch (Exception e) {
                            NooieLog.printStackTrace(e);
                        }
                        return Observable.just("");
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onSaveCameraPhoto(SDKConstant.ERROR, "");
                        }
                    }

                    @Override
                    public void onNext(String result) {
                        if (mTaskView != null) {
                            mTaskView.onSaveCameraPhoto(!TextUtils.isEmpty(result) ? SDKConstant.SUCCESS : SDKConstant.ERROR, result);
                        }
                    }
                });
    }
}
