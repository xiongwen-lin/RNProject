package com.afar.osaio.smart.router;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.device.bean.ParentalControlRuleInfo;
import com.afar.osaio.smart.routerlocal.UpdataRouterConnectDeviceInfo;
import com.afar.osaio.smart.setting.activity.RouterDetectionScheduleActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.InputFrameView;
import com.nooie.common.utils.log.NooieLog;
import com.suke.widget.SwitchButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 路由器 wifi settings 设置页
 */
public class RouterSetWifiActivity extends RouterBaseActivity implements SendHttpRequest.getRouterReturnInfo {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.switchSSID)
    SwitchButton switchSSID;
    @BindView(R.id.switchSSID_2)
    SwitchButton switchSSID_2;
    @BindView(R.id.switchSSID_5)
    SwitchButton switchSSID_5;
    @BindView(R.id.switch_password)
    SwitchButton switch_password;

    @BindView(R.id.smart_wifi_layout)
    View smart_wifi_layout;
    @BindView(R.id.smart_wifi_2G_layout)
    View smart_wifi_2G_layout;
    @BindView(R.id.smart_wifi_5G_layout)
    View smart_wifi_5G_layout;
    @BindView(R.id.layout_router_password)
    View layout_router_password;

    @BindView(R.id.layout_smart_wifi_name_2G)
    View layout_smart_wifi_name_2G;
    @BindView(R.id.layout_smart_wifi_password_2G)
    View layout_smart_wifi_password_2G;

    @BindView(R.id.layout_smart_wifi_name_5G)
    View layout_smart_wifi_name_5G;
    @BindView(R.id.layout_smart_wifi_password_5G)
    View layout_smart_wifi_password_5G;

    @BindView(R.id.wifi_name)
    InputFrameView wifi_name;
    @BindView(R.id.wifi_password)
    InputFrameView wifi_password;
    @BindView(R.id.wifi_name_2G)
    InputFrameView wifi_name_2G;
    @BindView(R.id.wifi_password_2G)
    InputFrameView wifi_password_2G;
    @BindView(R.id.wifi_name_5G)
    InputFrameView wifi_name_5G;
    @BindView(R.id.wifi_password_5G)
    InputFrameView wifi_password_5G;
    @BindView(R.id.router_password)
    InputFrameView router_password;

    @BindView(R.id.wifi_name_tips)
    TextView wifi_name_tips;
    @BindView(R.id.wifi_password_tips)
    TextView wifi_password_tips;
    @BindView(R.id.wifi_name_2G_tips)
    TextView wifi_name_2G_tips;
    @BindView(R.id.wifi_password_2G_tips)
    TextView wifi_password_2G_tips;
    @BindView(R.id.wifi_name_5G_tips)
    TextView wifi_name_5G_tips;
    @BindView(R.id.wifi_password_5G_tips)
    TextView wifi_password_5G_tips;
    @BindView(R.id.router_password_tips)
    TextView router_password_tips;

    @BindView(R.id.ssid_checkbox)
    CheckBox ssid_checkbox;
    @BindView(R.id.password_checkbox)
    CheckBox password_checkbox;
    @BindView(R.id.ssid_checkbox_2G)
    CheckBox ssid_checkbox_2G;
    @BindView(R.id.password_checkbox_2G)
    CheckBox password_checkbox_2G;
    @BindView(R.id.ssid_checkbox_5G)
    CheckBox ssid_checkbox_5G;
    @BindView(R.id.password_checkbox_5G)
    CheckBox password_checkbox_5G;

    @BindView(R.id.layout_wifi_schedule)
    View layout_wifi_schedule;
    @BindView(R.id.layout_wifi_qr_code)
    View layout_wifi_qr_code;
    @BindView(R.id.btnNext)
    FButton btnNext;
    @BindView(R.id.tvUseOnlyTip)
    TextView tvUseOnlyTip;

    private String deviceName = "";
    private String wifiManagement = "";
    private String routerReturnString = "";
    private JSONObject jsonObject;
    private List<ParentalControlRuleInfo> parentalControlRuleInfos = new ArrayList<>();


    public static void toRouterSetWifiActivity(Context from, String deviceName) {
        Intent intent = new Intent(from, RouterSetWifiActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, deviceName);
        from.startActivity(intent);
    }

    public static void toRouterSetWifiActivity(Context from, String deviceName, String wifiManagement/*, String routerReturnString*/) {
        Intent intent = new Intent(from, RouterSetWifiActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, deviceName);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_SETTING, wifiManagement);
        /*intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_RETURN_INFO, routerReturnString);*/
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_set_wifi);
        ButterKnife.bind(this);

        initData();
        initView();
        setupInputFrameView();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_wifi_setting);
        btnNext.setEnabled(false);
        btnNext.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        if (wifiManagement != null) {
            //layout_wifi_qr_code.setVisibility(View.VISIBLE);
            btnNext.setText(R.string.router_save);
        }

        switchSSID.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                switchSSID.setTag(isChecked ? 1 : 0);
                tvUseOnlyTip.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                checkBtnEnable();
                dealwithView(view.getId(), isChecked);
            }
        });

        switchSSID_2.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                switchSSID_2.setTag(isChecked ? 1 : 0);
                checkBtnEnable();
                dealwithView(view.getId(), isChecked);
            }
        });

        switchSSID_5.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                switchSSID_5.setTag(isChecked ? 1 : 0);
                checkBtnEnable();
                dealwithView(view.getId(), isChecked);
            }
        });

        switch_password.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                switch_password.setTag(isChecked ? 1 : 0);
                checkBtnEnable();
                dealwithView(view.getId(), isChecked);
            }
        });

        ssid_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ssid_checkbox.setTag(b ? 1 : 0);
