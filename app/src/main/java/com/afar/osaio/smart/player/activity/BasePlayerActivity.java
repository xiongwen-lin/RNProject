package com.afar.osaio.smart.player.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.nooie.common.base.GlobalData;
import com.nooie.common.bean.PhoneVolume;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.file.MediaStoreUtil;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.CalenderBean;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.tool.SystemUtil;
import com.nooie.sdk.listener.OnPlayerListener;
import com.nooie.sdk.media.NooieMediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import rx.Subscription;


/**
 * Created by Victor on 2019/8/7
 * Email: victor.qiao.0604@gmail.com
 * Copyright © 2020年 Victor. All rights reserved.
 */
public abstract class BasePlayerActivity extends BaseActivity implements OnPlayerListener {
    public static final String[] RECORD_AUDIO_PERMS = {
            Manifest.permission.RECORD_AUDIO
    };

    protected static final int SWITCH_CLOUD_SDCARD_SHOW = 0x10;
    protected static final int SWITCH_CLOUD_SDCARD_HIDE = 0x20;
    protected static final int SWITCH_CURRENT_IS_CLOUD = 0x30;
    protected static final int SWITCH_CURRENT_IS_SDCARD = 0x40;

    protected static final int SWITCH_WIDGET_SHOW_LONG = 1000 * 3;
    protected static final int ACTION_LAND_WIDGET_SHOW_LONG = 1000 * 5;

    public static final int GESTURE_MOVE_LEFT = 1;
    public static final int GESTURE_MOVE_TOP = 2;
    public static final int GESTURE_MOVE_RIGHT = 3;
    public static final int GESTURE_MOVE_BOTTOM = 4;
    public static final int GESTURE_TOUCH_DOWN = 5;
    public static final int GESTURE_TOUCH_UP = 6;

    public static final int ORIENTATION_TYPE_NONE = 0;
    public static final int ORIENTATION_TYPE_PORTRAIT = 1;

    private static final float DEFAULT_AUDIO_VOLUME_PERCENT = 0.7f;

    protected TextView tvRecordTime;
    protected TextView tvRecordTimeLand;

    public ImageView ivThumbnail = null;
    protected boolean mRecording = false;
    protected List<CalenderBean> mSDCardDataList = new ArrayList<>();
    protected List<CalenderBean> mCloudDataList = new ArrayList<>();
    public List<String> mSaveRcecordFile = new ArrayList<>();

    private Drawable redDotDrawable;
    private Drawable whiteDotDrawable;
    private boolean mIsVideoBitmap;
    private Bitmap thumbnailBitmap;
    private ObjectAnimator firstAnim;
    private AnimatorSet secondAnim;
    protected boolean mIsLandScreen = false;

    protected Handler mHandler = new Handler(Looper.myLooper());
    protected int flag;

    protected String mDeviceId;
    private PhoneVolume mPhoneVolume = null;
    private int mOrientationType = 0;

    protected abstract NooieMediaPlayer nooiePlayer();

    @Override
    public void onVideoStart(NooieMediaPlayer player) {
    }

    @Override
    public void onVideoStop(NooieMediaPlayer player) {
    }

    @Override
    public void onAudioStart(NooieMediaPlayer player) {
    }

    @Override
    public void onAudioStop(NooieMediaPlayer player) {
    }

    @Override
    public void onTalkingStart(NooieMediaPlayer player) {
    }

    @Override
    public void onTalkingStop(NooieMediaPlayer player) {
    }

    @Override
    public void onRecordStart(NooieMediaPlayer player, boolean result, String file) {
    }

    @Override
    public void onRecordStop(NooieMediaPlayer player, boolean result, String file) {
        if (result) {
            sendRefreshPicture(file);
        }
    }

    @Override
    public void onRecordTimer(NooieMediaPlayer player, int duration) {
        int h = duration / (60 * 60);
        int s = duration % (60 * 60);
        int m = s / 60;
        s = s % 60;
    }

