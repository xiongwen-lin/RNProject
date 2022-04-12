package com.afar.osaio.smart.media.presenter;

import com.afar.osaio.smart.media.bean.CameraPhotoItemBean;
import com.afar.osaio.smart.media.bean.CameraPhotoResult;
import com.afar.osaio.smart.media.contract.PhotoMediaContract;
import com.afar.osaio.smart.media.repository.PhotoMediaRepository;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.bean.FormatInfo;
import com.nooie.sdk.device.bean.ImgItem;
import com.nooie.sdk.listener.OnGetFormatInfoListener;
import com.nooie.sdk.listener.OnGetImgListListener;
import com.nooie.sdk.listener.OnGetRecDatesListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PhotoMediaPresenter implements PhotoMediaContract.Presenter {

    private static final int DEFAULT_LOAD_IMG_COUNT = 8;
    private final static int GET_PHOTO_PAGE_COUNT = 18;

    private PhotoMediaContract.View mTaskView;
    private int mStorageImageStartTime = 0;
    private int mLoadImgIndex = 0;
    private int mGetPhotoPage = 1;
    private Subscription mGetCameraPhotoListTask = null;

    public PhotoMediaPresenter(PhotoMediaContract.View view) {
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
    public void getFormatInfo(String deviceId, boolean isInitDate) {
        DeviceCmdApi.getInstance().getFormatInfo(deviceId, new OnGetFormatInfoListener() {
            @Override
            public void onGetFormatInfo(int code, FormatInfo formatInfo) {
                dealAfterGetFormatInfo(isInitDate, deviceId, code, formatInfo);
            }
        });
    }

    @Override
    public void getSDCardRecDay(String deviceId) {
        DeviceCmdApi.getInstance().getImgsDates(deviceId, new OnGetRecDatesListener() {
            @Override
            public void onRecDates(int code, int[] list, int today) {
                if (code == SDKConstant.CODE_CACHE) {
                    if (list != null && mTaskView != null) {
                        mTaskView.onGetSDCardRecDay(SDKConstant.SUCCESS, list);
                        return;
                    }
                    return;
                }
                if (mTaskView != null) {
                    mTaskView.onGetSDCardRecDay(code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR, list);
                }
            }
        });
    }

    @Override
    public void loadStorageImageList(String deviceId, boolean isRefresh, int start) {
        if (isRefresh) {
            mStorageImageStartTime = start;
            mLoadImgIndex = 0;
        }
        DeviceCmdApi.getInstance().getImgLists(deviceId, mStorageImageStartTime, mLoadImgIndex, DEFAULT_LOAD_IMG_COUNT, new OnGetImgListListener() {
            @Override
            public void onGetImgs(int code, ImgItem[] imgItems) {
                NooieLog.d("-->> debug PhotoMediaPresenter loadStorageImageList code=" + code + " size=" + (imgItems != null ? imgItems.length : -1));
                if (code == Constant.OK) {
                    mLoadImgIndex++;
                }
                if (imgItems != null) {
                    for (int i = 0; i < imgItems.length; i++) {
                        NooieLog.d("-->> debug PhotoMediaPresenter loadStorageImageList deviceId=" + deviceId + " start=" + imgItems[i].getStart() + " startms=" + imgItems[i].getStartMs());
                    }
                }
                if (mTaskView != null) {
                    mTaskView.onLoadStorageImageList((code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR), PhotoMediaRepository.getInstance().convertImgItemList(imgItems, true));
                }
            }
        });
    }

    @Override
    public void getCameraPhotoList(boolean isRefresh) {
        if (isRefresh) {
            stopGetPhotoListTask();
            mGetPhotoPage = 1;
        } else {
            mGetPhotoPage++;
        }
        mGetCameraPhotoListTask = Observable.just(mGetPhotoPage)
                .flatMap(new Func1<Integer, Observable<CameraPhotoResult>>() {
                    @Override
                    public Observable<CameraPhotoResult> call(Integer page) {
//                        List<CameraPhotoItemBean> photoItemBeanList = PhotoMediaRepository.getInstance().getPhotoItems(page, GET_PHOTO_PAGE_COUNT);
                        return Observable.just(PhotoMediaRepository.getInstance().getPhotoItems(page, GET_PHOTO_PAGE_COUNT));
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CameraPhotoResult>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onGetCameraPhotoList(SDKConstant.ERROR, null, isRefresh);
                        }
                    }

                    @Override
                    public void onNext(CameraPhotoResult result) {
                        if (result != null) {
                            PhotoMediaRepository.getInstance().startDownloadImg(result.getImgItemList());
                        }
                        if (mTaskView != null) {
                            List<CameraPhotoItemBean> photoItemList = result != null ? result.getPhotoItemList() : null;
                            mTaskView.onGetCameraPhotoList(SDKConstant.SUCCESS, photoItemList, isRefresh);
                        }
                    }
                });
    }

    private void stopGetPhotoListTask() {
        if (mGetCameraPhotoListTask != null && !mGetCameraPhotoListTask.isUnsubscribed()) {
            mGetCameraPhotoListTask.unsubscribe();
            mGetCameraPhotoListTask = null;
        }
    }

    private void dealAfterGetFormatInfo(boolean isInitDate, String deviceId, int codeOfFormatInfo, FormatInfo formatInfo) {
        if (codeOfFormatInfo == SDKConstant.CODE_CACHE) {
            if (formatInfo != null && mTaskView != null) {
                mTaskView.onGetFormatInfo(SDKConstant.SUCCESS, formatInfo, true, -1);
                return;
            }
            return;
        }
        if (!isInitDate) {
            if (mTaskView != null) {
                mTaskView.onGetFormatInfo((codeOfFormatInfo == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR), formatInfo, false, -1);
            }
            getSDCardRecDay(deviceId);
            return;
        }
        DeviceCmdApi.getInstance().getImgsDates(deviceId, new OnGetRecDatesListener() {
            @Override
            public void onRecDates(int code, int[] list, int today) {
                if (code == SDKConstant.CODE_CACHE) {
                    if (list != null && mTaskView != null) {
                        mTaskView.onGetSDCardRecDay(SDKConstant.SUCCESS, list);
                        return;
                    }
                    return;
                }
                if (mTaskView != null) {
                    mTaskView.onGetSDCardRecDay(code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR, list);
                    mTaskView.onGetFormatInfo((codeOfFormatInfo == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR), formatInfo, false, getFirstDateIndexOfImgList(list));
                }
            }
        });
    }

    private int getFirstDateIndexOfImgList(int[] imgDateList) {
        if (imgDateList == null || imgDateList.length <= 0) {
            return -1;
        }
        int imgDataListLen = imgDateList.length;
        int firstDataIndex = -1;
        for (int i = 0; i < imgDataListLen; i++) {
            if (imgDateList[i] == 1) {
                firstDataIndex = i;
                break;
            }
        }
        return firstDataIndex;
    }
}