//                wifi_name.setVisibility(b ? View.INVISIBLE : View.VISIBLE);
            }
        });

        password_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                password_checkbox.setTag(b ? 1 : 0);
            }
        });

        ssid_checkbox_2G.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ssid_checkbox_2G.setTag(b ? 1 : 0);
//                wifi_name_2G.setVisibility(b ? View.INVISIBLE : View.VISIBLE);
            }
        });

        password_checkbox_2G.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                password_checkbox_2G.setTag(b ? 1 : 0);
            }
        });

        ssid_checkbox_5G.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ssid_checkbox_5G.setTag(b ? 1 : 0);
//                wifi_name_5G.setVisibility(b ? View.INVISIBLE : View.VISIBLE);
            }
        });

        password_checkbox_5G.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                password_checkbox_5G.setTag(b ? 1 : 0);
            }
        });
    }

    private void initData() {
        switchSSID.setTag(0);
        switchSSID_2.setTag(1);
        switchSSID_5.setTag(1);
        switch_password.setTag(0);
        ssid_checkbox.setTag(0);
        ssid_checkbox_2G.setTag(0);
        ssid_checkbox_5G.setTag(0);
        password_checkbox.setTag(0);
        password_checkbox_2G.setTag(0);
        password_checkbox_5G.setTag(0);

        deviceName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
        wifiManagement = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_SETTING);
        //routerReturnString = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_RETURN_INFO);
        getRouterAdminPassword();
    }

    private void setupInputFrameView() {
        wifi_name.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT, R.color.black_80010c11)
                .setTextSSIDWatcher()
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setEtInputMaxLength(32)
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.black_80010c11)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                    }

                    @Override
                    public void onEtInputClick() {
                    }
                })
                .setInputTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        checkBtnEnable();
                    }
                });
        wifi_password.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT, R.color.black_80010c11)
                .setTextPasswordWatcher()
                .setEtInputToggle(true)
                .setEtInputMaxLength(64)
                .setBtnInputView(true)
                .setInputBtn(R.drawable.eye_close_icon_state_list)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                    }

                    @Override
                    public void onEtInputClick() {
                    }
                })
                .setInputTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        checkBtnEnable();
                    }
                });

        wifi_name_2G.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT, R.color.black_80010c11)
                .setTextSSIDWatcher()
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setEtInputMaxLength(32)
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.black_80010c11)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                    }

                    @Override
                    public void onEtInputClick() {
                    }
                })
                .setInputTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        checkBtnEnable();
                    }
                });

        wifi_password_2G.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT, R.color.black_80010c11)
                .setTextPasswordWatcher()
                .setEtInputToggle(true)
                .setEtInputMaxLength(64)
                .setBtnInputView(true)
                .setInputBtn(R.drawable.eye_close_icon_state_list)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                    }

                    @Override
                    public void onEtInputClick() {
                    }
                })
                .setInputTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        checkBtnEnable();
                    }
                });

        wifi_name_5G.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT, R.color.black_80010c11)
                .setTextSSIDWatcher()
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setEtInputMaxLength(32)
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.black_80010c11)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                    }

                    @Override
                    public void onEtInputClick() {
                    }
                })
                .setInputTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        checkBtnEnable();
                    }
                });

        wifi_password_5G.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT, R.color.black_80010c11)
                .setTextPasswordWatcher()
                .setEtInputToggle(true)
                .setEtInputMaxLength(64)
                .setBtnInputView(true)
                .setInputBtn(R.drawable.eye_close_icon_state_list)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                    }

                    @Override
                    public void onEtInputClick() {
                    }
                })
                .setInputTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        checkBtnEnable();
                    }
                });

        router_password.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT, R.color.black_80010c11)
                .setTextPasswordWatcher()
                .setEtInputToggle(true)
                .setEtInputMaxLength(32)
                .setBtnInputView(true)
                .setInputBtn(R.drawable.eye_close_icon_state_list)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                    }

                    @Override
                    public void onEtInputClick() {
                    }
                })
                .setInputTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        checkBtnEnable();
                    }
                });
    }

    private void dealwithView(int id, boolean isChecked) {
        if (id == R.id.switchSSID) {
            smart_wifi_layout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            smart_wifi_2G_layout.setVisibility(!isChecked ? View.VISIBLE : View.GONE);
            smart_wifi_5G_layout.setVisibility(!isChecked ? View.VISIBLE : View.GONE);
        } else if (id == R.id.switchSSID_2) {
            layout_smart_wifi_name_2G.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            layout_smart_wifi_password_2G.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        } else if (id == R.id.switchSSID_5) {
            layout_smart_wifi_name_5G.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            layout_smart_wifi_password_5G.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        } else if (id == R.id.switch_password) {
            layout_router_password.setVisibility(!isChecked ? View.VISIBLE : View.GONE);
        }

        if ((int) switchSSID_2.getTag() == 0 && (int) switchSSID_5.getTag() == 0) {
            layout_wifi_qr_code.setVisibility(View.GONE);
        } else if (wifiManagement != null && ((int) switchSSID_2.getTag() == 1 || (int) switchSSID_5.getTag() == 1)) {
            //layout_wifi_qr_code.setVisibility(View.VISIBLE);
        }
    }

    private void initViewShow() {
        try {
            switchSSID.setTag("1".equals(jsonObject.getString("merge")) ? 1 : 0);
            switchSSID.setChecked("1".equals(jsonObject.getString("merge")) ? true : false);

            if ("1".equals(jsonObject.getString("merge"))) {
                tvUseOnlyTip.setVisibility(View.GONE);
            } else {
                tvUseOnlyTip.setVisibility(View.VISIBLE);

            }

            dealwithView(R.id.switchSSID, "1".equals(jsonObject.getString("merge")) ? true : false);
            NooieLog.d("xxxxxxxxxxxxxxxxxxxxxxxxxname: " + jsonObject.getString("ssid").trim());
            wifi_name.setEtInputText(jsonObject.getString("ssid").trim());
            wifi_password.setEtInputText(jsonObject.getString("key"));
            ssid_checkbox.setTag("1".equals(jsonObject.getString("hssid")) ? 1 : 0);
            ssid_checkbox.setChecked("1".equals(jsonObject.getString("hssid")) ? true : false);
//            wifi_name.setVisibility("1".equals(jsonObject.getString("hssid")) ? View.INVISIBLE : View.VISIBLE);

            switchSSID_2.setTag("0".equals(jsonObject.getString("wifiOff")) ? 1 : 0);
            switchSSID_2.setChecked("0".equals(jsonObject.getString("wifiOff")) ? true : false);
            dealwithView(R.id.switchSSID_2, "0".equals(jsonObject.getString("wifiOff")) ? true : false);

            wifi_name_2G.setEtInputText(jsonObject.getString("ssid").trim());
            wifi_password_2G.setEtInputText(jsonObject.getString("key"));
            ssid_checkbox_2G.setTag("1".equals(jsonObject.getString("hssid")) ? 1 : 0);
            ssid_checkbox_2G.setChecked("1".equals(jsonObject.getString("hssid")) ? true : false);
//            wifi_name_2G.setVisibility("1".equals(jsonObject.getString("hssid")) ? View.INVISIBLE : View.VISIBLE);

            switchSSID_5.setTag("0".equals(jsonObject.getString("wifiOff5g")) ? 1 : 0);
            switchSSID_5.setChecked("0".equals(jsonObject.getString("wifiOff5g")) ? true : false);
            dealwithView(R.id.switchSSID_5, "0".equals(jsonObject.getString("wifiOff5g")) ? true : false);

            wifi_name_5G.setEtInputText(jsonObject.getString("ssid5g").trim());
            wifi_password_5G.setEtInputText(jsonObject.getString("key5g"));
            ssid_checkbox_5G.setTag("1".equals(jsonObject.getString("hssid5g")) ? 1 : 0);
            ssid_checkbox_5G.setChecked("1".equals(jsonObject.getString("hssid5g")) ? true : false);
//            wifi_name_5G.setVisibility("1".equals(jsonObject.getString("hssid5g")) ? View.INVISIBLE : View.VISIBLE);

            // 加密  密码为空可以不传  密码大于8 没有勾选box传1  大于8且勾选了传2
            password_checkbox.setChecked("2".endsWith(jsonObject.getString("wpaMode")) ? true : false);
            password_checkbox.setTag("2".endsWith(jsonObject.getString("wpaMode")) ? 1 : 0);

            password_checkbox_2G.setChecked("2".endsWith(jsonObject.getString("wpaMode")) ? true : false);
            password_checkbox_2G.setTag("2".endsWith(jsonObject.getString("wpaMode")) ? 1 : 0);

            password_checkbox_5G.setChecked("2".endsWith(jsonObject.getString("wpaMode5g")) ? true : false);
            password_checkbox_5G.setTag("2".endsWith(jsonObject.getString("wpaMode5g")) ? 1 : 0);

            GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
            switch_password.setChecked(prefs.getIsOpenRouterAdminPasswordSwitch());
            switch_password.setTag(prefs.getIsOpenRouterAdminPasswordSwitch() ? 1 : 0);
            dealwithView(R.id.switch_password, prefs.getIsOpenRouterAdminPasswordSwitch());

            /*if(null == jsonObject.getString("loginpass")){
               router_password.setEtInputText("admin");
            }else{
                router_password.setEtInputText(jsonObject.getString("loginpass"));
                prefs.setRouterAdminPassword(jsonObject.getString("loginpass"),false);
            }*/


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBtnEnable();
        getWizardCfg();
    }


    @OnClick({R.id.ivLeft, R.id.btnNext, R.id.layout_wifi_schedule, R.id.layout_wifi_qr_code})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnNext:
                btnNext.setTag(1);
                setRouterWizardCfg();
                break;
            case R.id.layout_wifi_schedule:
                RouterDetectionScheduleActivity.toRouterDetectionScheduleActivity(this, "", deviceName, "router", parentalControlRuleInfos);
                break;
            case R.id.layout_wifi_qr_code:
                if (jsonObject == null) {
                    return;
                }
                try {
                    RouterSetWifiManagerActivity.toRouterSetWifiManagerActivity(this, (int) switchSSID.getTag(), (int) switchSSID_2.getTag(), (int) switchSSID_5.getTag(),
                            jsonObject.getString("ssid"), jsonObject.getString("key"),
                            jsonObject.getString("ssid5g"), jsonObject.getString("key5g"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void checkBtnEnable() {
        if ((int) switch_password.getTag() == 0) {
            if (/*!"admin".equals(router_password.getInputText()) && */router_password.getInputTextNoTrim().length() <= 0) {
                btnNext.setEnabled(false);
                btnNext.setTextColor(getResources().getColor(R.color.unable_clickable_color));
                return;
            }
        }
        NooieLog.d("xxxxxxxxxxxxxxxxsize: " + wifi_name_2G.getInputText().length());
        // 双频合一
        if ((int) switchSSID.getTag() == 1) {
            if (!TextUtils.isEmpty(wifi_name.getInputText()) && wifi_password.getInputTextNoTrim().length() >= 8) {
                btnNext.setEnabled(true);
                btnNext.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
            } else {
                btnNext.setEnabled(false);
                btnNext.setTextColor(getResources().getColor(R.color.unable_clickable_color));
            }
            return;
        }

        if ((int) switchSSID_2.getTag() == 1 && (int) switchSSID_5.getTag() == 1) {
            if (!TextUtils.isEmpty(wifi_name_2G.getInputText()) && wifi_password_2G.getInputTextNoTrim().length() >= 8
                    && !TextUtils.isEmpty(wifi_name_5G.getInputText()) && wifi_password_5G.getInputTextNoTrim().length() >= 8) {
                btnNext.setEnabled(true);
                btnNext.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
            } else {
                btnNext.setEnabled(false);
                btnNext.setTextColor(getResources().getColor(R.color.unable_clickable_color));
            }
            return;
        }

        if ((int) switchSSID_2.getTag() == 1) {
            if (!TextUtils.isEmpty(wifi_name_2G.getInputText()) && wifi_password_2G.getInputTextNoTrim().length() >= 8) {
                btnNext.setEnabled(true);
                btnNext.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
            } else {
                btnNext.setEnabled(false);
                btnNext.setTextColor(getResources().getColor(R.color.unable_clickable_color));
            }
            return;
        }

        if ((int) switchSSID_5.getTag() == 1) {
            if (!TextUtils.isEmpty(wifi_name_5G.getInputText()) && wifi_password_5G.getInputTextNoTrim().length() >= 8) {
                btnNext.setEnabled(true);
                btnNext.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
            } else {
                btnNext.setEnabled(false);
                btnNext.setTextColor(getResources().getColor(R.color.unable_clickable_color));
            }
            return;
        }

        if ((int) switchSSID.getTag() == 1) {
            if ((int) switchSSID_2.getTag() == 0 && (int) switchSSID_5.getTag() == 0) {
                btnNext.setEnabled(true);
                btnNext.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
            }
        }
    }

    private void getWiFiScheduleCfg() {
        showLoadingDialog();
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.getWiFiScheduleCfg();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * ①设置后wifi关闭了，问询参数是否标错了意思
     * ②单个时间条没有状态，不知道是要展示开还是关
     */
    private void setWiFiScheduleCfg() {
        showLoadingDialog();
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.setWiFiScheduleCfg("1", "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setRouterWizardCfg() {
        setRouterAdminPassword();
        showLoadingDialog();
        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        List<String> internetModeInfo = prefs.getRouterInternetMode();
        String[] netInfo = new String[]{"", "", "", "", "", "", ""};

        try {
            if (wifiManagement != null) {
                if ("0".equals(jsonObject.getString("proto"))) {
                    netInfo[0] = jsonObject.getString("staticIp");
                    netInfo[1] = jsonObject.getString("staticMask");
                    netInfo[2] = jsonObject.getString("staticGw");
                    netInfo[3] = jsonObject.getString("priDns");
                    if (jsonObject.has("secDns")) {
                        netInfo[4] = jsonObject.getString("secDns");
                    }
                } else if ("3".equals(jsonObject.getString("proto"))) {
                    netInfo[5] = jsonObject.getString("pppoeUser");
                    netInfo[6] = jsonObject.getString("pppoePass");
                }
            } else {
                if ("0".equals(internetModeInfo.get(0))) {
                    netInfo[0] = internetModeInfo.get(1);
                    netInfo[1] = internetModeInfo.get(2);
                    netInfo[2] = internetModeInfo.get(3);
                    netInfo[3] = internetModeInfo.get(4);
                    netInfo[4] = internetModeInfo.get(5);
                } else if ("3".equals(internetModeInfo.get(0))) {
                    netInfo[5] = internetModeInfo.get(1);
                    netInfo[6] = internetModeInfo.get(2);
                }
            }

            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            // 双频合一
            if ((int) switchSSID.getTag() == 1) {
                routerDataFromCloud.setWizardCfg("UTC" + timeZoneConversion(),
                        wifiManagement != null ? jsonObject.getString("proto") : internetModeInfo.get(0),
                        "0".equals(wifiManagement != null ? jsonObject.getString("proto") : internetModeInfo.get(0)) ? "1" : "0",
                        "1",
                        "0", (int) ssid_checkbox.getTag() == 1 ? "1" : "0", (int) password_checkbox.getTag() == 1 ? "2" : "1",
                        wifi_name.getInputTextNoTrim(), wifi_password.getInputTextNoTrim(),
                        "0", (int) ssid_checkbox.getTag() == 1 ? "1" : "0", (int) password_checkbox.getTag() == 1 ? "2" : "1",
                        wifi_name.getInputTextNoTrim(), wifi_password.getInputTextNoTrim(),
                        router_password.getInputTextNoTrim(), "0", netInfo[0], netInfo[1], netInfo[2], netInfo[3], netInfo[4], netInfo[5], netInfo[6]);
            } else {
                routerDataFromCloud.setWizardCfg("UTC" + timeZoneConversion(),
                        wifiManagement != null ? jsonObject.getString("proto") : internetModeInfo.get(0),
                        "0".equals(wifiManagement != null ? jsonObject.getString("proto") : internetModeInfo.get(0)) ? "1" : "0",
                        "0", (int) switchSSID_2.getTag() == 1 ? "0" : "1", (int) ssid_checkbox_2G.getTag() == 1 ? "1" : "0",
                        (int) password_checkbox_2G.getTag() == 1 ? "2" : "1", wifi_name_2G.getInputTextNoTrim(), wifi_password_2G.getInputTextNoTrim(),
                        (int) switchSSID_5.getTag() == 1 ? "0" : "1", (int) ssid_checkbox_5G.getTag() == 1 ? "1" : "0",
                        (int) password_checkbox_5G.getTag() == 1 ? "2" : "1", wifi_name_5G.getInputTextNoTrim(), wifi_password_5G.getInputTextNoTrim(),
                        router_password.getInputTextNoTrim(), "0", netInfo[0], netInfo[1], netInfo[2], netInfo[3], netInfo[4], netInfo[5], netInfo[6]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getWizardCfg() {
        showLoadingDialog();
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.getWizardCfg();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getRouterAdminPassword() {
        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        router_password.setEtInputText(prefs.getRouterAdminPassword());
    }

    private void setRouterAdminPassword() {
        String password = "admin";
        // 用wifi密码作为路由器的登录密码
        if (switch_password.isChecked()) {
            if ((int) switchSSID.getTag() == 1) {
                if (!"".equals(wifi_password.getInputTextNoTrim())) {
                    password = wifi_password.getInputTextNoTrim();
                }
            } else {
                if (!"".equals(wifi_password_2G.getInputTextNoTrim())) {
                    password = wifi_password_2G.getInputTextNoTrim();
                }
            }
        } else {
            // 以自己修改的密码为路由器的登录密码
            password = router_password.getInputTextNoTrim();
        }
        router_password.setEtInputText(password);
        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        prefs.setRouterAdminPassword(password, switch_password.isChecked());
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                initViewShow();
            } else if (msg.what == 2) {
                ToastUtil.showToast(RouterSetWifiActivity.this, getString(R.string.router_network_exception));
            }

            if (msg.what != 2 && !"".equals(wifiManagement)) {
                getWiFiScheduleCfg();
            }
        }
    };

    @Override
    public void routerReturnInfo(String info, String topicurlString) {
        hideLoadingDialog();
        NooieLog.d("xxxxxxxxxxxxxxxxxx: " + info);
        try {
            if ("getWizardCfg".equals(topicurlString) && !"error".equals(info)) {
                jsonObject = new JSONObject(info);
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            } else if ("setWizardCfg".equals(topicurlString) && !"error".equals(info)) {
                if ((int) btnNext.getTag() == 1) {
                    if (wifiManagement != null) {
                        finish();
                    } else {
                        RouterBackupInfoForPhoneActivity.toRouterBackupInfoForPhoneActivity(this, deviceName);
                    }
                }
            } else if ("getWiFiScheduleCfg".equals(topicurlString) && !"error".equals(info)) {
                parentalControlRuleInfos = UpdataRouterConnectDeviceInfo.getWifiRules(info);
            } else if ("setWiFiScheduleCfg".equals(topicurlString) && !"error".equals(info)) {
                // 设置无线定时开关总开关
                Message message = new Message();
                message.what = 3;
                handler.sendMessage(message);
            } else if ("error".equals(info) || "".equals(info)) {
                // 当前连接网络不是路由器wifi
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String timeZoneConversion() {
        return super.timeZoneConversion();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showLoadingDialog() {
        showLoading("");
    }

    @Override
    public void hideLoadingDialog() {
        hideLoading();
    }
}