    @Override
    public void onSnapShot(NooieMediaPlayer player, boolean result, String path) {
        if (isAutoSnapShot(path)) {
            // 自动截首屏
            if (result) {
                String newFile = FileUtil.mapPreviewThumbPath(path);
                FileUtil.renamePreviewThumb(path, newFile);
                saveDevicePreviewFile(mDeviceId, newFile);
            }
        } else {
            // 手动点击截屏
            if (isDestroyed()) {
                return;
            }

            if (result) {
                updateFileInMediaStore(mUserAccount, path, MediaStoreUtil.MEDIA_TYPE_IMAGE_JPEG);
                sendRefreshPicture(path);
                showVideoThumbnail(path, false);
            } else {
                //ToastUtil.showToast(this, R.string.get_fail);
            }
        }
    }

    @Override
    public void onFps(NooieMediaPlayer player, int fps) {
    }

    @Override
    public void onBitrate(NooieMediaPlayer player, double bitrate) {
    }

    @Override
    public void onBufferingStart(NooieMediaPlayer player) {
    }

    @Override
    public void onBufferingStop(NooieMediaPlayer player) {
    }

    @Override
    public void onPlayFinish(NooieMediaPlayer player) {
    }

    @Override
    public void onPlayOneFinish(NooieMediaPlayer player) {
    }

    @Override
    public void onPlayFileBad(NooieMediaPlayer player) {
    }

    protected boolean isAutoSnapShot(String path) {
        if (!TextUtils.isEmpty(path) && path.contains(FileUtil.NOOIE_PREVIEW_THUMBNAIL_PREFIX)) {
            return true;
        }
        return false;
    }

    public static String getDevicePreviewFile(String deviceId) {
        String account = GlobalData.getInstance().getAccount();
        String key = String.format("%s_%s", GlobalPrefs.KEY_DEVICE_PREVIEW, deviceId);
        return GlobalPrefs.getString(NooieApplication.mCtx, account, key, "");
    }

    private void saveDevicePreviewFile(String deviceId, String file) {
        String account = GlobalData.getInstance().getAccount();
        String key = String.format("%s_%s", GlobalPrefs.KEY_DEVICE_PREVIEW, deviceId);

        // delete old files
        String oldFile = GlobalPrefs.getString(NooieApplication.mCtx, account, key, "");
        FileUtil.deleteFile(oldFile);

        GlobalPrefs.putString(NooieApplication.mCtx, account, key, file);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destoryMediaoScanner();
        unregisterRotationObserver();
    }

