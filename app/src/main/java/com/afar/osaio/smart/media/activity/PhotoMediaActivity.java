package com.afar.osaio.smart.media.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.media.adapter.CameraPhotoAdapter;
import com.afar.osaio.smart.media.bean.BaseCameraMediaBean;
import com.afar.osaio.smart.media.bean.CameraPhotoItemBean;
import com.afar.osaio.smart.media.bean.MediaDownloadBean;
import com.afar.osaio.smart.media.contract.PhotoMediaContract;
import com.afar.osaio.smart.media.download.MediaDownLoadManager;
import com.afar.osaio.smart.media.download.MediaDownloadManagerContract;
import com.afar.osaio.smart.media.listener.CameraPhotoListener;
import com.afar.osaio.smart.media.presenter.PhotoMediaPresenter;
import com.afar.osaio.smart.media.repository.PhotoMediaRepository;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.CalenderBean;
import com.afar.osaio.widget.UtcSelectDateView;
import com.afar.osaio.widget.listener.UtcSelectDataListener;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.bean.FormatInfo;
import com.nooie.sdk.device.bean.ImgItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PhotoMediaActivity extends BaseActivity implements PhotoMediaContract.View, OnRefreshListener, OnLoadMoreListener {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.usdvPhotoMedia)
    UtcSelectDateView usdvPhotoMedia;
    @BindView(R.id.stlPhotoMedia)
    SwipeToLoadLayout stlPhotoMedia;
    @BindView(R.id.swipe_target)
    RecyclerView rvPhotoMedia;

    private PhotoMediaContract.Presenter mPresenter;
    private CameraPhotoAdapter mAdapter;
    private FormatInfo mFormatInfo = null;
    private List<CalenderBean> mSDDataList = new ArrayList<>();
    private int mSelectDate = 0;
    private boolean mIsInitDate = true;

    public static void toPhotoMediaActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, PhotoMediaActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_media);
        ButterKnife.bind(this);

        initData();
        initView();
        initLoadPhotoMedia();
    }

    private void initData() {
        new PhotoMediaPresenter(this);
        PhotoMediaRepository.getInstance().initRepository(getDeviceId(), getTimeZone());
        mIsInitDate = true;
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.nooie_play_camera_photo_title);
        setupListView();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerMediaDownloadListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterMediaDownloadListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
    }

    @OnClick({R.id.ivLeft})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull PhotoMediaContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onGetFormatInfo(int state, FormatInfo formatInfo, boolean isCache, int dateIndex) {
        if (isDestroyed()) {
            return;
        }
        if (state == SDKConstant.SUCCESS) {
            if (!isCache) {
                mFormatInfo = formatInfo;
            }
            checkIsCanInitDate(mIsInitDate, mSDDataList, dateIndex);
            refreshPhotoListView(formatInfo, isCache);
        } else {
            displayPhotoListState(CameraPhotoAdapter.CAMERA_PHOTO_VIEW_TYPE_RETRY);
        }
    }

    @Override
    public void onGetSDCardRecDay(int state, int[] recDayList) {
        if (isDestroyed()) {
            return;
        }
        if (state == SDKConstant.SUCCESS) {
            if (usdvPhotoMedia != null) {
                usdvPhotoMedia.updateSelectDataList(recDayList);
            }
        }
    }

    @Override
    public void onLoadStorageImageList(int state, List<ImgItem> imageList) {
        if (isDestroyed()) {
            return;
        }
        if (state == SDKConstant.SUCCESS) {
            PhotoMediaRepository.getInstance().addImgItemList(imageList);
            startRefresh();
        } else {
            displayPhotoListState(CameraPhotoAdapter.CAMERA_PHOTO_VIEW_TYPE_RETRY);
        }
    }

    @Override
    public void onGetCameraPhotoList(int state, List<CameraPhotoItemBean> photoItemBeanList, boolean isRefresh) {
        if (isDestroyed()) {
            return;
        }
        if (state == SDKConstant.SUCCESS) {
            showPhotoListView(photoItemBeanList, isRefresh);
        }
        refreshPhotoListAfterLoadImageList(state);
        if (stlPhotoMedia != null) {
            stlPhotoMedia.setRefreshing(false);
            stlPhotoMedia.setLoadingMore(false);
        }
    }

    @Override
    public void onRefresh() {
        startRefresh();
    }

    @Override
    public void onLoadMore() {
        tryStartLoadMorePhotoList();
    }

    private void setupListView() {
        enableSwipeToRefresh(false);
        enableSwipeToLoadMore(false);
        stlPhotoMedia.setOnRefreshListener(this);
        stlPhotoMedia.setOnLoadMoreListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvPhotoMedia.setLayoutManager(layoutManager);
        mAdapter = new CameraPhotoAdapter();
        mAdapter.setListener(new CameraPhotoListener() {
            @Override
            public void onItemClick(BaseCameraMediaBean mediaBean) {
                gotoPhotoDetail(mediaBean);
            }

            @Override
            public void onRetryClick() {
                tryStartLoadPhotoList();
            }
        });
        rvPhotoMedia.setAdapter(mAdapter);
        mAdapter.setVHType(CameraPhotoAdapter.CAMERA_PHOTO_VIEW_TYPE_LIST);

        if (CollectionUtil.isNotEmpty(getSDDateList())) {
            int lastDateIndex = getSDDateList().size() - 1;
            mSelectDate = getSDDateList().get(lastDateIndex) != null && getSDDateList().get(lastDateIndex).getCalendar() != null ? (int)(getSDDateList().get(lastDateIndex).getCalendar().getTimeInMillis() / 1000L) : (int)(DateTimeUtil.getUtcTodayStartTimeStamp() / 1000L);
        }
        usdvPhotoMedia.setData(getSDDateList());
        usdvPhotoMedia.setListener(new UtcSelectDataListener() {
            @Override
            public void onClickItem(Calendar date, long currentSeekDay) {
                mIsInitDate = false;
                mSelectDate = (int)(currentSeekDay / 1000L);
                selectDateToLoadPhoto(date, currentSeekDay);
            }
        });
    }

    private void initLoadPhotoMedia() {
        tryStartLoadPhotoList();
    }

    private void gotoPhotoDetail(BaseCameraMediaBean mediaBean) {
        if (mediaBean == null) {
            return;
        }
        Bundle param = new Bundle();
        param.putString(ConstantValue.INTENT_KEY_DEVICE_ID, getDeviceId());
        if (mediaBean != null) {
            param.putString(ConstantValue.INTENT_KEY_DATA_PARAM_1, mediaBean.getPath());
            param.putLong(ConstantValue.INTENT_KEY_TIME_STAMP, mediaBean.getTime());
        }
        CameraPhotoDetailActivity.toCameraPhotoDetailActivity(this, param);
    }

    private void tryStartLoadPhotoList() {
        if (checkNull(mPresenter)) {
            return;
        }
        if (mFormatInfo == null) {
            mPresenter.getFormatInfo(getDeviceId(), mIsInitDate);
            return;
        }
        refreshPhotoListView(mFormatInfo, false);
    }

    private void tryLoadImageList() {
        if (mPresenter != null) {
            mPresenter.loadStorageImageList(getDeviceId(), true, mSelectDate);
        }
    }

    private void getCameraPhotoList(boolean isRefresh) {
        if (mPresenter != null) {
            mPresenter.getCameraPhotoList(isRefresh);
        }
    }

    private void startRefresh() {
        getCameraPhotoList(true);
    }

    private void tryStartLoadMorePhotoList() {
        boolean isHasSDCard = mFormatInfo != null && NooieDeviceHelper.isHasSdCard(mFormatInfo.getFormatStatus());
        if (isHasSDCard) {
            startLoadMore();
        }
    }

    private void startLoadMore() {
        getCameraPhotoList(false);
    }

    private void refreshPhotoListView(FormatInfo formatInfo, boolean isCache) {
        boolean isHasSDCard = formatInfo != null && NooieDeviceHelper.isHasSdCard(formatInfo.getFormatStatus());
        displayPhotoSelectDate(isHasSDCard);
        if (isHasSDCard) {
            displayPhotoListState(CameraPhotoAdapter.CAMERA_PHOTO_VIEW_TYPE_LOADING);
            if (isCache) {
                if (!PhotoMediaRepository.getInstance().checkIsImgItemListEmpty()) {
                    startRefresh();
                }
                return;
            }
            if (PhotoMediaRepository.getInstance().checkIsImgItemListEmpty()) {
                tryLoadImageList();
            } else {
                startRefresh();
            }
        } else {
            displayPhotoListState(CameraPhotoAdapter.CAMERA_PHOTO_VIEW_TYPE_NO_SD);
        }
        enableSwipeToRefresh(isHasSDCard);
        enableSwipeToLoadMore(isHasSDCard);
    }

    private void refreshPhotoListAfterLoadImageList(int state) {
        boolean isShowImageList = state == SDKConstant.SUCCESS || (mAdapter != null && CollectionUtil.isNotEmpty(mAdapter.getData()));
        displayPhotoListState(isShowImageList ? CameraPhotoAdapter.CAMERA_PHOTO_VIEW_TYPE_LIST : CameraPhotoAdapter.CAMERA_PHOTO_VIEW_TYPE_RETRY);
    }

    private void displayPhotoSelectDate(boolean show) {
        if (usdvPhotoMedia != null) {
            usdvPhotoMedia.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void displayPhotoListState(int type) {
        if (mAdapter != null) {
            mAdapter.setVHType(type);
        }
    }

    private void showPhotoListView(List<CameraPhotoItemBean> photoItemBeanList, boolean isRefresh) {
        if (CollectionUtil.isEmpty(photoItemBeanList) || mAdapter == null) {
            return;
        }
        if (isRefresh) {
            mAdapter.setData(photoItemBeanList);
        } else {
            List<CameraPhotoItemBean> result = PhotoMediaRepository.getInstance().mergerMediaItems(mAdapter.getData(), photoItemBeanList);
            mAdapter.setData(result);
        }
    }

    private void registerMediaDownloadListener() {
        MediaDownLoadManager.getInstance().setListener(new MediaDownloadManagerContract.MediaDownloadManagerListener() {
            @Override
            public void onMediaDownloadResult(int state, MediaDownloadBean downloadBean) {
                if (isDestroyed() || state != SDKConstant.SUCCESS || checkNull(downloadBean, mAdapter)) {
                    return;
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void unregisterMediaDownloadListener() {
        MediaDownLoadManager.getInstance().setListener(null);
    }

    private void enableSwipeToRefresh(boolean enable) {
        if (isDestroyed() || checkNull(stlPhotoMedia)) {
            return;
        }
        stlPhotoMedia.setRefreshEnabled(enable);
    }

    private void enableSwipeToLoadMore(boolean enable) {
        if (isDestroyed() || checkNull(stlPhotoMedia)) {
            return;
        }
        stlPhotoMedia.setLoadMoreEnabled(enable);
    }

    private void selectDateToLoadPhoto(Calendar date, long currentSeekDay) {
        if (date == null || !checkSDCardValid(mFormatInfo)) {
            return;
        }
        displayPhotoListState(CameraPhotoAdapter.CAMERA_PHOTO_VIEW_TYPE_LOADING);
        PhotoMediaRepository.getInstance().clearImgItemList();
        if (mAdapter != null) {
            mAdapter.clearData();
        }
        tryLoadImageList();
    }

    private boolean checkSDCardValid(FormatInfo formatInfo) {
        return formatInfo != null && NooieDeviceHelper.isHasSdCard(formatInfo.getFormatStatus());
    }

    private long getSelectDate() {
        return mSelectDate < 0 ? 0 : mSelectDate;
    }

    private List<CalenderBean> getSDDateList() {
        if (mSDDataList == null) {
            mSDDataList = new ArrayList<>();
        }
        if (CollectionUtil.isEmpty(mSDDataList)) {
            mSDDataList.addAll(getUtcRecent92Days());
        }
        return mSDDataList;
    }

    /**
     * 获取最近三个月
     *
     * @return
     */
    public List<CalenderBean> getUtcRecent92Days() {
        List<CalenderBean> list = new ArrayList<>();

        //获取92天的提示
        int sumDay = 92;
        long todayStartTmp = DateTimeUtil.getUtcTodayStartTimeStamp();

        // 现在Anyka IPC相机只会返回92的有效数据
        for (int i = 0; i < sumDay; i++) {
            Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone("UTC"));
            c.setTimeInMillis(todayStartTmp);
            c.add(Calendar.DAY_OF_MONTH, -(sumDay - i - 1));
            //list.add(new CalenderBean(c, i == (sumDay - 1)));
            list.add(new CalenderBean(c, false, false));
        }
        return list;
    }

    private void checkIsCanInitDate(boolean isInitDate, List<CalenderBean> sdDateList, int dateIndex) {
        if (!isInitDate || CollectionUtil.isEmpty(sdDateList) || dateIndex < 0) {
            return;
        }
        int sdDateListLen = CollectionUtil.size(sdDateList);
        int initDateIndex = (sdDateListLen - 1) - dateIndex >= 0 ? (sdDateListLen - 1) - dateIndex : -1;

        boolean isCanGetInitDate = CollectionUtil.isIndexSafe(initDateIndex, CollectionUtil.size(mSDDataList)) && mSDDataList.get(initDateIndex) != null && mSDDataList.get(initDateIndex).getCalendar() != null;
        if (isCanGetInitDate) {
            mSelectDate = (int)(mSDDataList.get(initDateIndex).getCalendar().getTimeInMillis() / 1000L);
            if (usdvPhotoMedia != null) {
                usdvPhotoMedia.updateCurrentState(mSDDataList.get(initDateIndex).getCalendar(), mSDDataList.get(initDateIndex).getCalendar().getTimeInMillis());
            }
        }
    }

    private String getDeviceId() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_DEVICE_ID);
    }

    private float getTimeZone() {
        if (getStartParam() == null || !getStartParam().containsKey(ConstantValue.INTENT_KEY_DATA_PARAM_1)) {
            return CountryUtil.getCurrentTimeZone();
        }
        return getStartParam().getFloat(ConstantValue.INTENT_KEY_DATA_PARAM_1, CountryUtil.getCurrentTimeZone());
    }
}
