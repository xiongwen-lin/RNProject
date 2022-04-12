package com.afar.osaio.smart.electrician.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.adapter.SelectedDeviceAdapter;
import com.afar.osaio.smart.electrician.bean.DeviceHelper;
import com.afar.osaio.smart.electrician.eventbus.GroupManageEvent;
import com.afar.osaio.smart.electrician.presenter.IManageDevicePresenter;
import com.afar.osaio.smart.electrician.presenter.ManageDevicePresenter;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.util.SpacesItemDecoration;
import com.afar.osaio.smart.electrician.view.IManageDeviceView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.sdk.bean.GroupDeviceBean;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * ManageDeviceActivity
 *
 * @author Administrator
 * @date 2019/3/21
 */
public class ManageDeviceActivity extends BaseActivity implements IManageDeviceView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.rcvCreateGroupDevice)
    RecyclerView rcvCreateGroupDevice;
    @BindView(R.id.tvShareTip)
    TextView tvShareTip;

    private IManageDevicePresenter mManagerPresenter;
    private long mGroupId;
    private String mProductId;
    private SelectedDeviceAdapter mDeviceAdapter;
    private List<GroupDeviceBean> mGroups;

    public static void toManageDeviceActivity(Activity from, long groupId, String productId) {
        Intent intent = new Intent(from, ManageDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_ID, groupId);
        intent.putExtra(ConstantValue.INTENT_KEY_PRODUCT_ID, productId);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPreMVersionStatusBar();
        setContentView(R.layout.activity_select_share_device);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.manage_devices);
        ivRight.setImageResource(R.drawable.define_black);
        tvShareTip.setVisibility(View.GONE);
        setupDeviceList();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mGroupId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_GROUP_ID, 0);
            mProductId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_PRODUCT_ID);
            mManagerPresenter = new ManageDevicePresenter(this);
            mManagerPresenter.loadSelectDevices(mGroupId, mProductId);
        }
    }

    private void setupDeviceList() {
        mDeviceAdapter = new SelectedDeviceAdapter();
        mDeviceAdapter.setListener(new SelectedDeviceAdapter.DeviceItemListener() {
            @Override
            public void onItemClick(DeviceBean device) {
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (CollectionUtil.isEmpty(DeviceHelper.covertDeviceBean(mGroups))) {
                    return 2;
                }
                return 1;
            }
        });
        rcvCreateGroupDevice.setLayoutManager(layoutManager);
        rcvCreateGroupDevice.setAdapter(mDeviceAdapter);
        ((SimpleItemAnimator) rcvCreateGroupDevice.getItemAnimator()).setSupportsChangeAnimations(false);

        int leftRight = DisplayUtil.dpToPx(this, 16);
        int topBottom = DisplayUtil.dpToPx(this, 16);
        rcvCreateGroupDevice.addItemDecoration(new SpacesItemDecoration(leftRight, topBottom));
    }

    @Override
    public void notifyLoadGroupSuccess(GroupBean groupBean) {

    }

    @Override
    public void notifyLoadDevicesSuccess(List<GroupDeviceBean> devices) {
        if (devices != null && devices.size() > 0) {
            mGroups = devices;
            GroupBean group = TuyaHomeSdk.getDataInstance().getGroupBean(mGroupId);
            if (group.getDeviceBeans() != null && group.getDeviceBeans().size() > 0) {
                for (DeviceBean device : group.getDeviceBeans()) {
                    mDeviceAdapter.addGroupDeviceIds(device.getDevId());
                }
            }
            mDeviceAdapter.setData(DeviceHelper.covertDeviceBean(devices));
        }
    }

    @Override
    public void notifyDevicesFailed(String msg) {
        ErrorHandleUtil.toastTuyaError(this,"fail");
    }

    @Override
    public void notifyUpdateDeviceState(String msg) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            EventBus.getDefault().post(new GroupManageEvent(true, mGroupId));
            finish();
        } else {
            ErrorHandleUtil.toastTuyaError(this, msg);
        }
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.ivRight:
                if (mDeviceAdapter != null) {
                    ArrayList<String> selectedDeviceIds = mDeviceAdapter.getSelectedDevice();
                    if (CollectionUtil.isNotEmpty(selectedDeviceIds)) {
                        mManagerPresenter.updateDeviceList(mGroupId, selectedDeviceIds);
                    } else {
                        ToastUtil.showToast(this, R.string.select_least_one_device);
                    }
                }
                break;
        }
    }
}
