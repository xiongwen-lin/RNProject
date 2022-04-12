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
import com.afar.osaio.smart.electrician.adapter.ManageLampDeviceAdapter;
import com.afar.osaio.smart.electrician.bean.DeviceHelper;
import com.afar.osaio.smart.electrician.eventbus.GroupManageEvent;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.manager.PowerStripHelper;
import com.afar.osaio.smart.electrician.presenter.IManageDevicePresenter;
import com.afar.osaio.smart.electrician.presenter.ManageDevicePresenter;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.util.SpacesItemDecoration;
import com.afar.osaio.smart.electrician.view.IManageDeviceView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.tuya.smart.api.MicroContext;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.panelcaller.api.AbsPanelCallerService;
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
 * 灯的群组设备管理
 */
public class ManageLampDeviceActivity extends BaseActivity implements IManageDeviceView {

    private final String TAG_COMMON = "1";
    private final String TAG_ALL_SELECT = "2";

    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvRight)
    TextView tvRight;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.rcvSelectDevice)
    RecyclerView rcvSelectDevice;
    @BindView(R.id.tvNoDevice)
    TextView tvNoDevice;
    @BindView(R.id.tvCancel)
    TextView tvCancel;
    @BindView(R.id.tvConfirm)
    TextView tvConfirm;

    private IManageDevicePresenter mManagerPresenter;
    private long mGroupId;
    private String mProductId;
    private ManageLampDeviceAdapter mDeviceSelectAdapter;

    public static void toManageLampDeviceActivity(Activity from, long groupId, String productId) {
        Intent intent = new Intent(from, ManageLampDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_ID, groupId);
        intent.putExtra(ConstantValue.INTENT_KEY_PRODUCT_ID, productId);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_lamp_device);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvRight.setText(getResources().getString(R.string.choose));
        ivRight.setVisibility(View.GONE);
        tvRight.setVisibility(View.VISIBLE);
        tvRight.setTextColor(getResources().getColor(R.color.theme_text_color));
        tvTitle.setText(getResources().getString(R.string.manage_devices));
        tvRight.setTag(TAG_COMMON);
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
            loadGroupInfo();
        }
    }

    private void loadGroupInfo() {
        TuyaHomeSdk.newHomeInstance(FamilyManager.getInstance().getCurrentHomeId()).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                mManagerPresenter.loadSelectDevices(mGroupId, mProductId);
            }

            @Override
            public void onError(String code, String msg) {
                mManagerPresenter.loadSelectDevices(mGroupId, mProductId);
            }
        });
    }


    private void changeTvRightState() {
        String tag = (String) tvRight.getTag();
        if (tag.equals(TAG_COMMON)) {//原展示状态
            tvRight.setText(getResources().getString(R.string.select_all));
            tvRight.setTag(TAG_ALL_SELECT);
            tvCancel.setVisibility(View.VISIBLE);
            tvConfirm.setVisibility(View.VISIBLE);
            mDeviceSelectAdapter.setTagType(1);
        } else if (tag.equals(TAG_ALL_SELECT)) {
            tvRight.setText(getResources().getString(R.string.choose));
            tvRight.setTag(TAG_COMMON);
            tvCancel.setVisibility(View.GONE);
            tvConfirm.setVisibility(View.GONE);
            mDeviceSelectAdapter.setTagType(0);
        }

    }

    @OnClick({R.id.ivLeft, R.id.tvRight, R.id.tvCancel, R.id.tvConfirm})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.tvRight:
                String tag = (String) tvRight.getTag();
                if (tag.equals(TAG_COMMON)) {
                    changeTvRightState();
                } else {
                    mDeviceSelectAdapter.setSelectedAll();
                }

                break;
            case R.id.tvCancel: {
                changeTvRightState();
                break;
            }
            case R.id.tvConfirm: {
                if (mDeviceSelectAdapter != null) {
                    ArrayList<String> selectedDeviceIds = mDeviceSelectAdapter.getSelectedDevice();
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

    private void setupDeviceList() {
        mDeviceSelectAdapter = new ManageLampDeviceAdapter();
        mDeviceSelectAdapter.setListener(new ManageLampDeviceAdapter.DeviceItemListener() {
            @Override
            public void onItemClick(DeviceBean device) {
                if (PowerStripHelper.getInstance().isUserTuyaPanel(device) || PowerStripHelper.getInstance().isLamp(device)) {
                    AbsPanelCallerService service = MicroContext.getServiceManager().findServiceByInterface(AbsPanelCallerService.class.getName());
                    service.goPanelWithCheckAndTip(ManageLampDeviceActivity.this, device.getDevId());
                } else {
                    WrongDeviceActivity.toWrongDeviceActivity(ManageLampDeviceActivity.this, device.getDevId());
                }
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rcvSelectDevice.setLayoutManager(layoutManager);
        rcvSelectDevice.setAdapter(mDeviceSelectAdapter);

        int leftRight = DisplayUtil.dpToPx(this, 24);
        int topBottom = DisplayUtil.dpToPx(this, 24);
        rcvSelectDevice.addItemDecoration(new SpacesItemDecoration(leftRight, topBottom));
    }


    @Override
    public void notifyLoadGroupSuccess(GroupBean groupBean) {
    }

    @Override
    public void notifyLoadDevicesSuccess(List<GroupDeviceBean> devices) {
        if (devices != null && devices.size() > 0) {
            GroupBean group = TuyaHomeSdk.getDataInstance().getGroupBean(mGroupId);
            if (group.getDeviceBeans() != null && group.getDeviceBeans().size() > 0) {
                for (DeviceBean device : group.getDeviceBeans()) {
                    mDeviceSelectAdapter.addGroupDeviceIds(device.getDevId());
                }
            }
            mDeviceSelectAdapter.setData(DeviceHelper.covertDeviceBean(devices));
            tvNoDevice.setVisibility(View.GONE);
        } else {
            tvNoDevice.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void notifyDevicesFailed(String msg) {
        ErrorHandleUtil.toastTuyaError(this,getString(R.string.get_fail));
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
}
