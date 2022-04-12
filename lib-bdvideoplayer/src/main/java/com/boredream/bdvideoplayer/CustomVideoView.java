package com.boredream.bdvideoplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.boredream.bdvideoplayer.BDVideoPlayer;
import com.boredream.bdvideoplayer.R;
import com.boredream.bdvideoplayer.bean.IVideoInfo;
import com.boredream.bdvideoplayer.listener.CustomVideoControlListener;
import com.boredream.bdvideoplayer.listener.OnVideoControlListener;
import com.boredream.bdvideoplayer.listener.SimplePlayerCallback;
import com.boredream.bdvideoplayer.utils.NetworkUtils;
import com.boredream.bdvideoplayer.view.VideoBehaviorView;
import com.boredream.bdvideoplayer.view.VideoControllerView;
import com.boredream.bdvideoplayer.view.VideoProgressOverlay;
import com.boredream.bdvideoplayer.view.VideoSystemOverlay;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * 视频播放器View
 */
public class CustomVideoView extends VideoBehaviorView {

    private SurfaceView mSurfaceView;
    private View mLoading;
    private VideoControllerView mediaController;
    private VideoSystemOverlay videoSystemOverlay;
    private VideoProgressOverlay videoProgressOverlay;
    private BDVideoPlayer mMediaPlayer;
    private ImageView videoThumbnail;

    private int initWidth;
    private int initHeight;

    public boolean isLock() {
        return mediaController.isLock();
    }

    public CustomVideoView(Context context) {
        super(context);
        init();
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.custom_video_view, this);

        mSurfaceView = (SurfaceView) findViewById(R.id.video_surface);
        mLoading = findViewById(R.id.video_loading);
        mediaController = (VideoControllerView) findViewById(R.id.video_controller);
        videoSystemOverlay = (VideoSystemOverlay) findViewById(R.id.video_system_overlay);
        videoProgressOverlay = (VideoProgressOverlay) findViewById(R.id.video_progress_overlay);
        videoThumbnail = (ImageView) findViewById(R.id.video_thumbnail);

        initPlayer();

        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initWidth = getWidth();
                initHeight = getHeight();

                if (mSurfaceView != null) {
                    mSurfaceView.invalidate();
                }

