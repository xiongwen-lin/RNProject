package com.afar.osaio.smart.electrician.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.adapter.SelectedDeviceAdapter;
import com.afar.osaio.smart.electrician.presenter.ISelectShareDevicePresenter;
import com.afar.osaio.smart.electrician.presenter.SelectShareDevicePresenter;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.util.SpacesItemDecoration;
import com.afar.osaio.smart.electrician.view.ISelectShareDeviceView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * SelectShareDeviceActivity
 * 设备分享给他人
 *
 * @author Administrator
 * @date 2019/3/21
 */
public class SelectShareDeviceActivity extends BaseActivity implements ISelectShareDeviceView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvShareTip)
    TextView tvShareTip;
    @BindView(R.id.rcvCreateGroupDevice)
    RecyclerView rcvCreateGroupDevice;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private ISelectShareDevicePresenter mSelectShareDevicePresenter;
    private SelectedDeviceAdapter mDeviceAdapter;

    private long memberId;
    private boolean isAddGuest;
    private long homeId;
    private String countryCode;
    private String uid;

    public static void toSelectShareDeviceActivity(Activity from, int requestCode, long homeId, String countryCode, String devicesIds, String uid, boolean isAddGuest) {
        Intent intent = new Intent(from, SelectShareDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_HOME_ID, homeId);
        intent.putExtra(ConstantValue.INTENT_KEY_COUNTRY_CODE, countryCode);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICESIDS, devicesIds);
        intent.putExtra(ConstantValue.INTENT_KEY_UID, uid);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_ADD_GUEST, isAddGuest);
        from.startActivityForResult(intent, requestCode);
    }

    public static void toSelectShareDeviceActivity(Activity from, int requestCode, long homeId, String nickname, String devicesIds, long memberId) {
        Intent intent = new Intent(from, SelectShareDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_HOME_ID, homeId);
        intent.putExtra(ConstantValue.INTENT_KEY_NICK_NAME, nickname);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICESIDS, devicesIds);
        intent.putExtra(ConstantValue.INTENT_KEY_MEMBER_ID, memberId);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_share_device);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_access);
        ivRight.setImageResource(R.drawable.define_black);
        setupDeviceList();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            memberId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_MEMBER_ID, 0);
            homeId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_HOME_ID, 0);
            isAddGuest = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_IS_ADD_GUEST, false);
            countryCode = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_COUNTRY_CODE);
            uid = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_UID);
            mSelectShareDevicePresenter = new SelectShareDevicePresenter(this);
            String nickname = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_NICK_NAME);
            if (nickname == null && !isAddGuest) {
                tvTitle.setText(R.string.add_access);
                tvShareTip.setVisibility(View.GONE);
            } else {
                tvTitle.setText(R.string.add_guest);
                tvShareTip.setText(getResources().getString(R.string.select_share_device_tip));
            }
            showDeviceList(TuyaHomeSdk.getDataInstance().getHomeDeviceList(homeId));
        }
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.ivRight: {
                if (mDeviceAdapter != null) {
                    ArrayList<String> selectedDeviceIds = mDeviceAdapter.getSelectedDevice();
                    if (CollectionUtil.isNotEmpty(selectedDeviceIds)) {
                        if (isAddGuest) {
                            mSelectShareDevicePresenter.addShareWithHomeId(homeId, countryCode, uid, selectedDeviceIds);
                        } else {
                            mSelectShareDevicePresenter.addShareWithMemberId(memberId, selectedDeviceIds);
                        }
                    } else {
                        ToastUtil.showToast(this, R.string.select_least_one_device);
                    }
                }
                break;
            }
        }
    }

    private GridLayoutManager layoutManager;

    private void setupDeviceList() {
        mDeviceAdapter = new SelectedDeviceAdapter();
        mDeviceAdapter.setListener(new SelectedDeviceAdapter.DeviceItemListener() {
            @Override
            public void onItemClick(DeviceBean device) {
            }
        });

        layoutManager = new GridLayoutManager(this, 2);

        rcvCreateGroupDevice.setLayoutManager(layoutManager);
        rcvCreateGroupDevice.setAdapter(mDeviceAdapter);

        int leftRight = DisplayUtil.dpToPx(this, 16);
        int topBottom = DisplayUtil.dpToPx(this, 16);
        rcvCreateGroupDevice.addItemDecoration(new SpacesItemDecoration(leftRight, topBottom));

    }

    private void showDeviceList(final List<DeviceBean> devices) {
        if (devices != null) {
            String devicesIds = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICESIDS);
            if (devicesIds != null) {
                Iterator<DeviceBean> iterator = devices.iterator();
                while (iterator.hasNext()) {
                    if (devicesIds.contains(iterator.next().getDevId())) {
                        iterator.remove();
                    }
                }
            }

            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {

                    if (CollectionUtil.isEmpty(devices)) {
                        return 2;
                    } else {
                        return 1;
                    }
                }
            });

            mDeviceAdapter.setData(devices);
        }
    }


    @Override
    public void notifySharedDeviceState(String msg) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        } else {
            ErrorHandleUtil.toastTuyaError(this, msg);
        }
    }

    @Override
    public void addShareWithMemberIdSucccess(String msg) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void addShareWithMemberIdFail(String error) {
        ErrorHandleUtil.toastTuyaError(this, error);
    }
}
