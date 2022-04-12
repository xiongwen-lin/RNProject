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
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.event.RouterOnLineStateEvent;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.InputFrameView;
import com.afar.osaio.widget.RouterGuestWifiPopupWindows;
import com.suke.widget.SwitchButton;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RouterGuestWifiActivity extends RouterBaseActivity implements SendHttpRequest.getRouterReturnInfo {

    public static void toRouterGuestWifiActivity(Context from) {
        Intent intent = new Intent(from, RouterGuestWifiActivity.class);
        from.startActivity(intent);
    }

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;

    @BindView(R.id.tvSmartTips)
    TextView tvTips;
    @BindView(R.id.ivSmartIcon)
    ImageView ivIcon;
    @BindView(R.id.smart_guest_text)
    TextView guest_text;
    @BindView(R.id.switchSmartButton)
    SwitchButton switchButton;
    @BindView(R.id.smart_layout)
    View layout;
    @BindView(R.id.layout_smart_wifi_set)
    View layout_smart_wifi_set;

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
    @BindView(R.id.ivIconSmartSwitch)
    View ivIconSmartSwitch;

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
    @BindView(R.id.layout_height)
    View layout_height;
    @BindView(R.id.btnNext)
    FButton btnNext;

    @BindView(R.id.id_smart_ssid)
    View id_smart_ssid;
    @BindView(R.id.layout_smart_wifi_password)
    View layout_smart_wifi_password;

    // RouterGuestWifiPopupWindows
    private RouterGuestWifiPopupWindows mPopMenus;
    private JSONObject jsonObject;
    private List<String> routerWifiInfo = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_set_wifi);
        ButterKnife.bind(this);

        initView();
        initData();
        setupInputFrameView();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_guest_wifi_guest_wifi);
        btnNext.setEnabled(false);
        btnNext.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        wifi_name_5G.setEtInputMaxLength(32);
        wifi_name_2G.setEtInputMaxLength(32);
        wifi_name.setEtInputMaxLength(32);

        layout_smart_wifi_set.setVisibility(View.VISIBLE);
        tvTips.setVisibility(View.GONE);
        ivIconSmartSwitch.setVisibility(View.GONE);
        layout_router_password.setVisibility(View.GONE);
        layout_height.setVisibility(View.GONE);

        password_checkbox.setVisibility(View.GONE);
        password_checkbox_2G.setVisibility(View.GONE);
        password_checkbox_5G.setVisibility(View.GONE);
        layout_wifi_schedule.setVisibility(View.GONE);
        //layout_wifi_qr_code.setVisibility(View.VISIBLE);
        btnNext.setText(getString(R.string.router_save));

        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                switchButton.setTag(isChecked ? 1 : 0);
                setViewShow(isChecked);
            }
        });
        //setupInputFrameView();

        switchSSID.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                switchSSID.setTag(isChecked ? 1 : 0);
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
                password_checkbox.setTag(b ? 2 : 0);
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
                password_checkbox_2G.setTag(b ? 2 : 0);
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
                password_checkbox_5G.setTag(b ? 2 : 0);
            }
        });
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
    }

    private void setViewShow(boolean isShow) {
        int flag = (int)switchSSID.getTag();
        if (isShow) {
            id_smart_ssid.setVisibility(View.VISIBLE);
            smart_wifi_layout.setVisibility(flag == 1 ? View.VISIBLE : View.GONE);
            smart_wifi_2G_layout.setVisibility(flag == 1 ? View.GONE : View.VISIBLE);
            smart_wifi_5G_layout.setVisibility(flag == 1 ? View.GONE : View.VISIBLE);
            layout_smart_wifi_password.setVisibility(View.VISIBLE);
        } else {
            id_smart_ssid.setVisibility(View.GONE);
            smart_wifi_layout.setVisibility(View.GONE);
            smart_wifi_2G_layout.setVisibility(View.GONE);
            smart_wifi_5G_layout.setVisibility(View.GONE);
            layout_smart_wifi_password.setVisibility(View.GONE);
        }
    }

    private void initData() {
        switchButton.setTag(0);
        switchSSID.setTag(0);
        switchSSID_2.setTag(1);
        switchSSID_5.setTag(1);
        ssid_checkbox.setTag(0);
        ssid_checkbox_2G.setTag(0);
        ssid_checkbox_5G.setTag(0);
        password_checkbox.setTag(0);
        password_checkbox_2G.setTag(0);
        password_checkbox_5G.setTag(0);
        getRouterGuestWifi();
    }

    private void loadViewInfo() {
        try {
            if ("1".equals(jsonObject.getString("wifiOff5g")) && "1".equals(jsonObject.getString("wifiOff"))) {
                switchButton.setTag(0);
                switchButton.setChecked(false);
                setViewShow(false);
            } else {
                switchButton.setTag(1);
                switchButton.setChecked(true);
                setViewShow(true);
            }

            switchSSID.setTag("1".equals(jsonObject.getString("merge")) ? 1 : 0);
            switchSSID.setChecked("1".equals(jsonObject.getString("merge")) ? true : false);
            dealwithView(R.id.switchSSID, "1".equals(jsonObject.getString("merge")) ? true : false);

            wifi_name.setEtInputText(jsonObject.getString("ssid"));
            wifi_password.setEtInputText(jsonObject.getString("key"));
            ssid_checkbox.setTag("1".equals(jsonObject.getString("hssid")) ? 1 : 0);
            ssid_checkbox.setChecked("1".equals(jsonObject.getString("hssid")) ? true : false);


            switchSSID_2.setTag("0".equals(jsonObject.getString("wifiOff")) ? 1 : 0);
            switchSSID_2.setChecked("0".equals(jsonObject.getString("wifiOff")) ? true : false);
            dealwithView(R.id.switchSSID_2, "0".equals(jsonObject.getString("wifiOff")) ? true : false);

            wifi_name_2G.setEtInputText(jsonObject.getString("ssid"));
            wifi_password_2G.setEtInputText(jsonObject.getString("key"));
            ssid_checkbox_2G.setTag("1".equals(jsonObject.getString("hssid")) ? 1 : 0);
            ssid_checkbox_2G.setChecked("1".equals(jsonObject.getString("hssid")) ? true : false);


            switchSSID_5.setTag("0".equals(jsonObject.getString("wifiOff5g")) ? 1 : 0);
            switchSSID_5.setChecked("0".equals(jsonObject.getString("wifiOff5g")) ? true : false);
            dealwithView(R.id.switchSSID_5, "0".equals(jsonObject.getString("wifiOff5g")) ? true : false);

            wifi_name_5G.setEtInputText(jsonObject.getString("ssid5g"));
            wifi_password_5G.setEtInputText(jsonObject.getString("key5g"));
            ssid_checkbox_5G.setTag("1".equals(jsonObject.getString("hssid5g")) ? 1 : 0);
            ssid_checkbox_5G.setChecked("1".equals(jsonObject.getString("hssid5g")) ? true : false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        checkBtnEnable();
    }

    private void dealwithView(int id, boolean isChecked) {
        if ((int)switchButton.getTag() == 0) {
            smart_wifi_layout.setVisibility(View.GONE);
            smart_wifi_2G_layout.setVisibility(View.GONE);
            smart_wifi_5G_layout.setVisibility(View.GONE);
            layout_smart_wifi_name_2G.setVisibility(View.GONE);
            layout_smart_wifi_password_2G.setVisibility(View.GONE);
            layout_smart_wifi_name_5G.setVisibility(View.GONE);
            layout_smart_wifi_password_5G.setVisibility(View.GONE);
            layout_router_password.setVisibility(View.GONE);
            layout_wifi_qr_code.setVisibility(View.GONE);
        } else {
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

            if ((int)switchSSID_2.getTag() == 0 && (int)switchSSID_5.getTag() == 0) {
                layout_wifi_qr_code.setVisibility(View.GONE);
            } else if ((int)switchSSID_2.getTag() == 1 || (int)switchSSID_5.getTag() == 1){
                //layout_wifi_qr_code.setVisibility(View.VISIBLE);
            }
        }
    }

    private void checkBtnEnable() {
        // 双频合一
        if ((int)switchSSID.getTag() == 1) {
            if (!TextUtils.isEmpty(wifi_name.getInputText()) && wifi_password.getInputTextNoTrim().length() >= 8) {
                btnNext.setEnabled(true);
                btnNext.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
            } else {
                btnNext.setEnabled(false);
                btnNext.setTextColor(getResources().getColor(R.color.unable_clickable_color));
            }
            return;
        }

        if ((int)switchSSID_2.getTag() == 1 && (int)switchSSID_5.getTag() == 1) {
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

        if ((int)switchSSID_2.getTag() == 1) {
            if (!TextUtils.isEmpty(wifi_name_2G.getInputText()) && wifi_password_2G.getInputTextNoTrim().length() >= 8) {
                btnNext.setEnabled(true);
                btnNext.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
            } else {
                btnNext.setEnabled(false);
                btnNext.setTextColor(getResources().getColor(R.color.unable_clickable_color));
            }
            return;
        }

        if ((int)switchSSID_5.getTag() == 1) {
            if (!TextUtils.isEmpty(wifi_name_5G.getInputText()) && wifi_password_5G.getInputTextNoTrim().length() >= 8) {
                btnNext.setEnabled(true);
                btnNext.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
            } else {
                btnNext.setEnabled(false);
                btnNext.setTextColor(getResources().getColor(R.color.unable_clickable_color));
            }
            return;
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
    }

    private void setRouterGuestWifi() {
        try {
            showLoadingDialog();
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            // 双频合一
            if ((int)switchSSID.getTag() == 1) {
                routerDataFromCloud.setWiFiEasyGuestCfg("0",
                        wifi_name.getInputTextNoTrim(), wifi_password.getInputTextNoTrim(),
                        (int)switchButton.getTag() == 0 ? "1" : "0",
                        (int)ssid_checkbox.getTag() == 1 ? "1" : "0",
                        "1" ,
                        wifi_name.getInputTextNoTrim(), wifi_password.getInputTextNoTrim(),
                        (int)switchButton.getTag() == 0 ? "1" : "0",
                        (int)ssid_checkbox.getTag() == 1 ? "1" : "0",
                        "0");
            } else {
                routerDataFromCloud.setWiFiEasyGuestCfg("0",
                        wifi_name_2G.getInputTextNoTrim(), wifi_password_2G.getInputTextNoTrim(),
                        (int)switchButton.getTag() == 0 ? "1" : ((int)switchSSID_2.getTag() == 1 ? "0" : "1"),
                        (int)ssid_checkbox_2G.getTag() == 1 ? "1" : "0",
                        "0" ,
                        wifi_name_5G.getInputTextNoTrim(), wifi_password_5G.getInputTextNoTrim(),
                        (int)switchButton.getTag() == 0 ? "1" : ((int)switchSSID_5.getTag() == 1 ? "0" : "1"),
                        (int)ssid_checkbox_5G.getTag() == 1 ? "1" : "0",
                        "0");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getRouterGuestWifi() {
        showLoadingDialog();
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.getWiFiEasyGuestCfg();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.ivLeft, R.id.btnNext, /*R.id.layout_SecurityMethod,*/ R.id.layout_wifi_qr_code})
    public void onViewClicked(View view) {
        switch(view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnNext:
                setRouterGuestWifi();
                break;
            /*case R.id.layout_SecurityMethod:
                showPopMenu();
                break;*/
            case R.id.layout_wifi_qr_code:
                showQrCode();
                break;
        }
    }

    private void showQrCode() {
        try {
            if (jsonObject == null) {
                return;
            }
            RouterSetWifiManagerActivity.toRouterSetWifiManagerActivity(this, "activity",
                    (int)switchSSID.getTag(), (int)switchSSID_2.getTag(), (int)switchSSID_5.getTag(),
                    jsonObject.getString("ssid"), jsonObject.getString("key"),
                    jsonObject.getString("ssid5g"), jsonObject.getString("key5g"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showPopMenu() {
        if (mPopMenus != null) {
            mPopMenus.dismiss();
        }

        mPopMenus = new RouterGuestWifiPopupWindows(this, new RouterGuestWifiPopupWindows.OnClickOpenWifiListener() {
            @Override
            public void onRouterGuestWifiPopupClick(int position) {

            }
        });
        mPopMenus.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mPopMenus = null;
            }
        });

        mPopMenus.showAtLocation(this.findViewById(R.id.layout_guest),
                /*Gravity.TOP | */Gravity.BOTTOM, 0, 0);
    }

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                loadViewInfo();
            } else {
                EventBus.getDefault().post(new RouterOnLineStateEvent());
            }
        }
    };

    @Override
    public void routerReturnInfo(String info, String topicurlString) {
        hideLoadingDialog();
        try {
            if ("getWiFiEasyGuestCfg".equals(topicurlString) && !"error".equals(info)) {
                    jsonObject = new JSONObject(info);
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
            } else if ("setWiFiEasyGuestCfg".equals(topicurlString) && !"error".equals(info)) {
                finish();
            } else {
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
