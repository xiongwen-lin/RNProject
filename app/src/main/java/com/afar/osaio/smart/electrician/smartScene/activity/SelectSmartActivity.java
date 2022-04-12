package com.afar.osaio.smart.electrician.smartScene.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;

import java.io.Serializable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SelectSmartActivity extends BaseActivity {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvLaunch)
    TextView tvLaunch;
    @BindView(R.id.ivLaunch)
    ImageView ivLaunch;

    private String sceneCondition;
    private String sceneTask;
    private boolean isAdd;
    private SceneBean mSceneBean;

    public static void toSelectSmartActivity(Activity from, String sceneCondition) {
        Intent intent = new Intent(from, SelectSmartActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        from.startActivity(intent);
    }

    public static void toSelectSmartActivity(Activity from, String sceneCondition, String sceneTask, boolean isAdd, Serializable sceneBean) {
        Intent intent = new Intent(from, SelectSmartActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEBEAN, sceneBean);
        from.startActivity(intent);
    }

    public static void toSelectSmartActivity(Activity from, String sceneCondition, String sceneTask, boolean isAdd) {
        Intent intent = new Intent(from, SelectSmartActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENECONDITION, sceneCondition);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENETASK, sceneTask);
        intent.putExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, isAdd);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSlideable(false);
        setContentView(R.layout.activity_select_smart);
        ButterKnife.bind(this);
        initView();
        processExtraData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(getText(R.string.select_smart));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        NooieLog.e("---------->SelectSmartActivity onNewIntent");
        processExtraData();
    }

    private void processExtraData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            sceneCondition = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENECONDITION);
            mSceneBean = (SceneBean) getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_SCENEBEAN);
            sceneTask = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCENETASK);
            isAdd = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_SCENEE_ADD, false);

            if (!TextUtils.isEmpty(sceneCondition)) {
                if (sceneCondition.equals("null")||sceneCondition.equals("[]")) {
                    return;
                }
                tvLaunch.setTextColor(getResources().getColor(R.color.theme_text_color));
                ivLaunch.setImageResource(R.drawable.scene_right_arrow);
            }
        }
    }

    @OnClick({R.id.ivLeft, R.id.clLaunch, R.id.clAutomation})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.clLaunch: {
                if (TextUtils.isEmpty(sceneCondition)) {
                    ToastUtil.showToast(this, R.string.launch_not_add);
                } else {
                    if (sceneCondition.equals("null")||sceneCondition.equals("[]")) {
                        ToastUtil.showToast(this, R.string.launch_not_add);
                    } else {
                        if (isAdd) {
                            if (mSceneBean != null) {
                                LaunchActivity.toLaunchActivity(this, sceneCondition, sceneTask, isAdd, mSceneBean);
                            } else {
                                LaunchActivity.toLaunchActivity(this, sceneCondition, sceneTask, isAdd);
                            }
                        } else {
                            LaunchActivity.toLaunchActivity(this, sceneCondition);
                        }
                    }
                }
                break;
            }
            case R.id.clAutomation: {
                if (isAdd) {
                    if (mSceneBean != null) {
                        AutomationActivity.toAutomationActivity(this, sceneCondition, sceneTask, isAdd, mSceneBean);
                    } else {
                        AutomationActivity.toAutomationActivity(this, sceneCondition, sceneTask, isAdd);
                    }
                } else {
                    AutomationActivity.toAutomationActivity(this, sceneCondition);
                }
                break;
            }
        }
    }
}
