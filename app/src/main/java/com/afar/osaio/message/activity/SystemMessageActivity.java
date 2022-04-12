package com.afar.osaio.message.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.message.activity.adapter.SystemMessageAdapter;
import com.afar.osaio.message.bean.HandleMessageType;
import com.afar.osaio.message.bean.SystemMessage;
import com.afar.osaio.message.model.RefreshType;
import com.afar.osaio.message.presenter.ISystemMessagePresenter;
import com.afar.osaio.message.presenter.SystemMessagePresenterImpl;
import com.afar.osaio.message.view.ISystemMessageView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.nooie.common.utils.notify.NotificationUtil;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by victor on 2018/7/9
 * Email is victor.qiao.0604@gmail.com
 */
public class SystemMessageActivity extends BaseActivity implements ISystemMessageView, OnRefreshListener,
        OnLoadMoreListener, SystemMessageAdapter.OnClickMessageListener {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.swipe_target)
    RecyclerView swipeTarget;
    @BindView(R.id.swipeToLoadLayout)
    SwipeToLoadLayout swipeToLoadLayout;
    @BindView(R.id.vSystemMsgTimeLine)
    View vSystemMsgTimeLine;

    private long mEarliestMsgTime; // 当前消息列表最早那条消息的时间
    private RefreshType mRefreshType;
    private ISystemMessagePresenter mMessagePresenter;
    private SystemMessageAdapter mSystemMessageAdapter;

    public static void toSystemMessageActivity(Context from) {
        Intent intent = new Intent(from, SystemMessageActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_message);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ivRight.setEnabled(false);
        ivRight.setVisibility(View.GONE);
        showLoading();
        mRefreshType = RefreshType.SET_DATA;
        mMessagePresenter.loadSystemMessage(0, System.currentTimeMillis() + 1000 * 60 * 10, 30);
        mMessagePresenter.setSystemMsgReadState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRefresh();
        stopLoadMore();
    }

    private void initData() {
        mMessagePresenter = new SystemMessagePresenterImpl(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.system_message);
        ivRight.setImageResource(R.drawable.delete_icon_state_list);

        swipeToLoadLayout.setOnRefreshListener(this);
        swipeToLoadLayout.setOnLoadMoreListener(this);

        mSystemMessageAdapter = new SystemMessageAdapter(this);
        swipeTarget.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        swipeTarget.setAdapter(mSystemMessageAdapter);
        mSystemMessageAdapter.setOnClickMessageListener(this);
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.ivRight:
                DialogUtils.showConfirmWithSubMsgDialog(this, R.string.message_delete_all_message, R.string.message_delete_all_message_sub_msg,
                        R.string.settings_no_delete, R.string.confirm_upper, mClickConfirmDialogBtnListener);
                break;
        }
    }

    private DialogUtils.OnClickConfirmButtonListener mClickConfirmDialogBtnListener = new DialogUtils.OnClickConfirmButtonListener() {
        @Override
        public void onClickRight() {
            mMessagePresenter.deleteSystemMessages(true, new ArrayList<String>());
        }

        @Override
        public void onClickLeft() {

        }
    };

    private void startRefresh() {
        swipeToLoadLayout.setRefreshing(true);
    }

    private void stopRefresh() {
        if (swipeToLoadLayout.isRefreshing())
            swipeToLoadLayout.setRefreshing(false);
    }

    private void startLoadMore() {
        swipeToLoadLayout.setLoadingMore(true);
    }

    private void stopLoadMore() {
        if (swipeToLoadLayout.isLoadingMore())
            swipeToLoadLayout.setLoadingMore(false);
    }

    @Override
    public void onClickRefuse(final SystemMessage message) {
        if (isPause()) return;

        DialogUtils.showConfirmWithSubMsgDialog(this, R.string.message_reject_confirm, R.string.message_reject_confirm_info, R.string.cancel, R.string.reject, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                mMessagePresenter.updateShareMsgState(Integer.parseInt(message.getId()), message.getShareId(), ApiConstant.SYS_MSG_SHARE_STATUS_REJECT);
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    @Override
    public void onClickAgree(final SystemMessage message) {
        if (isPause()) return;

        DialogUtils.showConfirmWithSubMsgDialog(this, R.string.message_accept_confirm, R.string.message_accept_confirm_info, R.string.cancel, R.string.accept, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                mMessagePresenter.updateShareMsgState(Integer.parseInt(message.getId()), message.getShareId(), ApiConstant.SYS_MSG_SHARE_STATUS_ACCEPT);
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    @Override
    public void onClickMessage(SystemMessage message) {
        if (isPause()) return;
    }

    @Override
    public void onRefresh() {
        mRefreshType = RefreshType.SET_DATA;
        mMessagePresenter.loadSystemMessage(0, System.currentTimeMillis() + 1000 * 60 * 10, 30);
    }

    @Override
    public void onLoadMore() {
        mRefreshType = RefreshType.LOAD_MORE;
        mMessagePresenter.loadSystemMessage(1, mEarliestMsgTime, 30);
    }

    @Override
    public void onLoadSystemMessage(@NonNull List<SystemMessage> messages) {
        if (isPause()) return;

        if (mRefreshType == RefreshType.SET_DATA) {
            hideLoading();
            if (messages.size() > 0) {
                mEarliestMsgTime = messages.get(messages.size() - 1).getUtcTime() - 1;
               /* ivRight.setEnabled(true);
                ivRight.setVisibility(View.VISIBLE);*/
            } else {
               /* ivRight.setEnabled(false);
                ivRight.setVisibility(View.GONE);*/
            }
            mSystemMessageAdapter.setDataSet(messages);
            stopRefresh();
        } else if (mRefreshType == RefreshType.LOAD_MORE) {
            if (messages.size() == 0) {
                ToastUtil.showToast(this, R.string.message_no_more);
            } else {
                mEarliestMsgTime = messages.get(messages.size() - 1).getUtcTime() - 1;
                mSystemMessageAdapter.insertItemsFromTail(messages);
            }
            stopLoadMore();
        }
    }

    @Override
    public void onHandleSuccess(HandleMessageType type) {
        if (isDestroyed()) {
            return;
        }

        switch (type) {
            case TYPE_SYSTEM_SHARED:
                Intent intent = new Intent(ConstantValue.BROADCAST_KEY_UPDATE_CAMERA);
                NotificationUtil.sendBroadcast(NooieApplication.mCtx, intent);
                break;
        }
        startRefresh();
    }

    @Override
    public void onHandleFailed(String message, int code) {
        if (isDestroyed()) {
            return;
        }

        if (code == StateCode.DEVICE_UNBINED.code || code == StateCode.UUID_NOT_EXISTED.code) {
            ToastUtil.showToast(this, R.string.api_code_1109);
        } else if (code == StateCode.SHARE_ACCOUNT_BOND_BY_DEVICE.code) {
            ToastUtil.showLongToast(this, R.string.share_send_invitation_wait);
        } else if (code == StateCode.SHARE_DEVICE_COUNT_OVER.code) {
            ToastUtil.showToast(this, NooieApplication.get().getString(R.string.share_device_count_over));
        } else {
            ToastUtil.showToast(this, NooieApplication.get().getString(R.string.get_fail));
        }

        startRefresh();
    }

    @Override
    public void notifyDeleteSystemMsgResult(String result) {
        if (isDestroyed()) {
            return;
        }

        if (result.equals(ConstantValue.SUCCESS)) {
            ToastUtil.showLongToast(this, R.string.message_clear_success);
            startRefresh();
        } else {
            //ToastUtil.showLongToast(this, result);
        }
    }
}