    protected void init() {
        //initMediaScanner();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            flag = getWindow().getDecorView().getSystemUiVisibility();
        }
        registerRotationObserver();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changeSystemScreenUi();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mIsLandScreen = true;
            screenOrientationChanged(true);
            setFullScreen(this);
        } else {
            mIsLandScreen = false;
            screenOrientationChanged(false);
            cancelFullScreen(this);
        }
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation){
        if (ApHelper.getInstance().checkIsApDirectConnectionMode()) {
            super.setRequestedOrientation(requestedOrientation);
            return;
        }
        if (mOrientationType == ORIENTATION_TYPE_PORTRAIT) {
            super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            super.setRequestedOrientation(requestedOrientation);
        }
    }

    public void setOrientationType(int orientationType) {
        mOrientationType = orientationType;
    }

    public void changeSystemScreenUi() {
        /*
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(flag);
        }
        */
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_BAR).fitsSystemWindows(false).init();
        } else {
            ImmersionBar.with(this).hideBar(BarHide.FLAG_SHOW_BAR).fitsSystemWindows(true).init();
        }
    }

    protected void screenOrientationChanged(boolean landscape) {
    }

    protected boolean isLandscape() {
        return mIsLandScreen;//NooieApplication.get().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * set the
     *
     * @param activity
     */
    protected void setFullScreen(Activity activity) {
        if (checkNull(activity, activity.getWindow())) {
            return;
        }
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 取消全屏
     *
     * @param activity
     */
    protected void cancelFullScreen(Activity activity) {
        if (checkNull(activity, activity.getWindow())) {
            return;
        }
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    protected static final int RECORD_DOT_SHOW = 0x10;
    protected static final int RECORD_DOT_HIDE = 0x20;

    private Subscription mRecordTimeTask;

    protected void startRecordTimer() {
        if (!isLandscape() && tvRecordTime != null && tvRecordTimeLand != null) {
            tvRecordTime.setVisibility(View.VISIBLE);
            tvRecordTimeLand.setVisibility(View.GONE);
        } else if (tvRecordTime != null && tvRecordTimeLand != null) {
            tvRecordTime.setVisibility(View.GONE);
            tvRecordTimeLand.setVisibility(View.VISIBLE);
        }

        mRecording = true;
        if (redDotDrawable == null) {
            redDotDrawable = getResources().getDrawable(R.drawable.solid_circle_red);
            redDotDrawable.setBounds(0, 0, redDotDrawable.getMinimumWidth(), redDotDrawable.getMinimumHeight());
        }

        if (whiteDotDrawable == null) {
            whiteDotDrawable = getResources().getDrawable(R.drawable.solid_circle_white);
            whiteDotDrawable.setBounds(0, 0, whiteDotDrawable.getMinimumWidth(), whiteDotDrawable.getMinimumHeight());
        }

        /*
        mRecordTimeTask = Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Long time) {
                        updateRecordTimer(time);
                    }
                });
                */
    }

    protected void stopRecordTimer() {
        if (tvRecordTime != null && tvRecordTimeLand != null) {
            tvRecordTime.setText(DateTimeUtil.getTimeHms(0));
            tvRecordTime.setVisibility(View.GONE);
            tvRecordTimeLand.setText(DateTimeUtil.getTimeHms(0));
            tvRecordTimeLand.setVisibility(View.GONE);
        }

        mRecording = false;
        /*
        if (mRecordTimeTask != null && !mRecordTimeTask.isUnsubscribed()) {
            mRecordTimeTask.unsubscribe();
        }
        */
    }

    protected void updateRecordTimer(long time) {
        if (tvRecordTime == null || tvRecordTimeLand == null || !mRecording) {
            return;
        }

        tvRecordTime.setText(DateTimeUtil.getTimeHms(time));
        tvRecordTimeLand.setText(DateTimeUtil.getTimeHms(time));
        if (tvRecordTime.getTag() == null || (Integer) tvRecordTime.getTag() == RECORD_DOT_SHOW) {
            tvRecordTime.setTag(RECORD_DOT_HIDE);
            tvRecordTime.setCompoundDrawables(redDotDrawable, null, null, null);
            tvRecordTimeLand.setCompoundDrawables(redDotDrawable, null, null, null);
        } else {
            tvRecordTime.setTag(RECORD_DOT_SHOW);
            tvRecordTime.setCompoundDrawables(whiteDotDrawable, null, null, null);
            tvRecordTimeLand.setCompoundDrawables(whiteDotDrawable, null, null, null);
        }
    }

    protected MyWorker autoHideOtherPlaybackViewAnimWorker = new MyWorker(this) {
        @Override
        public void run() {
            super.run();
            if (isDestroyed() || checkNull(weakReference, weakReference.get())) {
                return;
            }
            weakReference.get().hideOtherPlaybackViewAnim(500);
        }
    };

    protected void hideOtherPlaybackViewAnim(int duration) {
    }

    protected MyWorker autoHideActionViewLandWorker = new MyWorker(this) {
        @Override
        public void run() {
            super.run();
            if (isDestroyed() || checkNull(weakReference, weakReference.get())) {
                return;
            }
            weakReference.get().hideActionViewLand(500);
        }
    };

    protected void hideActionViewLand(int duration) {
    }

    protected static class MyWorker implements Runnable {

        WeakReference<BasePlayerActivity> weakReference;

        public MyWorker(BasePlayerActivity activity) {
            this.weakReference = new WeakReference<BasePlayerActivity>(activity);
        }

        @Override
        public void run() {
        }
    }

    public void showVideoThumbnail(final String file, final boolean video) {
        NooieMediaPlayer player = nooiePlayer();
        if (ivThumbnail == null || player == null) {
            return;
        }

        ToastUtil.showToast(this, video ? R.string.living_file_save_to_album : R.string.living_photo_file_save_to_album);

        // reset to default
        mHandler.removeCallbacks(dismissThumbnailRunnable);
        ivThumbnail.setImageBitmap(null);

        if (thumbnailBitmap != null && !thumbnailBitmap.isRecycled()) {
            thumbnailBitmap.recycle();
            thumbnailBitmap = null;
        }

        if (firstAnim != null && firstAnim.isRunning()) {
            firstAnim.cancel();
        }

        if (secondAnim != null && secondAnim.isRunning()) {
            secondAnim.cancel();
        }

        // get bitmap
        thumbnailBitmap = createBitmap(file, video);
        ivThumbnail.setVisibility(View.VISIBLE);

        ConstraintLayout.LayoutParams playerParams = (ConstraintLayout.LayoutParams) player.getLayoutParams();
        final int width = player.getMeasuredWidth();//playerParams.width;
        final int height = player.getMeasuredHeight();//playerParams.height;

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) ivThumbnail.getLayoutParams();
        int w = width;
        int h = height;
        if (height * 1.0 / width > 9.0 / 16) {
            // width is base
            h = w * 9 / 16;
        } else {
            // height is base
            w = h * 16 / 9;
        }

        params.width = w;
        params.height = h;
        ivThumbnail.setLayoutParams(params);
        ivThumbnail.setScaleY(1.0f);
        ivThumbnail.setScaleX(1.0f);
        ivThumbnail.setTranslationY(0);
        ivThumbnail.setTranslationX(0);

        // first animation
        firstAnim = ObjectAnimator.ofFloat(ivThumbnail, "Alpha", 0.0f, 1.0f);
        firstAnim.setDuration(300);

        firstAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (ivThumbnail == null) {
                    return;
                }
                // setup image
                if (thumbnailBitmap == null) {
                    //Snapshot default image
                    ivThumbnail.setImageResource(R.drawable.default_preview);
                } else {
                    ivThumbnail.setImageBitmap(thumbnailBitmap);
                }

                // second animation
                int margin = DisplayUtil.dpToPx(NooieApplication.mCtx, 16);
                int landMarginBottom = DisplayUtil.dpToPx(NooieApplication.mCtx, 100);
                int pivotX = width - margin;
                if (isLandscape()) {
                    //pivotX -= getResources().getDimensionPixelSize(R.dimen.date_select_width);
                }
                ivThumbnail.setPivotX(pivotX);
                if (isLandscape()) {
                    ivThumbnail.setPivotY(height - landMarginBottom);
                } else {
                    ivThumbnail.setPivotY(height - margin);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivThumbnail, "ScaleX", 1.0f, 0.2f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivThumbnail, "ScaleY", 1.0f, 0.2f);

        secondAnim = new AnimatorSet();
        secondAnim.playTogether(scaleX, scaleY);
        secondAnim.setDuration(600);
        secondAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mIsVideoBitmap = video;
                mHandler.removeCallbacks(dismissThumbnailRunnable);
                mHandler.postDelayed(dismissThumbnailRunnable, 1000 * 2);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        AnimatorSet showThumbnailAnim = new AnimatorSet();
        showThumbnailAnim.playSequentially(firstAnim, secondAnim);
        showThumbnailAnim.start();
    }

    private MyWorker dismissThumbnailRunnable = new MyWorker(this) {
        @Override
        public void run() {
            super.run();
            if (isDestroyed() || checkNull(weakReference, weakReference.get(), weakReference.get().ivThumbnail)) {
                return;
            }
            weakReference.get().ivThumbnail.setVisibility(View.GONE);
            weakReference.get().ivThumbnail.setScaleY(1.0f);
            weakReference.get().ivThumbnail.setScaleX(1.0f);
            weakReference.get().ivThumbnail.setImageBitmap(null);
            if (weakReference.get().thumbnailBitmap != null && !weakReference.get().thumbnailBitmap.isRecycled()) {
                weakReference.get().thumbnailBitmap.recycle();
                weakReference.get().thumbnailBitmap = null;
            }

            //ToastUtil.showToast(weakReference.get(), weakReference.get().mIsVideoBitmap ? R.string.living_file_save_to_album : R.string.living_photo_file_save_to_album);

        }
    };

    private Bitmap createBitmap(String file, boolean video) {
        if (video) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(file);
                return retriever.getFrameAtTime();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } finally {
                try {
                    retriever.release();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        } else {
            return BitmapFactory.decodeFile(file);
        }
        return null;
    }

    private MediaScannerConnection mMediaScannerConnection;

    private void initMediaScanner() {
        try {
            mMediaScannerConnection = new MediaScannerConnection(NooieApplication.mCtx, null);
            mMediaScannerConnection.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void destoryMediaoScanner() {
        if (mMediaScannerConnection != null) {
            mMediaScannerConnection.disconnect();
            mMediaScannerConnection = null;
        }
    }

    private void refreshVideo() {
        /*
        String videoPath = FileUtils.getRecordDir(mUserAccount).getAbsolutePath();
        NooieLog.d("-->> DanaleBasePlayerActivity initMediaScanner videoPath=" + videoPath);
        scanFile(videoPath, true);
         */
    }

    public void scanFile(String file, boolean isVideo) {
        if (mMediaScannerConnection != null && isVideo) {
            mMediaScannerConnection.scanFile(file, "video/mp4");
        }
    }

    public void updateFileInMediaStore(String account, String path, String mediaType) {
    }

    /**
     * 获取最近三个月
     *
     * @return
     */
    public static List<CalenderBean> getUtcRecent92Days() {
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

    public static List<CalenderBean> getUtcRecent7Days() {
        List<CalenderBean> list = new ArrayList<>();

        //获取7天的提示
        int sumDay = 7;
        long todayStartTmp = DateTimeUtil.getUtcTodayStartTimeStamp();

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

    public static List<CalenderBean> getUtcRecentDays(int dayNum, int defaultNum) {
        List<CalenderBean> list = new ArrayList<>();

        //获取7天的提示
        int sumDay = dayNum > 0 ? dayNum : defaultNum;
        long todayStartTmp = DateTimeUtil.getUtcTodayStartTimeStamp();

        for (int i = 0; i < sumDay; i++) {
            Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone("UTC"));
            c.setTimeInMillis(todayStartTmp);
            c.add(Calendar.DAY_OF_MONTH, -(sumDay - i - 1));
            //list.add(new CalenderBean(c, i == (sumDay - 1)));
            list.add(new CalenderBean(c, false));
        }
        return list;
    }

    /**
     * 获取最近三个月
     *
     * @return
     */
    public static List<CalenderBean> getRecent92Days() {
        List<CalenderBean> list = new ArrayList<>();

        //获取92天的提示
        int sumDay = 92;
        long todayStartTmp = DateTimeUtil.getTodayStartTimeStamp();

        // 现在Anyka IPC相机只会返回92的有效数据
        for (int i = 0; i < sumDay; i++) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(todayStartTmp);
            c.add(Calendar.DAY_OF_MONTH, -(sumDay - i - 1));
            list.add(new CalenderBean(c, i == (sumDay - 1)));
        }

        return list;
    }

    public static List<CalenderBean> getRecent7Days() {
        List<CalenderBean> list = new ArrayList<>();

        //获取7天的提示
        int sumDay = 7;
        long todayStartTmp = DateTimeUtil.getTodayStartTimeStamp();

        for (int i = 0; i < sumDay; i++) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(todayStartTmp);
            c.add(Calendar.DAY_OF_MONTH, -(sumDay - i - 1));
            list.add(new CalenderBean(c, i == (sumDay - 1)));
        }
        return list;
    }

    public static List<CalenderBean> getRecentDays(int dayNum, int defaultNum) {
        List<CalenderBean> list = new ArrayList<>();

        //获取7天的提示
        int sumDay = dayNum > 0 ? dayNum : defaultNum;
        long todayStartTmp = DateTimeUtil.getTodayStartTimeStamp();

        for (int i = 0; i < sumDay; i++) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(todayStartTmp);
            c.add(Calendar.DAY_OF_MONTH, -(sumDay - i - 1));
            list.add(new CalenderBean(c, i == (sumDay - 1)));
        }
        return list;
    }

    public void startSaveFileToMediaStoreTask() {
    }

    private OrientationSensorListener mOrientationSensorListener = null;
    private void registerRotationObserver() {
        //注册 Settings.System.ACCELEROMETER_ROTATION 监听用户旋转设置
        if (rotationObserver != null) {
            getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),true, rotationObserver);
        }
        //注册重力感应器  监听屏幕旋转
        unregisterRotationObserver();
        mOrientationSensorListener = new OrientationSensorListener();
        SensorManager mSm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = mSm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSm.registerListener(mOrientationSensorListener, mSensor, SensorManager.SENSOR_DELAY_UI);
    }

    private void unregisterRotationObserver() {
        if (mOrientationSensorListener != null) {
            SensorManager mSm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mSm.unregisterListener(mOrientationSensorListener);
            mOrientationSensorListener = null;
        }
        if (rotationObserver != null) {
            getContentResolver().unregisterContentObserver(rotationObserver);
            rotationObserver = null;
        }
    }

    public void setAngleChangeListener(AngleChangeListener listener) {
        if (mOrientationSensorListener != null) {
            mOrientationSensorListener.setOrientationSensorListener(listener);
        }
    }

    private ContentObserver rotationObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            NooieLog.d("-->> BasePlayerActivity onChange selfChange=" + selfChange);
            if (selfChange) {
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            }
        }
    };

    public class OrientationSensorListener implements SensorEventListener {
        private static final int _DATA_X = 0;
        private static final int _DATA_Y = 1;
        private static final int _DATA_Z = 2;

        public static final int ORIENTATION_UNKNOWN = -1;
        private boolean sensor_flag = true;
        private boolean mLastSensorFlag = true;

        public static final String TAG = "victure";

        int mLastAngle=-1;
        AngleChangeListener mAngleChangeListener;

        public void setOrientationSensorListener(AngleChangeListener angleChangeListener){
            mAngleChangeListener = angleChangeListener;
        }

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }


        @Override
        public void onSensorChanged(SensorEvent event) {

            float[] values = event.values;

            int orientation = ORIENTATION_UNKNOWN;
            float X = -values[_DATA_X];
            float Y = -values[_DATA_Y];
            float Z = -values[_DATA_Z];

            /**
             * android源码里面拿出来的计算 屏幕旋转的
             */
            float magnitude = X * X + Y * Y;
            // Don't trust the angle if the magnitude is small compared to the y value
            if (magnitude * 4 >= Z * Z) {
                //屏幕旋转时
                float OneEightyOverPi = 57.29577957855f;
                float angle = (float) Math.atan2(-Y, X) * OneEightyOverPi;
                orientation = 90 - (int) Math.round(angle);
                // normalize to 0 - 359 range
                while (orientation >= 360) {
                    orientation -= 360;
                }
                while (orientation < 0) {
                    orientation += 360;
                }
            }

            if (orientation > 225 && orientation < 315) {
                //横屏
                sensor_flag = false;
            } else if ((orientation > 315 && orientation < 360) || (orientation > 0 && orientation < 45)) {
                //竖屏
                sensor_flag = true;
            }
            //NooieLog.d("-->> OrientationSensorListener onSensorChanged orientation=" + orientation + " mLastAngle=" + mLastAngle + " isPortrait=" + sensor_flag);
            if(mLastAngle != orientation){
                mLastAngle=orientation;
                //NooieLog.d("-->> OrientationSensorListener onSensorChanged isPortrait=" + sensor_flag);
                if (mAngleChangeListener != null) {
                    mAngleChangeListener.onChange(orientation);
                }
            }

            if (mLastSensorFlag != sensor_flag) {
                mLastSensorFlag = sensor_flag;
                NooieLog.d("-->> OrientationSensorListener onSensorChanged screen isPortrait=" + sensor_flag);
                if (mAngleChangeListener != null) {
                    mAngleChangeListener.onScreenOrientationChange(sensor_flag);
                }
            }
        }
    }

    public interface AngleChangeListener {
        void onChange(int orientation);

        void onScreenOrientationChange(boolean isPortrait);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                SystemUtil.adjustPhoneVolume(NooieApplication.mCtx,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_SHOW_UI);
                logPhoneVolumeSetting("KEYCODE_VOLUME_UP");
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                SystemUtil.adjustPhoneVolume(NooieApplication.mCtx,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_SHOW_UI);
                logPhoneVolumeSetting("KEYCODE_VOLUME_DOWN");
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void changePhoneVolume(boolean isOpenAudio) {
//        if (!NooieApplication.TEST_MODE) {
//            return;
//        }
        try {
            if (isOpenAudio) {
                PhoneVolume phoneVolume = SystemUtil.getPhoneVolume(NooieApplication.mCtx, AudioManager.STREAM_MUSIC);
                boolean isNeedToRaiseVolume = mPhoneVolume == null && phoneVolume != null && phoneVolume.getVolume() > 0 && phoneVolume.getMaxVolume() > 0 && ((float)phoneVolume.getVolume() / phoneVolume.getMaxVolume()) < DEFAULT_AUDIO_VOLUME_PERCENT;
                if (isNeedToRaiseVolume) {
                    mPhoneVolume = SystemUtil.getPhoneVolume(NooieApplication.mCtx, AudioManager.STREAM_MUSIC);
                    SystemUtil.setPhoneVolumeByPercent(NooieApplication.mCtx, AudioManager.STREAM_MUSIC, DEFAULT_AUDIO_VOLUME_PERCENT, 0);
                }
            } else if (mPhoneVolume != null) {
                int volumeIndex = mPhoneVolume.getVolume() < 0 ? 1 : mPhoneVolume.getVolume();
                SystemUtil.setPhoneVolume(NooieApplication.mCtx, AudioManager.STREAM_MUSIC, volumeIndex, 0);
                mPhoneVolume = null;
            }
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    public boolean checkStartRecordingInvalid(boolean isPlaying, boolean recording) {
        return !recording && !isPlaying;
    }

    private void logPhoneVolumeSetting(String tag) {
        try {
            PhoneVolume phoneVolume = SystemUtil.getPhoneVolume(NooieApplication.mCtx, AudioManager.STREAM_MUSIC);
            if (phoneVolume == null) {
                return;
            }
            NooieLog.d("-->> debug BasePlayerActivity logPhoneVolumeSetting " + tag + " volume cur=" + phoneVolume.getVolume() + " min=" + phoneVolume.getMinVolume() + " max=" + phoneVolume.getMaxVolume());
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }
}
