package com.afar.osaio.smart.media.repository;

import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.media.bean.BaseMediaBean;
import com.afar.osaio.smart.media.bean.CameraPhotoItemBean;
import com.afar.osaio.smart.media.bean.CameraPhotoMediaBean;
import com.afar.osaio.smart.media.bean.CameraPhotoResult;
import com.afar.osaio.smart.media.bean.MediaDownloadBean;
import com.afar.osaio.smart.media.bean.NEMediaType;
import com.afar.osaio.smart.media.download.MediaDownLoadManager;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.encrypt.MD5Util;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.device.bean.ImgItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PhotoMediaRepository {

    private String mDeviceId;
    private float mTimeZone;
    private List<ImgItem> mImgItemList;

    private static final class PhotoMediaRepositoryHolder {
        public static final PhotoMediaRepository INSTANCE = new PhotoMediaRepository();
    }

    public static PhotoMediaRepository getInstance() {
        return PhotoMediaRepositoryHolder.INSTANCE;
    }

    private PhotoMediaRepository() {
        initImgItemList();
    }

    private void initImgItemList() {
        if (mImgItemList == null) {
            mImgItemList = new ArrayList<>();
        }
    }

    public void initRepository(String deviceId, float timeZone) {
        setDeviceId(deviceId);
        setTimeZone(timeZone);
        clearImgItemList();
    }

    public void setDeviceId(String deviceId) {
        mDeviceId = deviceId;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public void setTimeZone(float timeZone) {
        mTimeZone = timeZone;
    }

    public float getTimeZone() {
        return mTimeZone;
    }

    public List<ImgItem> getImgIteList() {
        return mImgItemList;
    }

    public void setImgItemList(List<ImgItem> imgItemList) {
        if (CollectionUtil.isEmpty(imgItemList)) {
            return;
        }
        initImgItemList();
        mImgItemList.clear();
        mImgItemList.addAll(imgItemList);
    }

    public void addImgItemList(List<ImgItem> imgItemList) {
        if (CollectionUtil.isEmpty(imgItemList)) {
            return;
        }
        initImgItemList();
        mImgItemList.addAll(imgItemList);
//        startDownloadImg(imgItemList);
    }

    public void clearImgItemList() {
        if (CollectionUtil.isEmpty(mImgItemList)) {
            return;
        }
        if (mImgItemList != null) {
            mImgItemList.clear();
        }
    }

    public boolean checkIsImgItemListEmpty() {
        return CollectionUtil.isEmpty(mImgItemList);
    }

    public List<CameraPhotoItemBean> getAllPhotoItem() {
        return getMediaItems(mImgItemList);
    }

    public CameraPhotoResult getPhotoItems(int page, int pageCount) {
        if (CollectionUtil.isEmpty(mImgItemList)) {
            return null;
        }
        int pageStartIndex = (page - 1) * pageCount;
        int pageEndIndex = page * pageCount;
        if (mImgItemList.size() - 1 < pageStartIndex) {
            return null;
        }
        if (mImgItemList.size() < pageEndIndex) {
            pageEndIndex = mImgItemList.size();
        }
        if (pageStartIndex < 0 || pageStartIndex > pageEndIndex || pageEndIndex > mImgItemList.size()) {
            return null;
        }
        List<ImgItem> imgItemList = mImgItemList.subList(pageStartIndex, pageEndIndex);
        CameraPhotoResult result = new CameraPhotoResult();
        result.setImgItemList(imgItemList);
        result.setPhotoItemList(getMediaItems(imgItemList));
        return result;
    }

    public List<CameraPhotoMediaBean> getAllPhoto() {
        return convertCameraPhotoMediaBean(mImgItemList);
    }

    public List<ImgItem> convertImgItemList(ImgItem[] imgItems, boolean isResolve) {
        List<ImgItem> result = new ArrayList<>();
        if (imgItems == null || imgItems.length == 0) {
            return result;
        }
        for (int i = 0; i < imgItems.length; i++) {
            if (imgItems[i] != null) {
                result.add(imgItems[i]);
            }
        }
        if (isResolve) {
            Collections.reverse(result);
        }
        return result;
    }

    public void startDownloadImg(List<ImgItem> imgItemList) {
        if (CollectionUtil.isEmpty(imgItemList)) {
            return;
        }
        MediaDownLoadManager.getInstance().startDownloadMediaFileTask(convertMediaDownloadBean(imgItemList));
    }

    public List<CameraPhotoItemBean> mergerMediaItems(List<CameraPhotoItemBean> photoItemList, List<CameraPhotoItemBean> newPhotoItemList) {
        List<CameraPhotoItemBean> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(photoItemList) && CollectionUtil.isEmpty(newPhotoItemList)) {
            return result;
        }
        if (CollectionUtil.isEmpty(photoItemList) || CollectionUtil.isEmpty(newPhotoItemList)) {
            return CollectionUtil.isNotEmpty(photoItemList) ? photoItemList : newPhotoItemList;
        }

        List<CameraPhotoMediaBean> mediaBeans = new ArrayList<>();
        for (CameraPhotoItemBean photoItemBean : CollectionUtil.safeFor(photoItemList)) {
            if (photoItemBean != null && CollectionUtil.isNotEmpty(photoItemBean.getMedias())) {
                mediaBeans.addAll(photoItemBean.getMedias());
            }
        }

        for (CameraPhotoItemBean photoItemBean : CollectionUtil.safeFor(newPhotoItemList)) {
            if (photoItemBean != null && CollectionUtil.isNotEmpty(photoItemBean.getMedias())) {
                mediaBeans.addAll(photoItemBean.getMedias());
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

        CameraPhotoMediaBean targetMediaBean = null;
        List<CameraPhotoMediaBean> mediaBeansOfDay = null;
        List<List<CameraPhotoMediaBean>> mediaBeansOfDayList = new ArrayList<>();

        for (int i = 0; i < mediaBeans.size(); i++) {
            CameraPhotoMediaBean mediaBean = mediaBeans.get(i);
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

        for (List<CameraPhotoMediaBean> mediaBeanList : CollectionUtil.safeFor(mediaBeansOfDayList)) {
            if (CollectionUtil.isNotEmpty(mediaBeanList)) {
                CameraPhotoItemBean mediaItemBean = new CameraPhotoItemBean();
                mediaItemBean.setMedias(mediaBeanList);
                result.add(mediaItemBean);
            }
        }

        return result;
    }

    private List<CameraPhotoItemBean> getMediaItems(List<ImgItem> imgItemList) {
        List<CameraPhotoItemBean> result = new ArrayList<>();

        if (CollectionUtil.isEmpty(imgItemList)) {
            return result;
        }

        List<CameraPhotoMediaBean> mediaBeans = new ArrayList<>();
        for (ImgItem imgItem : CollectionUtil.safeFor(imgItemList)) {
            NooieLog.d("-->> debug PhotoMediaRepository getMediaItems: img startTime=" + DateTimeUtil.formatDate(NooieApplication.mCtx, imgItem.getStart() * 1000L, DateTimeUtil.PATTERN_YMD_HMS_1)
                    + " time=" + DateTimeUtil.formatDate(NooieApplication.mCtx, convertImageTime(imgItem.getStart(), imgItem.getStartMs(), (int)mTimeZone), DateTimeUtil.PATTERN_YMD_HMS_1));
            //NooieLog.d("-->> MediaPresenter getMediaItems origin media path=" + mediaFile.getPath() + " lastM=" + mediaFile.lastModified());
            if (imgItem.getStart() > 0) {
                CameraPhotoMediaBean cameraPhotoMediaBean = new CameraPhotoMediaBean();
                cameraPhotoMediaBean.setStartTs(imgItem.getStart());
                cameraPhotoMediaBean.setStartMs(imgItem.getStartMs());
                cameraPhotoMediaBean.setTime(convertImageTime(imgItem.getStart(), imgItem.getStartMs(), (int)mTimeZone) + imgItem.getStartMs());
                //cameraPhotoMediaBean.setDownloadUrl("https://gimg2.baidu.com/image_search/src=http%3A%2F%2F01.minipic.eastday.com%2F20170406%2F20170406160424_4d1b428f75f354a05977978cb8aefdb4_2.jpeg&refer=http%3A%2F%2F01.minipic.eastday.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1620552108&t=ad19943e4657d34de40413c78cceef7d");
                cameraPhotoMediaBean.setDownloadUrl(getDevicePhotoDownloadUrl(imgItem.getStart(), imgItem.getStartMs()));
                cameraPhotoMediaBean.setId(Long.valueOf((cameraPhotoMediaBean.getTime() / 1000) / 3600 / 24).toString());
                cameraPhotoMediaBean.setPath(getDevicePhotoStoragePath(getDeviceId(), imgItem.getStart(), imgItem.getStartMs()));
                cameraPhotoMediaBean.setThumbnailPath(getDevicePhotoStoragePath(getDeviceId(), imgItem.getStart(), imgItem.getStartMs()));
                mediaBeans.add(cameraPhotoMediaBean);
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

        CameraPhotoMediaBean targetMediaBean = null;
        List<CameraPhotoMediaBean> mediaBeansOfDay = null;
        List<List<CameraPhotoMediaBean>> mediaBeansOfDayList = new ArrayList<>();

        for (int i = 0; i < mediaBeans.size(); i++) {
            CameraPhotoMediaBean mediaBean = mediaBeans.get(i);
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

        for (List<CameraPhotoMediaBean> mediaBeanList : CollectionUtil.safeFor(mediaBeansOfDayList)) {
            if (CollectionUtil.isNotEmpty(mediaBeanList)) {
                CameraPhotoItemBean mediaItemBean = new CameraPhotoItemBean();
                mediaItemBean.setMedias(mediaBeanList);
                result.add(mediaItemBean);
            }
        }

        return result;
    }

    private List<CameraPhotoMediaBean> convertCameraPhotoMediaBean(List<ImgItem> imgItemList) {
        List<CameraPhotoMediaBean> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(imgItemList)) {
            return result;
        }

        for (ImgItem imgItem : CollectionUtil.safeFor(imgItemList)) {
            NooieLog.d("-->> debug PhotoMediaRepository convertCameraPhotoMediaBean: img startTime=" + DateTimeUtil.formatDate(NooieApplication.mCtx, imgItem.getStart() * 1000L, DateTimeUtil.PATTERN_YMD_HMS_1)
                    + " time=" + DateTimeUtil.formatDate(NooieApplication.mCtx, convertImageTime(imgItem.getStart(), imgItem.getStartMs(), (int)mTimeZone), DateTimeUtil.PATTERN_YMD_HMS_1));
            //NooieLog.d("-->> MediaPresenter getMediaItems origin media path=" + mediaFile.getPath() + " lastM=" + mediaFile.lastModified());
            NooieLog.d("-->> debug PhotoMediaRepository convertCameraPhotoMediaBean: ");
            if (imgItem.getStart() > 0) {
                CameraPhotoMediaBean cameraPhotoMediaBean = new CameraPhotoMediaBean();
                cameraPhotoMediaBean.setStartTs(imgItem.getStart());
                cameraPhotoMediaBean.setStartMs(imgItem.getStartMs());
                //cameraPhotoMediaBean.setTime((imgItem.getStart() * 1000L) + imgItem.getStartMs());
                cameraPhotoMediaBean.setTime(convertImageTime(imgItem.getStart(), imgItem.getStartMs(), (int)mTimeZone) + imgItem.getStartMs());
                cameraPhotoMediaBean.setDownloadUrl(getDevicePhotoDownloadUrl(imgItem.getStart(), imgItem.getStartMs()));
                cameraPhotoMediaBean.setId(Long.valueOf((cameraPhotoMediaBean.getTime() / 1000) / 3600 / 24).toString());
                cameraPhotoMediaBean.setPath(getDevicePhotoStoragePath(getDeviceId(), imgItem.getStart(), imgItem.getStartMs()));
                cameraPhotoMediaBean.setThumbnailPath(getDevicePhotoStoragePath(getDeviceId(), imgItem.getStart(), imgItem.getStartMs()));
                result.add(cameraPhotoMediaBean);
            }
        }

        if (CollectionUtil.isEmpty(result)) {
            return result;
        }

        Collections.sort(result, new Comparator<BaseMediaBean>() {
            @Override
            public int compare(BaseMediaBean first, BaseMediaBean second) {
                long firstTime = first.getTime();
                long secondTime = second.getTime();
                return firstTime > secondTime ? -1 : 1;
            }
        });
        return result;
    }

    private List<MediaDownloadBean> convertMediaDownloadBean(List<ImgItem> imgItemList) {
        List<MediaDownloadBean> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(imgItemList)) {
            return result;
        }

        for (ImgItem imgItem : imgItemList) {
            if (imgItem != null) {
                MediaDownloadBean downloadBean = new MediaDownloadBean();
                downloadBean.setStoragePath(getDevicePhotoFolderPath(getDeviceId()));
                downloadBean.setStorageFileName(getDevicePhotoFileName(getDeviceId(), imgItem.getStart(), imgItem.getStartMs()));
                //downloadBean.setDownloadUrl("https://gimg2.baidu.com/image_search/src=http%3A%2F%2F01.minipic.eastday.com%2F20170406%2F20170406160424_4d1b428f75f354a05977978cb8aefdb4_2.jpeg&refer=http%3A%2F%2F01.minipic.eastday.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1620552108&t=ad19943e4657d34de40413c78cceef7d");
                downloadBean.setDownloadUrl(getDevicePhotoDownloadUrl(imgItem.getStart(), imgItem.getStartMs()));
                downloadBean.setMediaType(NEMediaType.IMAGE);
                downloadBean.setKey(getDeviceMediaKey(getDeviceId(), downloadBean.getDownloadUrl()));
                result.add(downloadBean);
            }
        }
        return result;
    }

    private String getDeviceMediaFolderPath(String deviceId) {
        StringBuilder pathSb = new StringBuilder();
        pathSb.append(FileUtil.getPrivateLocalRootSavePathDir(NooieApplication.mCtx, "DeviceMedia"))
                .append(File.separator)
                .append(deviceId);
        return pathSb.toString();
    }

    private String getDevicePhotoFolderPath(String deviceId) {
        StringBuilder pathSb = new StringBuilder();
        pathSb.append(getDeviceMediaFolderPath(deviceId))
                .append(File.separator)
                .append("Photo");
        return pathSb.toString();
    }

    private String getDeviceVideoFolderPath(String deviceId) {
        StringBuilder pathSb = new StringBuilder();
        pathSb.append(getDeviceMediaFolderPath(deviceId))
                .append(File.separator)
                .append("Video");
        return pathSb.toString();
    }

    private String getDevicePhotoStoragePath(String deviceId, int start, int startMs) {
        StringBuilder pathSb = new StringBuilder();
        pathSb.append(getDevicePhotoFolderPath(deviceId))
                .append(File.separator)
                .append(getDevicePhotoFileName(deviceId, start, startMs));
        return pathSb.toString();
    }

    private String getDevicePhotoFileName(String deviceId, int start, int startMs) {
        StringBuilder pathSb = new StringBuilder();
        pathSb.append(start + "_" + startMs + ".jpg");
        return pathSb.toString();
    }

    private String getDevicePhotoThumbnailStoragePath(String deviceId, int start, int startMs) {
        StringBuilder pathSb = new StringBuilder();
        pathSb.append(getDevicePhotoFolderPath(deviceId))
                .append(File.separator)
                .append(getDevicePhotoThumbnailFileName(deviceId, start, startMs));
        return pathSb.toString();
    }

    private String getDevicePhotoThumbnailFileName(String deviceId, int start, int startMs) {
        StringBuilder pathSb = new StringBuilder();
        pathSb.append("thumbnail" + "_" + start + "_" + startMs + ".jpg");
        return pathSb.toString();
    }

    private String getDeviceMediaKey(String deviceId, String downloadUrl) {
        return MD5Util.MD5Hash(new StringBuilder().append(deviceId).append(downloadUrl).toString());
    }

    private String getDevicePhotoDownloadUrl(int start, int startMs) {
        int timeZone = (int) mTimeZone;
        start = start > 0 ? (start - timeZone * 3600) : 0;
        long imgStart = start  * 1000L;
        StringBuilder downloadUrlSb = new StringBuilder();
        downloadUrlSb.append("http://192.168.43.1")
                .append(":")
                .append("8080")
                .append("/")
                .append(DateTimeUtil.formatDate(NooieApplication.mCtx, imgStart, "yyyyMMdd"))
                .append("/")
                .append("victure")
                .append("_")
                .append(DateTimeUtil.formatDate(NooieApplication.mCtx, imgStart, "yyyyMMdd_HHmmss"))
                .append("@")
                .append(getStartMsValue(startMs))
                .append(".jpg");
        return downloadUrlSb.toString();
    }

    private static final int START_MS_MAX_LEN = 6;
    private String getStartMsValue(int startMs) {
        String startMsStr = String.valueOf(startMs);
        if (TextUtils.isEmpty(startMsStr)) {
            return null;
        }
        int addZeroLen = START_MS_MAX_LEN - startMsStr.length();
        if (addZeroLen < 1) {
            return startMsStr;
        }
        StringBuilder startMsSb = new StringBuilder();
        for (int i = 0; i < addZeroLen; i++) {
            startMsSb.append("0");
        }
        startMsSb.append(startMsStr);
        return startMsSb.toString();
    }

    private long convertImageTime(int start, int startMs, int timeZone) {
        start = start > 0 ? (start - timeZone * 3600) : 0;
        long imgStart = start > 0 ? start * 1000L : 0;
        return imgStart;
    }
}
