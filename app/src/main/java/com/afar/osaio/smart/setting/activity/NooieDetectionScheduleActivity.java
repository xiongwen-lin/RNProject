package com.afar.osaio.smart.setting.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.bean.DetectionSchedule;
import com.afar.osaio.bean.ShortLinkDeviceParam;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.setting.adapter.DetectionScheduleAdapter;
import com.afar.osaio.smart.setting.presenter.INooieDetectionSchedulePresenter;
import com.afar.osaio.smart.setting.presenter.NooieDetectionSchedulePresenter;
import com.afar.osaio.smart.setting.view.INooieDetectionScheduleView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.widget.helper.ResHelper;
import com.nooie.common.utils.collection.CollectionUtil;
import com.afar.osaio.util.ToastUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NooieDetectionScheduleActivity extends BaseActivity implements INooieDetectionScheduleView {

    public static void toNooieDetectionScheduleActivity(Context from, String deviceId, int detectType) {
        Intent intent = new Intent(from, NooieDetectionScheduleActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.NOOIE_INTENT_KEY_DETECT_TYPE, detectType);
        from.startActivity(intent);
    }

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ivDeviceIcon)
    ImageView ivDeviceIcon;
    @BindView(R.id.tvDeviceName)
    TextView tvDeviceName;
    @BindView(R.id.btnDetectionScheduleOption)
    TextView btnDetectionScheduleOption;
    @BindView(R.id.rcvDetectionSchedule)
    RecyclerView rcvDetectionSchedule;
    @BindView(R.id.tvDetectionScheduleTip)
    TextView tvDetectionScheduleTip;

    private String mDeviceId;
    private DetectionScheduleAdapter mScheduleAdapter;
    private INooieDetectionSchedulePresenter mDetectionSchedulePresenter;
    private int mDetectType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection_schedule);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            return;
        } else {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mDetectType = getCurrentIntent().getIntExtra(ConstantValue.NOOIE_INTENT_KEY_DETECT_TYPE, ConstantValue.NOOIE_DETECT_TYPE_MOTION);
            mDetectionSchedulePresenter = new NooieDetectionSchedulePresenter(this);
        }
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.detection_schedule_label);
        ivRight.setVisibility(View.GONE);
        ivRight.setImageResource(R.drawable.add_photo);

        BindDevice device = NooieDeviceHelper.getDeviceById(mDeviceId);
        ivDeviceIcon.setImageResource(R.drawable.device_icon);
        if (device != null) {
            tvDeviceName.setText(device.getName());
            /*
            if (NooieDeviceHelper.mergeIpcType(IpcType.getIpcType(device.getType())) ==  IpcType.PC420 || NooieDeviceHelper.mergeIpcType(IpcType.getIpcType(device.getType())) == IpcType.MC120) {
                ivDeviceIcon.setImageResource(R.drawable.device_icon);
            } else if (NooieDeviceHelper.mergeIpcType(IpcType.getIpcType(device.getType())) == IpcType.PC530) {
                ivDeviceIcon.setImageResource(R.drawable.device_icon_360);
            } else if (NooieDeviceHelper.mergeIpcType(IpcType.getIpcType(device.getType())) == IpcType.PC730) {
                ivDeviceIcon.setImageResource(R.drawable.device_icon_outdoor);
            } else if (NooieDeviceHelper.mergeIpcType(IpcType.getIpcType(device.getType())) == IpcType.EC810_CAM) {
                ivDeviceIcon.setImageResource(R.drawable.device_icon_lp_810);
            } else {
                ivDeviceIcon.setImageResource(R.drawable.device_icon);
            }
            */
            ivDeviceIcon.setImageResource(ResHelper.getInstance().getDeviceIconByType(device.getType()));
        }
        //tvDetectionScheduleTip.setVisibility(mDetectType == ConstantValue.DETECT_TYPE_PIR ? View.GONE : View.VISIBLE);
        tvDetectionScheduleTip.setText(mDetectType == ConstantValue.DETECT_TYPE_PIR ? R.string.detection_schedule_pir_tips : (mDetectType == ConstantValue.NOOIE_DETECT_TYPE_MOTION ? R.string.detection_schedule_motion_tips : R.string.detection_schedule_sound_tips));
        btnDetectionScheduleOption.setVisibility(View.GONE);
        setupDetectionSchedule();
    }

    private void setupDetectionSchedule() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcvDetectionSchedule.setLayoutManager(layoutManager);
        mScheduleAdapter = new DetectionScheduleAdapter();
        mScheduleAdapter.setListener(new DetectionScheduleAdapter.DetectionScheduleListener() {
            @Override
            public void onItemClick(DetectionSchedule schedule) {
                gotoCreateSchedule(mDetectType, schedule);
            }

            @Override
            public void onItemSwitch(DetectionSchedule schedule) {
                switchSchedule(mDetectType, schedule);
            }
        });

        rcvDetectionSchedule.setAdapter(mScheduleAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
        registerShortLinkKeepListener();
    }

    public void resumeData() {
        showLoading();
        if (mDetectType == ConstantValue.NOOIE_DETECT_TYPE_MOTION) {
            mDetectionSchedulePresenter.getDetectionSchedules(mDetectType, mDeviceId, true);
        } else if (mDetectType == ConstantValue.NOOIE_DETECT_TYPE_SOUND) {
            mDetectionSchedulePresenter.getDetectionSchedules(mDetectType, mDeviceId, true);
        } else if (mDetectType == ConstantValue.DETECT_TYPE_PIR) {
            mDetectionSchedulePresenter.getDetectionSchedules(mDetectType, mDeviceId, true);
        } else {
            hideLoading();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unRegisterShortLinkKeepListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDetectionSchedulePresenter != null) {
            mDetectionSchedulePresenter.detachView();
            mDetectionSchedulePresenter = null;
        }
        hideScheduleAdjustmentDialog();
        release();
    }

    private void release() {
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        ivDeviceIcon = null;
        tvDeviceName = null;
        btnDetectionScheduleOption = null;
        if (rcvDetectionSchedule != null) {
            rcvDetectionSchedule.setAdapter(null);
        }
        tvDetectionScheduleTip = null;
        if (mScheduleAdapter != null) {
            mScheduleAdapter.setListener(null);
            mScheduleAdapter.clearData();
        }
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                setIsGotoOtherPage(true);
                finish();
                break;
            case R.id.ivRight:
                if (mScheduleAdapter != null && mScheduleAdapter.getItemCount() < 3) {
                    gotoCreateSchedule(mDetectType, null);
                }
                break;
        }
    }

    private void gotoCreateSchedule(int detectType, DetectionSchedule schedule) {
        if (detectType == ConstantValue.DETECT_TYPE_PIR) {
            setIsGotoOtherPage(true);
            int scheduleId = schedule != null ? schedule.getId() : 0;
            DeviceCreateDetectionScheduleActivity.toDeviceCreateDetectionScheduleActivity(NooieDetectionScheduleActivity.this, ConstantValue.REQUEST_CODE_SELECT_SCHEDULE, mDeviceId, schedule, scheduleId);
        } else {
            if (schedule != null) {
                NooieCreateDetectionScheduleActivity.toNooieCreateDetectionScheduleActivity(NooieDetectionScheduleActivity.this, ConstantValue.REQUEST_CODE_SELECT_SCHEDULE, mDeviceId, schedule, schedule.getId());
            }
        }
    }

    private void switchSchedule(int detectType, DetectionSchedule schedule) {
        if (detectType == ConstantValue.DETECT_TYPE_PIR) {
            if (mDetectionSchedulePresenter != null) {
                mDetectionSchedulePresenter.setDetectionSchedules(mDetectType, mDeviceId, schedule);
            }
        } else {
            if (schedule != null && schedule.isEffective()) {
                mDetectionSchedulePresenter.setDetectionSchedules(mDetectType, mDeviceId, schedule);
            } else if (schedule != null) {
                NooieCreateDetectionScheduleActivity.toNooieCreateDetectionScheduleActivity(NooieDetectionScheduleActivity.this, ConstantValue.REQUEST_CODE_SELECT_SCHEDULE, mDeviceId, schedule, schedule.getId());
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ConstantValue.REQUEST_CODE_SELECT_SCHEDULE:
                    if (intent != null) {
                        DetectionSchedule schedule = (DetectionSchedule)intent.getSerializableExtra(ConstantValue.INTENT_KEY_DATA_TYPE);
                        updateDetectionSchedule(mDetectType, mDeviceId, schedule);
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void updateDetectionSchedule(int detectType, String deviceId, DetectionSchedule schedule) {
        if (detectType == ConstantValue.DETECT_TYPE_PIR) {
            if (schedule != null) {
                NooieLog.d("-->> NooieDetectionScheduleActivity updateDetectionSchedule {r detectType=" + detectType + " deviceId=" + deviceId + " schedule " + schedule.convertString());
                mDetectionSchedulePresenter.setDetectionSchedules(mDetectType, deviceId, schedule);
            }
        } else {
            if (schedule != null) {
                mDetectionSchedulePresenter.setDetectionSchedules(mDetectType, deviceId, schedule);
            }
        }
    }

    private Dialog mDialog1 = null;
    @Override
    public void notifyGetDetectionSchedulesSuccess(int detectType, List<DetectionSchedule> schedules, boolean isHideLoading) {
        if (isDestroyed() || checkNull(rcvDetectionSchedule, mScheduleAdapter)) {
            return;
        }

        if (isHideLoading) {
            hideLoading();
        }
        boolean isShowAddSchedule = detectType == ConstantValue.DETECT_TYPE_PIR && (CollectionUtil.isEmpty(schedules) || schedules.size() < 3);
        ivRight.setVisibility(isShowAddSchedule ? View.VISIBLE : View.GONE);
        boolean isSchedulesInvalid = detectType == ConstantValue.DETECT_TYPE_PIR ? CollectionUtil.isEmpty(schedules) : (CollectionUtil.isEmpty(schedules) || schedules.size() < 3);
        if (isSchedulesInvalid) {
            return;
        }

        if (detectType == ConstantValue.NOOIE_DETECT_TYPE_MOTION) {
            mScheduleAdapter.setData(schedules);
        } else if (detectType == ConstantValue.NOOIE_DETECT_TYPE_SOUND) {
            mScheduleAdapter.setData(schedules);
        } else if (detectType == ConstantValue.DETECT_TYPE_PIR) {
            mScheduleAdapter.setData(schedules);
        }

        boolean isScheduleEffective = schedules.get(0) != null ? schedules.get(0).isEffective() : false;
        if (!isScheduleEffective) {
            showScheduleAdjustmentDialog();
        }
    }

    private void showScheduleAdjustmentDialog() {
        hideScheduleAdjustmentDialog();
        mDialog1 = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.detection_schedule_adjust_title, R.string.detection_schedule_adjust_content, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void hideScheduleAdjustmentDialog() {
        if (mDialog1 != null) {
            mDialog1.dismiss();
            mDialog1 = null;
        }
    }

    @Override
    public void notifyGetDetectionSchedulesFailed(int detectType, boolean isHideLoading) {
        if (isDestroyed()) {
            return;
        }
        if (isHideLoading) {
            hideLoading();
        }
        if (detectType == ConstantValue.NOOIE_DETECT_TYPE_MOTION) {
        } else if (detectType == ConstantValue.NOOIE_DETECT_TYPE_SOUND) {
        }
    }

    @Override
    public void notifySetDetectionSchedulesResult(String result) {
        if (isDestroyed()) {
            return;
        }

        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            ToastUtil.showToast(this, R.string.success);
        } else {
            ToastUtil.showToast(this, R.string.get_fail);
        }
    }

    @Override
    public void displayLoading(boolean show) {
        if (isDestroyed()) {
            return;
        }

        if (show) {
            //showLoading();
            showLoading(false);
        } else {
            hideLoading();
        }
    }

    @Override
    public ShortLinkDeviceParam getShortLinkDeviceParam() {
        BindDevice device = NooieDeviceHelper.getDeviceById(mDeviceId);
        if (device == null) {
            return null;
        }
        String model = device.getType();
        boolean isSubDevice = NooieDeviceHelper.isSubDevice(device.getPuuid(), device.getType());
        ShortLinkDeviceParam shortLinkDeviceParam = new ShortLinkDeviceParam(mUid, mDeviceId, model, isSubDevice, false, ConstantValue.CONNECTION_MODE_QC);
        return shortLinkDeviceParam;
    }
}
