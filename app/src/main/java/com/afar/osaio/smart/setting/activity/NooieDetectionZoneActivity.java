package com.afar.osaio.smart.setting.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.player.activity.BasePlayerActivity;
import com.afar.osaio.smart.setting.contract.DetectionZoneContract;
import com.afar.osaio.smart.setting.presenter.DetectionZonePresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.tool.TaskUtil;
import com.nooie.sdk.device.bean.AreaRect;
import com.nooie.sdk.device.bean.MTAreaInfo;
import com.steelkiwi.cropiwa.OnBoundsChangeListener;
import com.steelkiwi.cropiwa.SelectAreaIwaView;
import com.suke.widget.SwitchButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class NooieDetectionZoneActivity extends BaseActivity implements DetectionZoneContract.View {

    private static final int UPDATE_DETECTION_ZONE_STATE = 1;
    private static final int UPDATE_DETECTION_ZONE_AREA = 2;

    public static void toNooieDetectionZoneActivity(Activity from, int requestCode, String deviceId) {
        Intent intent = new Intent(from, NooieDetectionZoneActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        from.startActivityForResult(intent, requestCode);
    }

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvRight)
    TextView tvRight;
    @BindView(R.id.tvDetectionZoneTip)
    TextView tvDetectionZoneTip;
    @BindView(R.id.sbDetectionZoneSwitch)
    SwitchButton sbDetectionZoneSwitch;
    @BindView(R.id.iwaSelectArea)
    SelectAreaIwaView ciwaSelectArea;
    @BindView(R.id.ivDetectionTarget)
    ImageView ivDetectionTarget;

    private DetectionZoneContract.Presenter mPresenter;
    private String mDeviceId;
    private RectF mSelectZoneRectF = null;
    private int mTargetViewWidth = 0;
    private int mTargetViewHeight = 0;
    private MTAreaInfo mLastMtAreaInfo = null;
    private boolean mIsResetCropArea = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection_zone);
        ButterKnife.bind(this);

        initData();
        initView();
        if (mPresenter != null) {
            showLoading();
            setIsResettingCropArea(true);
            mPresenter.getMtAreaInfo(mDeviceId);
        }
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            return;
        } else {
            new DetectionZonePresenter(this);
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
        }
    }

    private void initView() {
        tvTitle.setText(R.string.detection_zone_label);
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setImageResource(R.drawable.confirm_black);
        tvRight.setText(R.string.edit);
        sbDetectionZoneSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {
                    displayZoneEditView(ConstantValue.EDIT_MODE_EDITABLE, false);
                } else {
                    displayZoneEditView(ConstantValue.EDIT_MODE_NORMAL, false);
                }
                updateMtAreaInfo(mDeviceId, isChecked);
            }
        });

        setupSelectAreaView();
        displayZoneEditView(ConstantValue.EDIT_MODE_NORMAL, false);
    }

    private void setupSelectAreaView() {
        String imgPath = BasePlayerActivity.getDevicePreviewFile(mDeviceId);
        Glide.with(NooieApplication.mCtx)
                .load(imgPath)
                .apply(new RequestOptions()
                                .dontTransform().transform(new CenterCrop())//.transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 10))))
                                //.placeholder(placeHolderDrawable)
                                .placeholder(R.drawable.default_preview)
                                .format(DecodeFormat.PREFER_RGB_565).diskCacheStrategy(DiskCacheStrategy.NONE)
                        //.error(R.drawable.default_preview)
                )
                .transition(withCrossFade())
                .into(ivDetectionTarget);

        ciwaSelectArea.setDrawOverlay(true);
        ciwaSelectArea.setIsCropDragEnable(false);
        //ciwaSelectArea.configureOverlay().setDynamicCrop(true).setShouldDrawGrid(false).setCornerColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.orange_ff8800)).setBorderColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.transparent)).apply();
        ciwaSelectArea.configureOverlay().setDynamicCrop(true).setShouldDrawGrid(false).setCornerColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.transparent)).setBorderColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.transparent)).apply();
        ciwaSelectArea.setOnBoundshangeListener(new OnBoundsChangeListener() {
            @Override
            public void onBoundsChange(RectF bounds, int vWidth, int vHeight) {
                if (vWidth != 0 && vHeight != 0) {
                    mTargetViewWidth = vWidth;
                    mTargetViewHeight = vHeight;
                }
                if (bounds != null) {
                    NooieLog.d("-->> NooieDetectionZoneActivity onBoundsChange bounds left=" + bounds.left + " top=" + bounds.top + " right=" + bounds.right + " bottom=" + bounds.bottom + " w=" + vWidth + " h=" + vHeight);
                }
                RectF zoneRectF = NooieDeviceHelper.convertZoneRectF(bounds, vWidth, vHeight);
                if (zoneRectF != null) {
                    NooieLog.d("-->> NooieDetectionZoneActivity onBoundsChange zoneRectF left=" + zoneRectF.left + " top=" + zoneRectF.top + " right=" + zoneRectF.right + " bottom=" + zoneRectF.bottom);
                }
                updateSelectZoneParam(zoneRectF);
            }
        });
    }

    private void displayZoneEditView(int editMode, boolean isShowSelectArae) {
        if (checkNull(tvRight, ivRight, ciwaSelectArea)) {
            return;
        }
        tvRight.setTag(editMode);
        if (editMode == ConstantValue.EDIT_MODE_EDITING) {
            tvRight.setVisibility(View.GONE);
            ivRight.setVisibility(View.VISIBLE);
            ciwaSelectArea.setVisibility(View.VISIBLE);
            ciwaSelectArea.configureOverlay().setCornerColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.orange_ff8800)).apply();
            ciwaSelectArea.setIsCropDragEnable(true);
            ivDetectionTarget.setVisibility(View.VISIBLE);
            sbDetectionZoneSwitch.setEnabled(false);
        } else if (editMode == ConstantValue.EDIT_MODE_EDITABLE) {
            ivRight.setVisibility(View.GONE);
            ciwaSelectArea.setVisibility(isShowSelectArae ? View.VISIBLE : View.GONE);
            ciwaSelectArea.configureOverlay().setCornerColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.transparent)).apply();
            ciwaSelectArea.setIsCropDragEnable(false);
            ivDetectionTarget.setVisibility(View.VISIBLE);
            tvRight.setVisibility(View.VISIBLE);
            sbDetectionZoneSwitch.setEnabled(true);
        } else  {
            ivRight.setVisibility(View.GONE);
            ciwaSelectArea.setVisibility(View.GONE);
            //ciwaSelectArea.configureOverlay().setCornerColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.transparent)).apply();
            ciwaSelectArea.setIsCropDragEnable(false);
            ivDetectionTarget.setVisibility(View.GONE);
            tvRight.setVisibility(View.GONE);
            sbDetectionZoneSwitch.setEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void resumeData() {
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        tvRight = null;
        tvDetectionZoneTip = null;
        if (sbDetectionZoneSwitch != null) {
            sbDetectionZoneSwitch.setOnCheckedChangeListener(null);
            sbDetectionZoneSwitch = null;
        }
        if (ciwaSelectArea != null) {
            ciwaSelectArea.setOnBoundshangeListener(null);
            ciwaSelectArea = null;
        }
        ivDetectionTarget = null;
    }

    @OnClick({R.id.ivLeft, R.id.ivRight, R.id.tvRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                if (tvRight != null && tvRight.getTag() != null && (Integer)tvRight.getTag() == ConstantValue.EDIT_MODE_EDITING) {
                    if (mIsResetCropArea) {
                        break;
                    }
                    displayZoneEditView(ConstantValue.EDIT_MODE_EDITABLE, false);
                    refreshView(mLastMtAreaInfo);
                    break;
                }
                finish();
                break;
            case R.id.ivRight:
                if (mIsResetCropArea) {
                    break;
                }
                updateMtAreaInfo(mDeviceId, mSelectZoneRectF);
                displayZoneEditView(ConstantValue.EDIT_MODE_EDITABLE, false);
                break;
            case R.id.tvRight:
                if (mIsResetCropArea) {
                    break;
                }
                displayZoneEditView(ConstantValue.EDIT_MODE_EDITING, false);
                updateCropZone(mTargetViewWidth, mTargetViewHeight, mLastMtAreaInfo);
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull DetectionZoneContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private void refreshView(MTAreaInfo mtAreaInfo) {
        if (isDestroyed() || checkNull(sbDetectionZoneSwitch)) {
            return;
        }

        if (mtAreaInfo == null) {
            return;
        }

        if (sbDetectionZoneSwitch.isChecked() != mtAreaInfo.state) {
            sbDetectionZoneSwitch.toggleNoCallback();
        }
        displayZoneEditView((mtAreaInfo.state ? ConstantValue.EDIT_MODE_EDITABLE : ConstantValue.EDIT_MODE_NORMAL), mtAreaInfo.state);
        if (!NooieDeviceHelper.isMtAreaInfoInvalid(mtAreaInfo) && mtAreaInfo.state && !NooieDeviceHelper.isAreaRectInValid(mtAreaInfo.areaRects[0])) {
            updateCropZone(mTargetViewWidth, mTargetViewHeight, mtAreaInfo);
        } else {
            ciwaSelectArea.setVisibility(View.GONE);
        }
    }

    private void updateCropZone(int width, int height, MTAreaInfo mtAreaInfo) {
        if (mtAreaInfo == null || mtAreaInfo.areaRects == null || mtAreaInfo.areaRects.length < 1) {
            return;
        }
        RectF selectZoneRect = NooieDeviceHelper.convertSelectZoneRect(mtAreaInfo.horMaxSteps, mtAreaInfo.verMaxSteps, mtAreaInfo.areaRects[0]);
        NooieLog.d("-->> debug NooieDetectionZoneActivity onGetMtAreaInfo: 2 width=" + width + " height=" + height);
        if (selectZoneRect == null) {
            return;
        }
        NooieLog.d("-->> debug NooieDetectionZoneActivity onGetMtAreaInfo: 1 selectZoneRect left=" + selectZoneRect.left + " top=" + selectZoneRect.top + " right=" + selectZoneRect.right + " bottom=" + selectZoneRect.bottom);
        setIsResettingCropArea(true);
        TaskUtil.delayAction(((width > 0 && height > 0) ? 300 : 500), new TaskUtil.OnDelayTimeFinishListener() {
            @Override
            public void onFinish() {
                if (isDestroyed() || (checkNull(ciwaSelectArea))) {
                    return;
                }
                NooieLog.d("-->> debug NooieDetectionZoneActivity onGetMtAreaInfo: 2 width=" + ciwaSelectArea.getMeasuredWidth() + " height=" + ciwaSelectArea.getMeasuredHeight());
                resetCropZoneView(ciwaSelectArea.getMeasuredWidth(), ciwaSelectArea.getMeasuredHeight(), selectZoneRect);
                setIsResettingCropArea(false);
            }
        });
    }

    private void resetCropZoneView(int width, int height, RectF selectZoneRect) {
        if (checkNull(ciwaSelectArea)) {
            return;
        }
        RectF cropZoneRect = NooieDeviceHelper.reconvertZoneRectF(selectZoneRect, width, height);
        if (cropZoneRect == null) {
            ciwaSelectArea.setVisibility(View.GONE);
            return;
        }
        ciwaSelectArea.setVisibility(View.VISIBLE);
        ciwaSelectArea.setCropRect(cropZoneRect);
        ciwaSelectArea.invalidate();
    }

    private void updateSelectZoneParam(RectF selectZoneRectF) {
        mSelectZoneRectF = selectZoneRectF;
    }

    private void updateMtAreaInfo(String deviceId, boolean state) {
        if (isDestroyed() || TextUtils.isEmpty(deviceId)) {
            return;
        }
        if (mPresenter != null) {
            showLoading();
            mPresenter.updateMtAreaInfo(deviceId, UPDATE_DETECTION_ZONE_STATE, state);
        }
    }
    private void updateMtAreaInfo(String deviceId, RectF selectZoneRectF) {
        if (TextUtils.isEmpty(deviceId) || selectZoneRectF == null) {
            return;
        }
        AreaRect[] areaRects = createAreaRect();
        if (areaRects != null && areaRects.length > 1 && areaRects[0] != null) {
            areaRects[0].ltX = (int)selectZoneRectF.left;
            areaRects[0].ltY = (int)selectZoneRectF.top;
            areaRects[0].rbX = (int)selectZoneRectF.right;
            areaRects[0].rbY = (int)selectZoneRectF.bottom;
        }
        if (mPresenter != null) {
            showLoading();
            mPresenter.updateMtAreaInfo(mDeviceId, UPDATE_DETECTION_ZONE_AREA, selectZoneRectF);
        }
        /*
        Intent intent = new Intent();
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, 0);
        setResult(RESULT_OK, intent);
        finish();
        */
    }

    private AreaRect[] createAreaRect() {
        AreaRect[] areaRects = new AreaRect[5];
        for (int i = 0; i < areaRects.length; i++) {
            areaRects[i] = new AreaRect();
        }
        return areaRects;
    }

    private void setIsResettingCropArea(boolean isResettingCropArea) {
        mIsResetCropArea = isResettingCropArea;
    }

    @Override
    public void onUpdateMtAreaInfo(String result, int type, MTAreaInfo info) {
        NooieLog.d("-->> debug NooieDetectionZoneActivity onUpdateMtAreaInfo: result=" + result + " type=" + type);
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (type == UPDATE_DETECTION_ZONE_AREA) {
                Intent intent = new Intent();
                intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, 0);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                refreshView(info);
            }
            mLastMtAreaInfo = info;
        } else {
            ToastUtil.showToast(this, getString(R.string.get_fail));
        }
    }

    @Override
    public void onSetMtAreaInfo(String result) {
        if (isDestroyed()) {
            return;
        }
    }

    @Override
    public void onGetMtAreaInfo(String result, MTAreaInfo info) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        setIsResettingCropArea(false);
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            refreshView(info);
            mLastMtAreaInfo = info;
        }
    }
}
