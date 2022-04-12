package com.afar.osaio.smart.home.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.message.activity.MessageActivity;
import com.afar.osaio.smart.electrician.adapter.BannerHolderView;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieBaseMainFragment;
import com.afar.osaio.smart.device.bean.DeviceInfo;
import com.afar.osaio.message.activity.adapter.MessageAdapter;
import com.afar.osaio.message.bean.InboxBean;
import com.afar.osaio.message.bean.MsgUnreadInfo;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.event.TabSelectedEvent;
import com.afar.osaio.smart.home.contract.MessageContract;
import com.afar.osaio.smart.home.presenter.MessagePresenter;
import com.nooie.sdk.api.network.base.bean.entity.BannerResult;
import com.nooie.sdk.bean.SDKConstant;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.yokeyword.eventbusactivityscope.EventBusActivityScope;

public class MessageFragment extends NooieBaseMainFragment implements MessageContract.View {

    public static MessageFragment newInstance() {
        Bundle args = new Bundle();
        MessageFragment fragment = new MessageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /* @BindView(R.id.ivLeft)
     ImageView ivLeft;
     @BindView(R.id.tvTitle)
     TextView tvTitle;
     @BindView(R.id.ivRight)
     ImageView ivRight;*/
    @BindView(R.id.rcvMessage)
    RecyclerView rcvMessage;
    @BindView(R.id.cbAdvertise)
    ConvenientBanner cbAdvertise;
    @BindView(R.id.ivCancel)
    ImageView ivCancel;

    private MessageContract.Presenter mPresenter;
    private MessageAdapter mMessageAdapter;
    private List<InboxBean> mDatas = new ArrayList<>();
    private List<String> mIds = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_new, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /*ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivLeft.setVisibility(View.INVISIBLE);
        tvTitle.setText(R.string.home_tab_label_message);*/
        rcvMessage.setLayoutManager(new LinearLayoutManager(getContext()));
        mMessageAdapter = new MessageAdapter(getContext());
        rcvMessage.setAdapter(mMessageAdapter);
        if (mPresenter != null) {
            mPresenter.loadBanner(mUid);
        }
    }

    private void initData() {
        new MessagePresenter(this);
    }

    private void initView(View view) {
        ButterKnife.bind(this, view);
        EventBusActivityScope.getDefault(_mActivity).register(this);
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        NooieLog.d("-->> debug MessageFragment onResume: ");
        if (cbAdvertise != null) {
            cbAdvertise.startTurning(7000L);
        }
        refreshData();
    }

    @Override
    public void onPause() {
        super.onPause();
        NooieLog.d("-->> debug MessageFragment onPause: ");
        if (cbAdvertise != null) {
            cbAdvertise.stopTurning();
        }
    }

    private void refreshData() {
        mDatas.clear();
        mIds.clear();
        if (ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
            refreshMessageIndexView(mDatas);
            return;
        }
        mDatas.add(new InboxBean(MessageAdapter.SYSTEM_MESSAGE_ID, getString(R.string.system_message), 0));
        List<DeviceInfo> devices = NooieDeviceHelper.getAllDeviceInfo();
        if (CollectionUtil.isNotEmpty(devices)) {
            NooieDeviceHelper.sortDeviceInfo(devices);
        }
        for (DeviceInfo deviceInfo : CollectionUtil.safeFor(devices)) {
            InboxBean inboxBean = new InboxBean(deviceInfo.getNooieDevice().getUuid(), deviceInfo.getNooieDevice().getName(), 0);
            mDatas.add(inboxBean);
            mIds.add(deviceInfo.getNooieDevice().getUuid());
        }
        refreshMessageIndexView(mDatas);
        if (mPresenter != null) {
            mPresenter.loadMsgUnread(mIds);
        }
    }

