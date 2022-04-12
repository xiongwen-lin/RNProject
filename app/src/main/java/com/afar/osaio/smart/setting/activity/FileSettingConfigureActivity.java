package com.afar.osaio.smart.setting.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.bean.CurrentDeviceParam;
import com.afar.osaio.bean.FileSettingConfigureParam;
import com.afar.osaio.smart.setting.adapter.FileSettingConfigureAdapter;
import com.afar.osaio.smart.setting.adapter.listener.FileSettingConfigureListener;
import com.afar.osaio.smart.setting.contract.FileSettingConfigureContract;
import com.afar.osaio.smart.setting.presenter.FileSettingConfigurePresenter;
import com.afar.osaio.util.ConstantValue;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.bean.NooieMediaMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FileSettingConfigureActivity extends BaseActivity implements FileSettingConfigureContract.View {

    private static final int MAX_SNAP_NUMBER = 10;
    private static final int SINGLE_RECORDING_DURATION = 5;
    private static final int MAX_RECORDING_DURATION_COUNT = 4;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvFileSettingConfigureTitle)
    TextView tvFileSettingConfigureTitle;
    @BindView(R.id.rvFileSettingConfigureList)
    RecyclerView rvFileSettingConfigureList;

    private FileSettingConfigureContract.Presenter mPresenter;
    private FileSettingConfigureAdapter mAdapter;

    public static void toFileSettingConfigureActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, FileSettingConfigureActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_setting_configure);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new FileSettingConfigurePresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        setupFileSettingConfigureView();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
        if (mPresenter != null) {
            showLoading();
            mPresenter.getFileSettingMode(getDeviceId());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
    }

    @OnClick({R.id.ivLeft})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull FileSettingConfigureContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onGetFileSettingMode(int state, NooieMediaMode mode) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (state == SDKConstant.SUCCESS) {
            refreshFileSettingView(getFileSettingConfigureMode(), mode);
        }
    }

    @Override
    public void onSetFileSettingMode(int state, NooieMediaMode mode) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
    }

    @Override
    public CurrentDeviceParam getCurrentDeviceParam() {
        if (TextUtils.isEmpty(getDeviceId())) {
            return null;
        }
        CurrentDeviceParam currentDeviceParam = null;
        if (getConnectionMode() == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            currentDeviceParam = new CurrentDeviceParam();
            currentDeviceParam.setDeviceId(getDeviceId());
            currentDeviceParam.setConnectionMode(getConnectionMode());
            currentDeviceParam.setModel(getDeviceModel());
        } else {
        }
        return currentDeviceParam;
    }

    private void setupFileSettingConfigureView() {
        tvTitle.setText(getConfigureTitle(getFileSettingConfigureMode()));
        tvFileSettingConfigureTitle.setText(getConfigureTip(getFileSettingConfigureMode()));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvFileSettingConfigureList.setLayoutManager(layoutManager);
        mAdapter = new FileSettingConfigureAdapter();
        mAdapter.setListener(new FileSettingConfigureListener() {
            @Override
            public void onItemClick(FileSettingConfigureParam configure) {
                setFileSettingConfigure(configure);
            }
        });
        mAdapter.setData(createFileConfigureList(getFileSettingConfigureMode()));
        rvFileSettingConfigureList.setAdapter(mAdapter);
    }

    private String getConfigureTitle(int mode) {
        if (mode == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_MODE) {
            return getString(R.string.file_setting_mode);
        } else if (mode == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_SNAP_NUMBER) {
            return getString(R.string.file_setting_snap_number);
        } else if (mode == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_RECORDING_TIME) {
            return getString(R.string.file_setting_record_time);
        } else {
            return new String();
        }
    }

    private String getConfigureTip(int mode) {
        if (mode == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_MODE) {
            return getString(R.string.file_setting_configure_mode_tip);
        } else if (mode == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_SNAP_NUMBER) {
            return getString(R.string.file_setting_configure_snap_number_tip);
        } else if (mode == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_RECORDING_TIME) {
            return getString(R.string.file_setting_configure_record_time_tip);
        } else {
            return new String();
        }
    }

    private List<FileSettingConfigureParam> createFileConfigureList(int configureMode) {
        List<FileSettingConfigureParam> paramList = new ArrayList<>();
        if (configureMode == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_MODE) {
            FileSettingConfigureParam videoParam = new FileSettingConfigureParam();
            videoParam.setType(ConstantValue.TYPE_FILE_SETTING_CONFIGURE_MODE);
            videoParam.setMode(ConstantValue.DEVICE_MEDIA_MODE_VIDEO);
            videoParam.setSelected(false);
            paramList.add(videoParam);

            FileSettingConfigureParam imageParam = new FileSettingConfigureParam();
            imageParam.setType(ConstantValue.TYPE_FILE_SETTING_CONFIGURE_MODE);
            imageParam.setMode(ConstantValue.DEVICE_MEDIA_MODE_IMAGE);
            imageParam.setSelected(false);
            paramList.add(imageParam);

            FileSettingConfigureParam videoImageParam = new FileSettingConfigureParam();
            videoImageParam.setType(ConstantValue.TYPE_FILE_SETTING_CONFIGURE_MODE);
            videoImageParam.setMode(ConstantValue.DEVICE_MEDIA_MODE_VIDEO_IMAGE);
            videoImageParam.setSelected(false);
            paramList.add(videoImageParam);
        } else if (configureMode == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_SNAP_NUMBER) {
            for (int i = 1; i <= MAX_SNAP_NUMBER; i++) {
                FileSettingConfigureParam param = new FileSettingConfigureParam();
                param.setType(ConstantValue.TYPE_FILE_SETTING_CONFIGURE_SNAP_NUMBER);
                param.setSnapNumber(i);
                param.setSelected(false);
                paramList.add(param);
            }
        } else if (configureMode == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_RECORDING_TIME) {
            for (int i = 1; i <= MAX_RECORDING_DURATION_COUNT; i++) {
                FileSettingConfigureParam param = new FileSettingConfigureParam();
                param.setType(ConstantValue.TYPE_FILE_SETTING_CONFIGURE_RECORDING_TIME);
                param.setRecordingTime(computeRecordingTime(i));
                param.setSelected(false);
                paramList.add(param);
            }
        }

        return paramList;
    }

    private int computeRecordingTime(int n) {
        if (n <= 1) {
            return SINGLE_RECORDING_DURATION;
        }
        return 2 * (n - 1) * SINGLE_RECORDING_DURATION;
    }

    private void refreshFileSettingView(int configureMode, NooieMediaMode mediaMode) {
        if (checkNull(mAdapter)) {
            return;
        }
        mAdapter.setMediaMode(configureMode, mediaMode);
    }

    private void setFileSettingConfigure(FileSettingConfigureParam configure) {
        if (isDestroyed()) {
            return;
        }
        if (configure != null && mPresenter != null) {
            showLoading();
            mPresenter.setFileSettingConfigure(getDeviceId(), configure);
        }
    }

    private String getDeviceId() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_DEVICE_ID);
    }

    private String getDeviceModel() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL);
    }

    private int getConnectionMode() {
        if (getStartParam() == null) {
            return ConstantValue.CONNECTION_MODE_QC;
        }
        return getStartParam().getInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
    }

    private int getFileSettingConfigureMode() {
        if (getStartParam() == null) {
            return ConstantValue.TYPE_FILE_SETTING_CONFIGURE_NONE;
        }
        return getStartParam().getInt(ConstantValue.INTENT_KEY_DATA_PARAM_1, ConstantValue.TYPE_FILE_SETTING_CONFIGURE_NONE);
    }

    private int getMediaMode() {
        if (getStartParam() == null) {
            return ConstantValue.DEVICE_MEDIA_MODE_IMAGE;
        }
        return getStartParam().getInt(ConstantValue.INTENT_KEY_DATA_PARAM_2);
    }

    private int getImageNum() {
        if (getStartParam() == null) {
            return 0;
        }
        return getStartParam().getInt(ConstantValue.INTENT_KEY_DATA_PARAM_3);
    }

    private int getRecordingDuration() {
        if (getStartParam() == null) {
            return 0;
        }
        return getStartParam().getInt(ConstantValue.INTENT_KEY_DATA_PARAM_4);
    }
}
