package com.afar.osaio.smart.media.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.boredream.bdvideoplayer.CustomVideoView;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieBaseSupportFragment;
import com.afar.osaio.smart.event.ViewPagerSwitchEvent;
import com.afar.osaio.smart.media.bean.BDVideoInfo;
import com.nooie.common.utils.log.NooieLog;

import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.yokeyword.eventbusactivityscope.EventBusActivityScope;

public class PreviewVideoFragment extends NooieBaseSupportFragment {

    @BindView(R.id.bdvPreviewVideo)
    CustomVideoView bdvPreviewVideo;

    private int mPosition;
    private String url;
    private int image;
    private int index;

    public static Fragment newInstant(int position, String url , int image){
        PreviewVideoFragment videoFragment = new PreviewVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putString("url",url);
        bundle.putInt("image",image);
        videoFragment.setArguments(bundle);
        return videoFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("debug","-->> PreviewVideoFragment onCreate");
        Bundle arguments = getArguments();
        if (arguments != null) {
            mPosition = arguments.getInt("position");
            url = arguments.getString("url");
            image = arguments.getInt("image");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d("debug","-->> PreviewVideoFragment onCreateView");
        View view = inflater.inflate(R.layout.fragment_preview_video, container, false);
        ButterKnife.bind(this, view);
        EventBusActivityScope.getDefault(_mActivity).register(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("debug","-->> PreviewVideoFragment onViewCreated");
        BDVideoInfo videoInfo = new BDVideoInfo();
        videoInfo.videoPath = url;
        bdvPreviewVideo.setVideoThumbnail(videoInfo.videoPath);
        bdvPreviewVideo.preparePlayVideo(videoInfo);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("debug","-->> PreviewVideoFragment onActivityCreated");
        Log.d("初始化操作","------"+index++);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("debug","-->> PreviewVideoFragment onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("debug","-->> PreviewVideoFragment onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("debug","-->> PreviewVideoFragment onPause");
        if (bdvPreviewVideo != null) {
            bdvPreviewVideo.resetPlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("debug","-->> PreviewVideoFragment onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("debug","-->> PreviewVideoFragment onDestroyView");
        EventBusActivityScope.getDefault(_mActivity).unregister(this);
        if (bdvPreviewVideo != null) {
            bdvPreviewVideo.onDestroy();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("debug","-->> PreviewVideoFragment onDestroy");
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        Log.d("debug","-->> PreviewVideoFragment onSupportVisible");
    }

    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
        Log.d("debug","-->> PreviewVideoFragment onSupportInvisible");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d("debug","-->> PreviewVideoFragment setUserVisibleHint isVisibleToUser=" + isVisibleToUser);
    }

    @Subscribe
    public void onPageSwitch(ViewPagerSwitchEvent event) {
        if (event != null) {
            NooieLog.d("-->> PreviewVideoFragment onPageSwitch event type=" + event.type + " position=" + event.position + " current position=" + mPosition);
            if (event.type == ViewPagerSwitchEvent.VIEWPAGER_SWITCH_OF_PREVIEW_VIDEO && event.position != mPosition && bdvPreviewVideo != null) {
                bdvPreviewVideo.resetPlayer();
            }
        }
    }

}
