package com.afar.osaio.smart.home.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.base.NooieBaseSupportActivity;
import com.afar.osaio.base.NooieBaseSupportFragment;
import com.afar.osaio.smart.electrician.fragment.SmartHomeFragment;
import com.afar.osaio.smart.event.MsgCountUpdateEvent;
import com.afar.osaio.smart.event.TabSelectedEvent;
import com.afar.osaio.smart.event.TabSwitchEvent;
import com.afar.osaio.util.CompatUtil;
import com.alibaba.android.arouter.launcher.ARouter;
import com.apemans.platformbridge.TestMessageActivity;
import com.apemans.platformbridge.TestUserFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.majiajie.pagerbottomtabstrip.NavigationController;
import me.majiajie.pagerbottomtabstrip.PageNavigationView;
import me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectedListener;
import me.yokeyword.eventbusactivityscope.EventBusActivityScope;
import me.yokeyword.fragmentation.base.SupportFragment;

public class HomeFragment extends NooieBaseSupportFragment {

    private static final int REQ_MSG = 10;

    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int THIRD = 2;
    public static final int FOUR = 3;
    public static final int FINE = 4;

    //底部栏tab的个数，与支持显示fragment页面数一致
    private static final int TAB_COUNT = 3;

    private SupportFragment[] mFragments = new SupportFragment[TAB_COUNT];

    @BindView(R.id.navBottomBar)
    PageNavigationView navBottomBar;

    private int mCurrentPosition = FIRST;
    NavigationController mNavigationController;

    Fragment fragmentMine;

    public static HomeFragment newInstance() {

        Bundle args = new Bundle();

        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SupportFragment firstFragment = findChildFragment(SmartHomeFragment.class);

        if (firstFragment == null) {
           /* mFragments[FIRST] = new SmartHomeFragment();
            mFragments[SECOND] = DeviceListFragment.newInstance();*/
            //mFragments[FIRST] = DeviceListFragment.newInstance();
            /*mFragments[FIRST] = NooieApplication.TEST_MODE ? SmartDeviceListFragment.newInstance() : DeviceListFragment.newInstance();
            mFragments[SECOND] = NooieApplication.TEST_MODE ? HomeTestFragment.newInstance() : new SmartHomeFragment();
            mFragments[THIRD] = MessageFragment.newInstance();
            mFragments[FOUR] = PersonFragment.newInstance();*/
            mFragments[FIRST] = SmartDeviceListFragment.newInstance();
            mFragments[SECOND] = MessageFragment.newInstance();
            mFragments[THIRD] = /*(SupportFragment) fragmentMine*/PersonFragment.newInstance();

            loadMultipleRootFragment(R.id.fl_tab_container, FIRST,
                    mFragments[FIRST],
                    mFragments[SECOND],
                    mFragments[THIRD]);
            //mFragments[FOUR]);
        } else {
            // 这里库已经做了Fragment恢复,所有不需要额外的处理了, 不会出现重叠问题
            // 这里我们需要拿到mFragments的引用
           /* mFragments[FIRST] = findChildFragment(SmartHomeFragment.class);
            mFragments[SECOND] = findChildFragment(DeviceListFragment.class);*/
            //mFragments[FIRST] = findChildFragment(DeviceListFragment.class);
          /*  mFragments[FIRST] = NooieApplication.TEST_MODE ? findChildFragment(SmartDeviceListFragment.class) : findChildFragment(DeviceListFragment.class);
            mFragments[SECOND] = NooieApplication.TEST_MODE ? findChildFragment(HomeTestFragment.class) : findChildFragment(SmartHomeFragment.class);
            mFragments[THIRD] = findChildFragment(MessageFragment.class);
            mFragments[FOUR] = findChildFragment(PersonFragment.class);*/
            mFragments[FIRST] = findChildFragment(SmartDeviceListFragment.class);
            mFragments[SECOND] = findChildFragment(MessageFragment.class);
            mFragments[THIRD] = findChildFragment(PersonFragment.class);
        }
    }

    private void initView(View view) {
        ButterKnife.bind(this, view);
        setupNavBottomBar();
        fragmentMine = TestUserFragment.INSTANCE.getUserFragment();
        EventBusActivityScope.getDefault(_mActivity).register(this);
    }

