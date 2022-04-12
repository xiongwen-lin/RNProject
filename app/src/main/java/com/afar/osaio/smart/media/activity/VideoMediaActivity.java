package com.afar.osaio.smart.media.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.base.NooieBaseSupportActivity;
import com.afar.osaio.smart.event.ViewPagerSwitchEvent;
import com.afar.osaio.smart.media.bean.BaseMediaBean;
import com.afar.osaio.smart.media.bean.MediaItemBean;
import com.afar.osaio.smart.media.fragment.PreviewVideoFragment;
import com.afar.osaio.util.DialogUtils;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.time.DateTimeUtil;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.media.bean.BDVideoInfo;
import com.afar.osaio.smart.media.bean.VideoMediaBean;
import com.afar.osaio.util.ConstantValue;
import com.yc.pagerlib.pager.DirectionalViewPager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.yokeyword.eventbusactivityscope.EventBusActivityScope;

public class VideoMediaActivity extends NooieBaseSupportActivity {

    public static void toVideoMediaActivity(Activity from, VideoMediaBean videoBean, int requestCode) {
        Intent intent = new Intent(from, VideoMediaActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM, videoBean);
        from.startActivityForResult(intent, requestCode);
    }

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.vpPreviewVideo)
    DirectionalViewPager vpPreviewVideo;

    VideoMediaBean mVideoMediaBean;
    List<MediaItemBean> mMediaItemBean;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_media);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData() {
        if (getIntent() == null) {
            return;
        }

        mVideoMediaBean = getIntent().getParcelableExtra(ConstantValue.INTENT_KEY_DATA_PARAM);
        mMediaItemBean = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(MediaActivity.getVideoMedias())) {
            mMediaItemBean.addAll(MediaActivity.getVideoMedias());
        }
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_white);
        ivRight.setImageResource(R.drawable.delete_white);


        if (mVideoMediaBean != null) {
            StringBuilder titleSb = new StringBuilder();
            titleSb.append(DateTimeUtil.getUtcMonthDisplayName(NooieApplication.mCtx, mVideoMediaBean.getTime()));
            titleSb.append(" ");
            titleSb.append(DateTimeUtil.getUtcTimeString(mVideoMediaBean.getTime(), DateTimeUtil.PATTERN_D));
            tvTitle.setText(titleSb.toString());
            tvTitle.setTextColor(ContextCompat.getColor(this, R.color.theme_white));

            BDVideoInfo videoInfo = new BDVideoInfo();
            videoInfo.videoPath = mVideoMediaBean.getPath();
        }
        setupViewPager();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideDeleteMediaDialog();
    }

    private void setupViewPager() {
        refreshVideoInfo(mVideoMediaBean);
        List<VideoMediaBean> videoMediaBeans = getAllVideoMediaBeans();
        ArrayList<Fragment> fragments = new ArrayList<>();
        int videoSize = CollectionUtil.size(videoMediaBeans);
        for (int a = 0; a< videoSize ; a++){
            fragments.add(PreviewVideoFragment.newInstant(a, videoMediaBeans.get(a).getPath(), 0));
        }
        int videoIndex = mVideoMediaBean != null && getVideoMediaBeanIndex(mVideoMediaBean, videoMediaBeans) != -1 ? getVideoMediaBeanIndex(mVideoMediaBean, videoMediaBeans) : 0;
        vpPreviewVideo.setOffscreenPageLimit(1);
        vpPreviewVideo.setOrientation(DirectionalViewPager.HORIZONTAL);
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(fragments, supportFragmentManager);
        vpPreviewVideo.setAdapter(myPagerAdapter);
        vpPreviewVideo.setCurrentItem(videoIndex);
        vpPreviewVideo.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (isDestroyed()) {
                    return;
                }
                NooieLog.d("-->> PreviewVideoFragment onPageSelected position=" + position);
                EventBusActivityScope.getDefault(VideoMediaActivity.this).post(new ViewPagerSwitchEvent(ViewPagerSwitchEvent.VIEWPAGER_SWITCH_OF_PREVIEW_VIDEO, position, true));
                if (CollectionUtil.isNotEmpty(videoMediaBeans) && CollectionUtil.isIndexSafe(position, videoMediaBeans.size())) {
                    mVideoMediaBean = videoMediaBeans.get(position);
                    refreshVideoInfo(mVideoMediaBean);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void refreshVideoInfo(VideoMediaBean videoMediaBean) {
        if (isDestroyed() || videoMediaBean == null || tvTitle == null) {
            return;
        }

        StringBuilder titleSb = new StringBuilder();
        titleSb.append(DateTimeUtil.getUtcMonthDisplayName(NooieApplication.mCtx, videoMediaBean.getTime()));
        titleSb.append(" ");
        titleSb.append(DateTimeUtil.getUtcTimeString(videoMediaBean.getTime(), DateTimeUtil.PATTERN_D));
        tvTitle.setText(titleSb.toString());
        tvTitle.setTextColor(ContextCompat.getColor(this, R.color.theme_white));
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.ivRight:
                showDeleteMediaDialog();
                break;
        }
    }

    private void deleteMedia() {
        Bundle bundle = new Bundle();
        if (mVideoMediaBean != null) {
            bundle.putString(ConstantValue.INTENT_KEY_DATA_ID, mVideoMediaBean.getPath());
        }
        finishForResult(bundle);
    }

    private List<VideoMediaBean> getAllVideoMediaBeans() {
        List<VideoMediaBean> videoMediaBeans = new ArrayList<>();
        if (CollectionUtil.isEmpty(mMediaItemBean)) {
            return videoMediaBeans;
        }
        for (MediaItemBean mediaItemBean : CollectionUtil.safeFor(mMediaItemBean)) {
            if (mediaItemBean != null && CollectionUtil.isNotEmpty(mediaItemBean.getMedias())) {
                for (BaseMediaBean mediaBean : CollectionUtil.safeFor(mediaItemBean.getMedias())) {
                    if (mediaBean != null) {
                        videoMediaBeans.add((VideoMediaBean)mediaBean);
                    }
                }
            }
        }
        return videoMediaBeans;
    }

    private int getVideoMediaBeanIndex(VideoMediaBean videoMediaBean, List<VideoMediaBean> videoMediaBeans) {
        int index = -1;
        if (videoMediaBean == null || CollectionUtil.isEmpty(videoMediaBeans)) {
            return index;
        }
        index = 0;
        int size = CollectionUtil.size(videoMediaBeans);
        for (int i = 0; i < size; i++) {
            if (videoMediaBeans.get(i) != null && !TextUtils.isEmpty(videoMediaBeans.get(i).getPath()) && videoMediaBeans.get(i).getPath().equalsIgnoreCase(videoMediaBean.getPath())) {
                index = i;
                break;
            }
        }
        return index;
    }

    private Dialog mDeleteMediaDialog;

    private void showDeleteMediaDialog() {
        hideDeleteMediaDialog();
        mDeleteMediaDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.media_delete_video_title, R.string.media_delete_video_content, R.string.cancel, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                deleteMedia();
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void hideDeleteMediaDialog() {
        if (mDeleteMediaDialog != null) {
            mDeleteMediaDialog.dismiss();
            mDeleteMediaDialog = null;
        }
    }

    class MyPagerAdapter extends FragmentStatePagerAdapter {

        private ArrayList<Fragment> list;

        public MyPagerAdapter(ArrayList<Fragment> list , FragmentManager fm){
            super(fm);
            this.list = list;
        }

        @Override
        public Fragment getItem(int i) {
            return list.get(i);
        }

        @Override
        public int getCount() {
            return list!=null ? list.size() : 0;
        }
    }
}