                if (mMediaPlayer != null) {
                    mMediaPlayer.setDisplay(holder);
                    mMediaPlayer.openVideo(false);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });

        registerNetChangedReceiver();
        setVideoControlListener(false);
    }

    private void initPlayer() {
        mMediaPlayer = new BDVideoPlayer();
        mMediaPlayer.setCallback(new SimplePlayerCallback() {

            @Override
            public void onStateChanged(int curState) {
                switch (curState) {
                    case BDVideoPlayer.STATE_IDLE:
                        am.abandonAudioFocus(null);
                        break;
                    case BDVideoPlayer.STATE_PREPARING:
                        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                        break;
                }
            }

            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaController.updatePausePlay();
                resetPlayer();
            }

            @Override
            public void onError(MediaPlayer mp, int what, int extra) {
                mediaController.checkShowError(false);
            }

            @Override
            public void onLoadingChanged(boolean isShow) {
                if (isShow) {
                    showLoading();
                } else {
                    hideLoading();
                }
            }

            @Override
            public void onPrepared(MediaPlayer mp, boolean isAutoStart) {
                if (isAutoStart) {
                    mMediaPlayer.start();
                } else {
                    videoThumbnail.setVisibility(VISIBLE);
                }
                mediaController.show();
                mediaController.displayStartBtn(true);
                mediaController.hideErrorView();
            }
        });
        mediaController.setMediaPlayer(mMediaPlayer);
    }

    private void showLoading() {
        mLoading.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        mLoading.setVisibility(View.GONE);
    }

    private boolean isBackgroundPause;

    public void onStop() {
        if (mMediaPlayer.isPlaying()) {
            // 如果已经开始且在播放，则暂停同时记录状态
            isBackgroundPause = true;
            mMediaPlayer.pause();
        }
    }

    public void onStart() {
        if (isBackgroundPause) {
            // 如果切换到后台暂停，后又切回来，则继续播放
            isBackgroundPause = false;
            mMediaPlayer.start();
        }
    }

    public void onDestroy() {
        //mMediaPlayer.stop();
        mMediaPlayer.reset();
        mediaController.release();
        unRegisterNetChangedReceiver();
    }

    public void resetPlayer() {
        if (mMediaPlayer != null && (mMediaPlayer.isPlaying() || mMediaPlayer.isPlayFinish())) {
            mMediaPlayer.reset();
            mMediaPlayer.openVideo(false);
        }
    }

    public void preparePlayVideo(final IVideoInfo video) {
        if (video == null) {
            return;
        }

        mMediaPlayer.reset();

        String videoPath = video.getVideoPath();
        mediaController.setVideoInfo(video);
        mMediaPlayer.setVideoPath(videoPath);
    }

    /**
     * 开始播放
     */
    public void startPlayVideo(final IVideoInfo video) {
        if (video == null) {
            return;
        }

        mMediaPlayer.reset();

        String videoPath = video.getVideoPath();
        mediaController.setVideoInfo(video);
        mMediaPlayer.setVideoPath(videoPath);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        mediaController.toggleDisplay();
        return super.onSingleTapUp(e);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (isLock()) {
            return false;
        }
        return super.onDown(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (isLock()) {
            return false;
        }
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    protected void endGesture(int behaviorType) {
        switch (behaviorType) {
            case VideoBehaviorView.FINGER_BEHAVIOR_BRIGHTNESS:
            case VideoBehaviorView.FINGER_BEHAVIOR_VOLUME:
                Log.i("DDD", "endGesture: left right");
                videoSystemOverlay.hide();
                break;
            case VideoBehaviorView.FINGER_BEHAVIOR_PROGRESS:
                Log.i("DDD", "endGesture: bottom");
                mMediaPlayer.seekTo(videoProgressOverlay.getTargetProgress());
                videoProgressOverlay.hide();
                break;
        }
    }

    @Override
    protected void updateSeekUI(int delProgress) {
        //屏蔽进度UI
        //videoProgressOverlay.show(delProgress, mMediaPlayer.getCurrentPosition(), mMediaPlayer.getDuration());
    }

    @Override
    protected void updateVolumeUI(int max, int progress) {
        //屏蔽声音大小UI
        //videoSystemOverlay.show(VideoSystemOverlay.SystemType.VOLUME, max, progress);
    }

    @Override
    protected void updateLightUI(int max, int progress) {
        //屏蔽亮度UI
        //videoSystemOverlay.show(VideoSystemOverlay.SystemType.BRIGHTNESS, max, progress);
    }

    public void setOnVideoControlListener(OnVideoControlListener onVideoControlListener) {
        mediaController.setOnVideoControlListener(onVideoControlListener);
    }

    public void setVideoControlListener(boolean isRelease) {
        if (mediaController != null) {
            mediaController.setVideoControlListener(isRelease ? null : new CustomVideoControlListener() {
                @Override
                public void onClickStart() {
                    if (videoThumbnail != null) {
                        videoThumbnail.setVisibility(GONE);
                    }
                }

                @Override
                public void onClickPause() {
                }
            });
        }
    }

    public void setVideoThumbnail(String path) {
        if (TextUtils.isEmpty(path) || videoThumbnail == null) {
            return;
        }
        Glide.with(getContext())
                .load(Uri.fromFile(new File(path)))
                .apply(new RequestOptions()
                        .format(DecodeFormat.PREFER_RGB_565)
                )
                .transition(withCrossFade())
                .into(videoThumbnail);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getLayoutParams().width = initWidth;
            getLayoutParams().height = initHeight;
        } else {
            getLayoutParams().width = LayoutParams.MATCH_PARENT;
            getLayoutParams().height = LayoutParams.MATCH_PARENT;
        }

    }

    private NetChangedReceiver netChangedReceiver;

    public void registerNetChangedReceiver() {
        if (netChangedReceiver == null) {
            netChangedReceiver = new NetChangedReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            activity.registerReceiver(netChangedReceiver, filter);
        }
    }

    public void unRegisterNetChangedReceiver() {
        if (netChangedReceiver != null) {
            activity.unregisterReceiver(netChangedReceiver);
        }
    }

    private class NetChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Parcelable extra = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (extra != null && extra instanceof NetworkInfo) {
                NetworkInfo netInfo = (NetworkInfo) extra;

                if (NetworkUtils.isNetworkConnected(context) && netInfo.getState() != NetworkInfo.State.CONNECTED) {
                    // 网络连接的情况下只处理连接完成状态
                    return;
                }

                mediaController.checkShowError(true);
            }
        }
    }
}