    private void setupNavBottomBar() {
        mNavigationController = navBottomBar.material()
                //.addItem(R.drawable.tab_camera_gray, R.drawable.tab_camera_active, getString(R.string.home_tab_label_camera), CompatUtil.getColor(NooieApplication.mCtx, R.color.theme_green))
                .addItem(R.drawable.tab_home_gray, R.drawable.tab_home_active, getString(R.string.device), CompatUtil.getColor(NooieApplication.mCtx, R.color.theme_green))
                .addItem(R.drawable.tab_message_gray, R.drawable.tab_message_active, getString(R.string.home_tab_label_message), CompatUtil.getColor(NooieApplication.mCtx, R.color.theme_green))
                //.addItem(R.drawable.tab_store_gray, R.drawable.tab_store_active, getString(R.string.home_tab_label_store), CompatUtil.getColor(NooieApplication.mCtx, R.color.theme_green))
                .addItem(R.drawable.tab_person_gray, R.drawable.tab_person_active, getString(R.string.home_tab_label_person), CompatUtil.getColor(NooieApplication.mCtx, R.color.theme_green))
                .setDefaultColor(CompatUtil.getColor(NooieApplication.mCtx, R.color.theme_text_color))//未选中状态的颜色
                .dontTintIcon()
                //.setMode(MaterialMode.HIDE_TEXT)//这里可以设置样式模式，总共可以组合出4种效果
                .build();

        // 设置Item选中事件的监听
        mNavigationController.addTabItemSelectedListener(new OnTabItemSelectedListener() {
            @Override
            public void onSelected(int index, int old) {
                //NooieLog.d("-->> HomeFragment onSelected index=" + index + " old=" + old);
                boolean isGoToLogin = (index == SECOND || index == THIRD || index == FOUR) && !((NooieBaseSupportActivity) _mActivity).checkLogin("", "");
                if (isGoToLogin) {
                    return;
                }
                tabBottomBar(index);
            }

            @Override
            public void onRepeat(int index) {
                //NooieLog.d("-->> HomeFragment onRepeat index=" + index);
            }
        });
    }

    private void tabBottomBar(int position) {
        switch (position) {
            case FIRST:
                if (mCurrentPosition == FIRST) {
                    break;
                }
                showHideFragment(mFragments[FIRST], mFragments[mCurrentPosition]);
                mCurrentPosition = FIRST;
                break;
            case SECOND:
                if (mCurrentPosition == SECOND) {
                    break;
                }
                showHideFragment(mFragments[SECOND], mFragments[mCurrentPosition]);
                mCurrentPosition = SECOND;
                break;
            case THIRD:
//                ARouter.getInstance().build("/user/account")
//                        .navigation();
//                TestMessageActivity.Companion.start();
                if (mCurrentPosition == THIRD) {
                    break;
                }
                showHideFragment(mFragments[THIRD], mFragments[mCurrentPosition]);
                mCurrentPosition = THIRD;
                break;
            /*case FOUR:
                if (mCurrentPosition == FOUR) {
                    break;
                }
                showHideFragment(mFragments[FOUR], mFragments[mCurrentPosition]);
                mCurrentPosition = FOUR;
                break;*/
        }
        EventBusActivityScope.getDefault(_mActivity).post(new TabSelectedEvent(position));
        EventBus.getDefault().post(new TabSelectedEvent(position));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBusActivityScope.getDefault(_mActivity).unregister(this);
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        super.onFragmentResult(requestCode, resultCode, data);
        if (requestCode == REQ_MSG && resultCode == RESULT_OK) {
        }
    }

    @Subscribe
    public void onMsgCountUpdate(MsgCountUpdateEvent event) {
        int count = event != null ? event.count : 0;
        if (mNavigationController != null) {
            //mNavigationController.setHasMessage(THIRD, count > 0);
            mNavigationController.setHasMessage(SECOND, count > 0);
        }
    }

    @Subscribe
    public void onTabSwitch(TabSwitchEvent event) {
        int position = event != null ? event.position : FIRST;
        if (mNavigationController != null) {
            mNavigationController.setSelect(position);
        }
    }

    /**
     * start other BrotherFragment
     */
    public void startBrotherFragment(SupportFragment targetFragment) {
        start(targetFragment);
    }
}
