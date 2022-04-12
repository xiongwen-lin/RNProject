package com.afar.osaio.smart.setting.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.bean.FlashLightMode;
import com.afar.osaio.smart.setting.adapter.FlashLightModeAdapter;
import com.afar.osaio.smart.setting.adapter.listener.FlashLightModeListener;
import com.afar.osaio.smart.setting.contract.FlashLightContract;
import com.afar.osaio.smart.setting.presenter.FlashLightPresenter;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.data.EventDictionary;
import com.nooie.eventtracking.EventTrackingApi;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.bean.PirStateV2;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class FlashLightActivity extends BaseActivity implements FlashLightContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.rvFlashLightMode)
    RecyclerView rvFlashLightMode;
    @BindView(R.id.ivFlashLightModeIcon)
    ImageView ivFlashLightModeIcon;
    @BindView(R.id.givFlashLightModeIcon)
    GifImageView givFlashLightModeIcon;

    private FlashLightContract.Presenter mPresenter;
    private FlashLightModeAdapter mAdapter;
    private GifDrawable mFlashLightIconGif = null;

    public static void toFlashLightActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, FlashLightActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_light);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new FlashLightPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.cam_setting_flash_light);
        setupFlashModeView();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
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
    public void setPresenter(@NonNull FlashLightContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onGetPirState(int state, PirStateV2 pirState) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (state == SDKConstant.SUCCESS) {
            if (pirState != null) {
                refreshFlashLightMode(pirState.lightMode);
            }
        }
    }

    @Override
    public void onSetFlashLightMode(int state) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
    }

    private void setupFlashModeView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvFlashLightMode.setLayoutManager(layoutManager);

        mAdapter = new FlashLightModeAdapter();
        mAdapter.setListener(new FlashLightModeListener() {
            @Override
            public void onItemClick(FlashLightMode mode) {
                changeFlashLightMode(mode);
            }
        });

        rvFlashLightMode.setAdapter(mAdapter);
        mAdapter.setData(getFlashLightModeList());

        refreshFlashLightMode(ConstantValue.FLASH_LIGHT_MODE_CLOSE);
        if (mPresenter != null) {
            showLoading();
            mPresenter.getPirState(getDeviceId());
        }
    }

    private List<FlashLightMode> getFlashLightModeList() {
        List<FlashLightMode> flashLightModeList = new ArrayList<>();
        FlashLightMode flashLightMode1 = new FlashLightMode();
        flashLightMode1.setMode(ConstantValue.FLASH_LIGHT_MODE_FULL_COLOR_NIGHT_VISION);
        flashLightModeList.add(flashLightMode1);
        FlashLightMode flashLightMode2 = new FlashLightMode();
        flashLightMode2.setMode(ConstantValue.FLASH_LIGHT_MODE_FLASH_WARNING);
        flashLightModeList.add(flashLightMode2);
        FlashLightMode flashLightMode3 = new FlashLightMode();
        flashLightMode3.setMode(ConstantValue.FLASH_LIGHT_MODE_CLOSE);
        flashLightModeList.add(flashLightMode3);
        return flashLightModeList;
    }

    private void changeFlashLightMode(FlashLightMode flashLightMode) {
        if (isDestroyed() ||  checkNull(flashLightMode)) {
            return;
        }
        refreshFlashLightIcon(flashLightMode.getMode());
        if (mPresenter != null) {
            showLoading();
            mPresenter.setFlashLightMode(getDeviceId(), flashLightMode.getMode());
            sendFlashLightModeEvent(flashLightMode.getMode());
        }
    }

    private void refreshFlashLightMode(int mode) {
        if (isDestroyed() || checkNull(mAdapter)) {
            return;
        }
        mAdapter.updateSelectMode(mode);
        refreshFlashLightIcon(mode);
    }

    private void refreshFlashLightIcon(int mode) {
        if (isDestroyed()) {
            return;
        }
        clearFlashLightModeIcon();
        releaseFlashLightIcon();
        if (mode == ConstantValue.FLASH_LIGHT_MODE_CLOSE) {
            displayFlashLightModeIconWithImage(R.drawable.flash_light_close_mode);
            return;
        } else if (mode == ConstantValue.FLASH_LIGHT_MODE_FULL_COLOR_NIGHT_VISION) {
            displayFlashLightModeIconWithImage(R.drawable.flash_light_full_color_mode);
            return;
        }
        try {
            mFlashLightIconGif = new GifDrawable(getResources(), getFlashLightIconGifResId(mode));
            mFlashLightIconGif.setSpeed(1);
            mFlashLightIconGif.addAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationCompleted(int loopNumber) {
                }
            });
            displayFlashLightModeIconWithGif(mFlashLightIconGif);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    private void releaseFlashLightIcon() {
        if (mFlashLightIconGif != null) {
            mFlashLightIconGif = null;
        }
    }

    private int getFlashLightIconGifResId(int mode) {
        int gifResId = R.raw.flash_light_waring_mode;
        switch (mode) {
            case ConstantValue.FLASH_LIGHT_MODE_FULL_COLOR_NIGHT_VISION :
                gifResId = R.raw.flash_light_waring_mode;
                break;
            case ConstantValue.FLASH_LIGHT_MODE_FLASH_WARNING :
                gifResId = R.raw.flash_light_waring_mode;
                break;
        }
        return gifResId;
    }

    private Bundle getParam() {
        if (getCurrentIntent() == null) {
            return null;
        }
        return getCurrentIntent().getBundleExtra(ConstantValue.INTENT_KEY_DATA_PARAM);
    }

    private String getDeviceId() {
        if (getParam() == null) {
            return null;
        }
        return getParam().getString(ConstantValue.INTENT_KEY_DEVICE_ID);
    }

    private String getDeviceModel() {
        if (getParam() == null) {
            return null;
        }
        return getParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL);
    }

    private void sendFlashLightModeEvent(int mode) {
        try {
            ArrayMap<String, Object> externalMap = new ArrayMap<>();
            if (!TextUtils.isEmpty(getDeviceModel())) {
                externalMap.put("deviceModel", getDeviceId());
            }
            externalMap.put("mode", convertFlashLightModeForEvent(mode));
            EventTrackingApi.getInstance().trackNormalEvent(EventDictionary.EVENT_ID_157, "", 0, GsonHelper.convertToJson(externalMap), getDeviceId());
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    private String convertFlashLightModeForEvent(int mode) {
        String modeStr = "off";
        switch(mode) {
            case ConstantValue.FLASH_LIGHT_MODE_FULL_COLOR_NIGHT_VISION :
                modeStr = "fullColor";
                break;
            case ConstantValue.FLASH_LIGHT_MODE_FLASH_WARNING :
                modeStr = "alarm";
                break;
        }
        return modeStr;
    }

    private void displayFlashLightModeIconWithImage(int resId) {
        givFlashLightModeIcon.setVisibility(View.GONE);
        ivFlashLightModeIcon.setVisibility(View.VISIBLE);
        ivFlashLightModeIcon.setImageResource(resId);
    }

    private void displayFlashLightModeIconWithGif(GifDrawable res) {
        ivFlashLightModeIcon.setVisibility(View.GONE);
        givFlashLightModeIcon.setVisibility(View.VISIBLE);
        givFlashLightModeIcon.setImageDrawable(res);
    }

    private void clearFlashLightModeIcon() {
        ivFlashLightModeIcon.setImageDrawable(null);
        givFlashLightModeIcon.setImageDrawable(null);
    }
}
