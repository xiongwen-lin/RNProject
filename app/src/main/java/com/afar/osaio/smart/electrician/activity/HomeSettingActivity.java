package com.afar.osaio.smart.electrician.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.eventbus.HomeChangeEvent;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.presenter.HomeSettingPresenter;
import com.afar.osaio.smart.electrician.presenter.IHomeSettingPresenter;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.IHomeSettingView;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.FButton;
import com.tuya.smart.api.service.MicroServiceManager;
import com.tuya.smart.commonbiz.bizbundle.family.api.AbsBizBundleFamilyService;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * HomeSettingActivity
 *
 * @author Administrator
 * @date 2019/3/14
 */
public class HomeSettingActivity extends BaseActivity implements IHomeSettingView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.homeNameBar)
    View homeNameBar;
    @BindView(R.id.tvHomeName)
    TextView tvHomeName;
    @BindView(R.id.ivRightarrow)
    ImageView ivRightArrow;
    @BindView(R.id.btnHomeRemove)
    FButton btnHomeRemove;

    private IHomeSettingPresenter mHomeSettingPresenter;
    private long mHomeId;
    private boolean isChange;
    private boolean isAdmin;

    public static void toHomeSettingActivity(Activity from, int requestCode, long homeId, boolean isAdmin) {
        Intent intent = new Intent(from, HomeSettingActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_HOME_ID, homeId);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_HOME_ADMIN, isAdmin);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_setting);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.home_settings);
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mHomeId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_HOME_ID, 0);
            isAdmin = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_IS_HOME_ADMIN, false);
            //initUi(isAdmin);
            mHomeSettingPresenter = new HomeSettingPresenter(this);
            updateUI(TuyaHomeSdk.getDataInstance().getHomeBean(mHomeId));
            isChange = false;
        }
    }

    @OnClick({R.id.ivLeft, R.id.homeNameBar, R.id.btnHomeRemove})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                if (isChange) {
                    setResult(RESULT_OK, new Intent());
                }
                finish();
                break;
            }
            case R.id.homeNameBar: {
                NameHomeActivity.toNameHomeActivity(this, ConstantValue.REQUEST_CODE_HOME_RENAME, mHomeId, tvHomeName.getText().toString());
                break;
            }
            case R.id.btnHomeRemove: {
                DialogUtil.showConfirmWithSubMsgDialog(this, R.string.remove_home, R.string.home_remove_tip, R.string.cancel, R.string.confirm_upper, mRemoveListener);
                break;
            }
        }
    }

    private void initUi(boolean isAdmin) {
        btnHomeRemove.setVisibility(isAdmin ? View.VISIBLE : View.INVISIBLE);
        ivRightArrow.setVisibility(isAdmin ? View.VISIBLE : View.INVISIBLE);
    }

    private DialogUtil.OnClickInputDialogListener mRenameListener = new DialogUtil.OnClickInputDialogListener() {
        @Override
        public void onClickCancel() {
        }

        @Override
        public void onClickSave(String text) {
            if (!TextUtils.isEmpty(text)) {
                mHomeSettingPresenter.updateHome(TuyaHomeSdk.getDataInstance().getHomeBean(mHomeId), text);
            } else {
            }
        }
    };

    private DialogUtil.OnClickConfirmButtonListener mRemoveListener = new DialogUtil.OnClickConfirmButtonListener() {
        @Override
        public void onClickRight() {
            showLoadingDialog();
            mHomeSettingPresenter.removeHome(mHomeId);
        }

        @Override
        public void onClickLeft() {
        }
    };

    private void updateUI(HomeBean homeBean) {
        if (homeBean != null) {
            tvHomeName.setText(homeBean.getName());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ConstantValue.REQUEST_CODE_HOME_RENAME:
                    isChange = true;
                    mHomeSettingPresenter.refreshHome();
                    break;
            }
        }
    }

    @Override
    public void notifyUpdateHomeState(String msg) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            mHomeSettingPresenter.refreshHome();
        } else {
        }
    }

    @Override
    public void notifyRemoveHomeState(String msg) {
        hideLoadingDialog();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            AbsBizBundleFamilyService service = MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
            //设置为当前家庭的homeId
            service.setCurrentHomeId(FamilyManager.getInstance().getCurrentHomeId());
            //EventBus.getDefault().post(new HomeChangeEvent(FamilyManager.getInstance().getCurrentHomeId()));
            HomeActivity.toHomeActivity(HomeSettingActivity.this, HomeActivity.TYPE_REMOVE_HOME);
            finish();
        } else {
            ErrorHandleUtil.toastTuyaError(this, msg);
        }
    }

    @Override
    public void notifyRefreshHomeState(String msg) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            AbsBizBundleFamilyService service = MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
            //设置为当前家庭的homeId
            service.setCurrentHomeId(FamilyManager.getInstance().getCurrentHomeId());
            EventBus.getDefault().post(new HomeChangeEvent(FamilyManager.getInstance().getCurrentHomeId()));
            updateUI(FamilyManager.getInstance().getCurrentHome());
        } else {
        }
    }
}