    private void refreshMessageIndexView(List<InboxBean> inboxBeans) {
        if (mMessageAdapter != null) {
            mMessageAdapter.setData(inboxBeans);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBusActivityScope.getDefault(_mActivity).unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
    }

    @OnClick({/*R.id.ivLeft,*/ R.id.ivCancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
           /* case R.id.ivLeft:
                ((MessageActivity) _mActivity).onClickNavLeft();
                break;*/
            case R.id.ivCancel: {
                cbAdvertise.setVisibility(View.GONE);
                ivCancel.setVisibility(View.GONE);
                break;
            }
        }
    }

   /* @Override
    public boolean onBackPressedSupport() {
        ((MessageActivity) _mActivity).onClickNavLeft();
        return true;
    }*/

    @Override
    public void setPresenter(MessageContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Subscribe
    public void onTabSelectedEvent(TabSelectedEvent event) {
        NooieLog.d("-->> debug MessageFragment onTabSelectedEvent: position=" + (event != null ? event.position : -1));
        if (event == null || event.position != HomeFragment.SECOND) {
            return;
        }
        refreshData();
    }

    @Override
    public void notifyGetUnreadMsgFailed(String message) {
    }

    @Override
    public void notifyGetUnreadMsgSuccess(MsgUnreadInfo info) {

        if (info == null) {
            return;
        }

        int mTotalUnreadCount = info.getSystemUnreadCount();
        updateData(MessageAdapter.SYSTEM_MESSAGE_ID, info.getSystemUnreadCount());
        for (int i = 0; i < info.getDevMsgUnreadInfos().size(); i++) {
            mTotalUnreadCount += info.getDevMsgUnreadInfos().get(i).getUnreadCount();
            updateData(info.getDevMsgUnreadInfos().get(i).getId(), info.getDevMsgUnreadInfos().get(i).getUnreadCount());
        }

        refreshMessageIndexView(mDatas);

        //EventBusActivityScope.getDefault(_mActivity).post(new MsgCountUpdateEvent(mTotalUnreadCount));
    }

    @Override
    public void onLoadBannerSuccess(List<BannerResult.BannerInfo> bannerList) {
        List<String> urlList = new ArrayList<>();
        for (BannerResult.BannerInfo bannerInfo : bannerList) {
            NooieLog.e("--------onLoadBannerSuccess url " + bannerInfo.getImg_url());
            if (bannerInfo.getImg_url() != null) {
                urlList.add(bannerInfo.getImg_url());
            }
        }
        cbAdvertise.setPages(new CBViewHolderCreator() {
            @Override
            public Holder createHolder(View itemView) {
                return new BannerHolderView(itemView);
            }

            @Override
            public int getLayoutId() {
                return R.layout.item_image;
            }
        }, urlList)
                //设置两个点图片作为翻页指示器，不设置则没有指示器，可以根据自己需求自行配合自己的指示器，不需要圆点指示器可以不设
                .setPageIndicator(new int[]{R.drawable.point_gray, R.drawable.point_black})
                //设置指示器的位置（左、中、右）
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
                //设置指示器是否可见
                .setPointViewVisible(true)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        if (!TextUtils.isEmpty(bannerList.get(position).getJump_url())) {
                            NooieLog.e(urlList.get(position) + "  jump_url=" + bannerList.get(position).getJump_url());
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(bannerList.get(position).getJump_url())));
                        }
                    }
                });
        if (urlList != null) {
            ivCancel.setVisibility(View.VISIBLE);
            cbAdvertise.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoadBannerFail(String msg) {

    }

    private void updateData(String deviceId, int unreadCount) {
        if (TextUtils.isEmpty(deviceId) || CollectionUtil.isEmpty(mDatas)) {
            return;
        }
        for (int i = 0; i < mDatas.size(); i++) {
            if (mDatas.get(i) != null && mDatas.get(i).getId() != null && mDatas.get(i).getId().equalsIgnoreCase(deviceId)) {
                mDatas.get(i).setUnreadCount(unreadCount);
            }
        }
    }

    @Override
    public void updateGlobalData(String action) {
        if (checkNull(mDatas, mIds, mMessageAdapter)) {
            return;
        }
        if (SDKConstant.ACTION_UPDATE_GLOBAL_DATA_FOR_LOGOUT.equalsIgnoreCase(action)) {
            mDatas.clear();
            mIds.clear();
            refreshMessageIndexView(mDatas);
            //EventBusActivityScope.getDefault(_mActivity).post(new MsgCountUpdateEvent(0));
        }
    }

}
