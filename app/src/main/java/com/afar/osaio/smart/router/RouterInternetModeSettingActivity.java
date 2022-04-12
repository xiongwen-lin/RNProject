package com.afar.osaio.smart.router;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.event.RouterOnLineStateEvent;
import com.afar.osaio.smart.routerlocal.internet.InternetConnectionStatus;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.InputFrameView;
import com.nooie.common.utils.configure.FontUtil;
import com.nooie.common.utils.log.NooieLog;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 路由器 model 设置 Inter
 */
public class RouterInternetModeSettingActivity extends RouterBaseActivity implements SendHttpRequest.getRouterReturnInfo {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.selectMode)
    Spinner selectMode;
    @BindView(R.id.select_address_mode)
    Spinner select_address_mode;
    @BindView(R.id.address_mode)
    LinearLayout address_mode;
    @BindView(R.id.static_type)
    View static_type;
    @BindView(R.id.pppoe_type)
    View pppoe_type;
    @BindView(R.id.pptp_type)
    View pptp_type;
    @BindView(R.id.tvText)
    TextView tvText;

    @BindView(R.id.layout_pptp_ip_address)
    LinearLayout layout_pptp_ip_address;
    @BindView(R.id.layout_pptp_subnet_mask)
    LinearLayout layout_pptp_subnet_mask;
    @BindView(R.id.layout_pptp_gateway)
    LinearLayout layout_pptp_gateway;
    @BindView(R.id.layout_pptp_primary_dns)
    LinearLayout layout_pptp_primary_dns;
    @BindView(R.id.layout_pptp_secondary_dns)
    LinearLayout layout_pptp_secondary_dns;

    @BindView(R.id.static_ip)
    InputFrameView static_ip;
    @BindView(R.id.static_mask)
    InputFrameView static_mask;
    @BindView(R.id.static_gateway)
    InputFrameView static_gateway;
    @BindView(R.id.static_primary_dns)
    InputFrameView static_primary_dns;
    @BindView(R.id.static_secondary_dns)
    InputFrameView static_secondary_dns;

    @BindView(R.id.pppoe_user_name)
    InputFrameView pppoe_user_name;
    @BindView(R.id.pppoe_password)
    InputFrameView pppoe_password;

    @BindView(R.id.pptp_ip)
    InputFrameView pptp_ip;
    @BindView(R.id.pptp_mask)
    InputFrameView pptp_mask;
    @BindView(R.id.pptp_gateway)
    InputFrameView pptp_gateway;
    @BindView(R.id.pptp_server_address)
    InputFrameView pptp_server_address;
    @BindView(R.id.pptp_primary_dns)
    InputFrameView pptp_primary_dns;
    @BindView(R.id.pptp_secondary_dns)
    InputFrameView pptp_secondary_dns;
    @BindView(R.id.pptp_user_name)
    InputFrameView pptp_user_name;
    @BindView(R.id.pptp_password)
    InputFrameView pptp_password;

    @BindView(R.id.MPPE)
    CheckBox MPPE;
    @BindView(R.id.MPPC)
    CheckBox MPPC;

    @BindView(R.id.btnNext)
    FButton btnNext;

    private static final int INTERNET_MODE_TYPE = 0;
    private static final int ADDRESS_TYPE = 1;
    private String setting = "";
    private JSONObject getOpModeJson;
    private String autoDetect = "";
    private String wanMode = "";

    public static void toRouterInternetModeSettingActivity(Context from) {
        Intent intent = new Intent(from, RouterInternetModeSettingActivity.class);
        from.startActivity(intent);
    }

    public static void toRouterInternetModeSettingActivity(Context from, String network) {
        Intent intent = new Intent(from, RouterInternetModeSettingActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_SETTING, network);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_mode);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_internet_setting_title);
        selectMode.setTag(0);
        MPPE.setVisibility(View.INVISIBLE);
        MPPC.setVisibility(View.INVISIBLE);

        selectMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                //String[] languages = getResources().getStringArray(R.array.internetmode);
                selectMode.setTag(pos);
                checkBtnEnable();
                setViewForInternetModeType(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        select_address_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                //String[] languages = getResources().getStringArray(R.array.internetmode);
                setViewForAddressType(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        initFrameView();
        checkBtnEnable();

        pppoe_password.setPPOEPasswordTextAlign();
    }

    private void initFrameView() {
        static_ip.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setTextSSIDWatcher()
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setHintTexe("0.0.0.0")
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.gray_cc616161)
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

        static_mask.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setTextSSIDWatcher()
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setHintTexe("0.0.0.0")
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.gray_cc616161)
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

        static_gateway.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setTextSSIDWatcher()
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setHintTexe("0.0.0.0")
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.gray_cc616161)
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

        static_primary_dns.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setTextSSIDWatcher()
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setHintTexe("0.0.0.0")
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.gray_cc616161)
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

        static_secondary_dns.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setTextSSIDWatcher()
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setHintTexe("0.0.0.0")
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.gray_cc616161)
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

        pppoe_user_name.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setTextSSIDWatcher()
                .setEtInputMaxLength(32)
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setHintTexe("")
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.gray_cc616161)
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

        pppoe_password.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setTextSSIDWatcher()
                .setEtInputMaxLength(32)
                .setEtInputToggle(true)
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

        pptp_ip.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setTextSSIDWatcher()
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setHintTexe("0.0.0.0")
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.gray_cc616161)
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

        pptp_mask.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setTextSSIDWatcher()
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setHintTexe("0.0.0.0")
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.gray_cc616161)
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

        pptp_gateway.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setTextSSIDWatcher()
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setHintTexe("0.0.0.0")
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.gray_cc616161)
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

        pptp_server_address.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setTextSSIDWatcher()
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setHintTexe("0.0.0.0")
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.gray_cc616161)
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
                        checkBtnEnable();
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

        pptp_user_name.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setTextSSIDWatcher()
                .setEtInputMaxLength(32)
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setHintTexe("")
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.gray_cc616161)
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

        pptp_password.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setTextSSIDWatcher()
                .setEtInputMaxLength(32)
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setHintTexe("")
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.gray_cc616161)
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

        pptp_primary_dns.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setTextSSIDWatcher()
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setHintTexe("0.0.0.0")
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.gray_cc616161)
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

        pptp_secondary_dns.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setTextSSIDWatcher()
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setHintTexe("0.0.0.0")
                .setInputBtnIsShow(true)
                .setTextInputBtnColor(R.color.gray_cc616161)
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

        MPPE.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });

        MPPC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        //tvText
        String url2 = getString(R.string.router_internet_setting_jump);
        String url1 = String.format(getString(R.string.router_internet_setting_normal),url2);

        SpannableStringBuilder style = new SpannableStringBuilder(url1);
        ClickableSpan myURLSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                if (setting != null && "network".equals(setting)) {
                    finish();
                } else {
                    RouterResetNameActivity.toRouterResetNameActivity(RouterInternetModeSettingActivity.this);
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setTypeface(FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope-semibold.otf"));
            }
        };
        style.setSpan(myURLSpan, url1.indexOf(url2), url1.indexOf(url2)+url2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        style.setSpan(new StyleSpan(Typeface.NORMAL), url1.indexOf(url2), url1.indexOf(url2)+url2.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        //style.setSpan(new UnderlineSpan(), url1.indexOf(url2), url1.indexOf(url2)+url2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.theme_green)), url1.indexOf(url2), url1.indexOf(url2)+url2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //style.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.colorAccent)), url1.length(),url.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvText.setText(style);

        //设置超链接为可点击状态
        tvText.setMovementMethod(LinkMovementMethod.getInstance());

        setting = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_SETTING);
        if (setting != null && "network".equals(setting)) {
            getWizardCfg();
            btnNext.setText(getString(R.string.save));
        } else if (setting != null && "backUp".equals(setting)) {
            setBackUpView();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAvailableShowView(InternetConnectionStatus.isNetSystemUsable());
    }

    private void isAvailableShowView(boolean isShow) {
        NooieLog.d("result = showView " + isShow);
        tvText.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    private void setBackUpView() {
        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        List<String> internetModeInfo = prefs.getRouterInternetMode();
        if ("0".equals(internetModeInfo.get(0))) {
            // 静态
            selectMode.setSelection(1);
            selectMode.setTag(1);
            setViewForInternetModeType(1);
            static_ip.setEtInputText(internetModeInfo.get(1));
            static_mask.setEtInputText(internetModeInfo.get(2));
            static_gateway.setEtInputText(internetModeInfo.get(3));
            static_primary_dns.setEtInputText(internetModeInfo.get(4));
            static_secondary_dns.setEtInputText(internetModeInfo.get(5));
        } else if ("3".equals(internetModeInfo.get(0))) {
            // pppoe
            selectMode.setSelection(2);
            selectMode.setTag(2);
            setViewForInternetModeType(2);
            pppoe_user_name.setEtInputText(internetModeInfo.get(1));
            pppoe_password.setEtInputText(internetModeInfo.get(2));
        }
    }

    private void setViewIsShow(int internetType, boolean isShow) {
        if (internetType == INTERNET_MODE_TYPE && !isShow) {
            address_mode.setVisibility(View.GONE);
            static_type.setVisibility(View.GONE);
            pppoe_type.setVisibility(View.GONE);
            pptp_type.setVisibility(View.GONE);
        } else if (internetType == ADDRESS_TYPE) {
            layout_pptp_ip_address.setVisibility(isShow ? View.VISIBLE : View.GONE);
            layout_pptp_subnet_mask.setVisibility(isShow ? View.VISIBLE : View.GONE);
            layout_pptp_gateway.setVisibility(isShow ? View.VISIBLE : View.GONE);
            layout_pptp_primary_dns.setVisibility(isShow ? View.VISIBLE : View.GONE);
            layout_pptp_secondary_dns.setVisibility(isShow ? View.VISIBLE : View.GONE);
        }
        MPPE.setVisibility(View.VISIBLE);
        MPPC.setVisibility(View.VISIBLE);
    }

    private void setViewForInternetModeType(int pos) {
        setViewIsShow(INTERNET_MODE_TYPE, false);
        switch (pos) {
            case 0:
                break;
            case 1:
                static_type.setVisibility(View.VISIBLE);
                break;
            case 2:
                pppoe_type.setVisibility(View.VISIBLE);
                break;
            case 3:
                address_mode.setVisibility(View.VISIBLE);
                pptp_type.setVisibility(View.VISIBLE);
                break;
            case 4:
                address_mode.setVisibility(View.VISIBLE);
                pptp_type.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setViewForAddressType(int pos) {
        setViewIsShow(ADDRESS_TYPE, false);
        switch (pos) {
            case 0:
                MPPE.setVisibility(View.INVISIBLE);
                MPPC.setVisibility(View.INVISIBLE);
                break;
            case 1:
                setViewIsShow(ADDRESS_TYPE, true);
                break;
        }
    }

    private void checkBtnEnable() {
        if ((int) selectMode.getTag() == 0) {
            btnNext.setEnabled(true);
        } else if ((int) selectMode.getTag() == 1) {
            if (static_ip.getInputText().length() > 0 && static_mask.getInputText().length() > 0
                    && static_gateway.getInputText().length() > 0 && static_primary_dns.getInputText().length() > 0
                    && checFinalIp(static_primary_dns.getInputText())
            ) {
                btnNext.setEnabled(true);
                btnNext.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
            } else {
                btnNext.setEnabled(false);
                btnNext.setTextColor(getResources().getColor(R.color.unable_clickable_color));
            }
        } else if ((int) selectMode.getTag() == 2) {
            if (pppoe_user_name.getInputText().length() > 0 && pppoe_password.getInputText().length() > 0) {
                btnNext.setEnabled(true);
                btnNext.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
            } else {
                btnNext.setEnabled(false);
                btnNext.setTextColor(getResources().getColor(R.color.unable_clickable_color));
            }
        }
    }

    private void autoParaForMode(String posString) throws JSONException {
        if ("0".equals(posString)) {
            static_ip.setEtInputText(getOpModeJson.getString("staticIp"));
            static_mask.setEtInputText(getOpModeJson.getString("staticMask"));
            static_gateway.setEtInputText(getOpModeJson.getString("staticGw"));
            static_primary_dns.setEtInputText(getOpModeJson.getString("priDns"));
            if (getOpModeJson.has("secDns")) {
                static_secondary_dns.setEtInputText(getOpModeJson.getString("secDns"));
            }

        } else if ("3".equals(posString)) {
            pppoe_user_name.setEtInputText(getOpModeJson.getString("pppoeUser"));
            pppoe_password.setEtInputText(getOpModeJson.getString("pppoePass"));
        }
    }

    private void setNetMode(int mode) throws JSONException {
        if (mode == 2) {
            autoParaForMode(autoDetect);
        }
        switch (autoDetect) {
            case "-1":
                selectMode.setSelection(0);
                selectMode.setTag(0);
                setViewForInternetModeType(0);
                break;
            case "0":
                selectMode.setSelection(1);
                selectMode.setTag(1);
                setViewForInternetModeType(1);
                break;
            case "1":
                selectMode.setSelection(0);
                selectMode.setTag(0);
                setViewForInternetModeType(0);
                break;
            case "3":
                selectMode.setSelection(2);
                selectMode.setTag(2);
                setViewForInternetModeType(2);
                break;
        }
        checkBtnEnable();
    }


    @OnClick({R.id.ivLeft, R.id.btnNext, R.id.auto_detect})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnNext:
                checkIPParameter();
                break;
            case R.id.auto_detect:
                discoverWan();
                break;
        }
    }

    private void routerInternetMode() {
        int internetMode = (int) selectMode.getTag();
        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        switch (internetMode) {
            case 0:
                prefs.setRouterInternetMode("1", "", "", "", "", "", "", "");
                break;
            case 1:
                prefs.setRouterInternetMode("0", static_ip.getInputTextNoTrim(), static_mask.getInputTextNoTrim(),
                        static_gateway.getInputTextNoTrim(), static_primary_dns.getInputTextNoTrim(), static_secondary_dns.getInputTextNoTrim(),
                        "", "");
                break;
            case 2:
                prefs.setRouterInternetMode("3", "", "", "", "", "", pppoe_user_name.getInputTextNoTrim(), pppoe_password.getInputTextNoTrim());
                break;
            case 3:
                break;
            case 4:
                break;
        }
        RouterResetNameActivity.toRouterResetNameActivity(RouterInternetModeSettingActivity.this);
    }

    private void remindInfo(int selectType, int position) {
        String ip = "0.0.0.0";
        String[][] remindInfoList = new String[][]{
                {
                        getString(R.string.router_internet_setting_ip_error),
                        getString(R.string.router_internet_setting_subnet_mask_error),
                        getString(R.string.router_internet_setting_gateway_error),
                        getString(R.string.router_internet_setting_primary_dns_error),
                        getString(R.string.router_internet_setting_secondary_dns_error)
                },
                {
                        getString(R.string.router_internet_setting_pptip_ip_error),
                        getString(R.string.router_internet_setting_pptp_mask_error),
                        getString(R.string.router_internet_setting_pptp_gateway_error),
                        getString(R.string.router_internet_setting_primary_dns_error),
                        getString(R.string.router_internet_setting_secondary_dns_error)

                }
        };
        ToastUtil.showToast(this, remindInfoList[selectType][position]);
        String text = String.format(getString(R.string.router_internet_setting_error), remindInfoList[selectType][position], ip);
        ToastUtil.showToast(this, text);
    }

    private void checkIPParameter() {
        List<String> listString = new ArrayList<>();
        if (listString.size() > 0) {
            listString.clear();
        }
        if (static_type.getVisibility() == View.VISIBLE) {
            listString.add(static_ip.getInputTextNoTrim());
            listString.add(static_mask.getInputTextNoTrim());
            listString.add(static_gateway.getInputTextNoTrim());
            listString.add(static_primary_dns.getInputTextNoTrim());
            listString.add(static_secondary_dns.getInputTextNoTrim());
            for (int i = 0; i < listString.size() - 1; i++) {
                if (!checkIP(listString.get(i))) {
                    remindInfo(0, i);
                    return;
                }
            }
        } else if (pptp_type.getVisibility() == View.VISIBLE) {
            listString.add(pptp_server_address.getInputTextNoTrim());
            if (address_mode.getVisibility() == View.VISIBLE && layout_pptp_ip_address.getVisibility() == View.VISIBLE) {
                listString.add(pptp_ip.getInputTextNoTrim());
                listString.add(pptp_mask.getInputTextNoTrim());
                listString.add(pptp_gateway.getInputTextNoTrim());
                listString.add(pptp_primary_dns.getInputTextNoTrim());
                listString.add(pptp_secondary_dns.getInputTextNoTrim());
            }
            for (int i = 0; i < listString.size() - 1; i++) {
                if (!checkIP(listString.get(i))) {
                    remindInfo(1, i);
                    return;
                }
            }
            String user_name = pptp_user_name.getInputTextNoTrim();
            String user_password = pptp_password.getInputTextNoTrim();
            if (user_name.length() <= 0 || user_password.length() <= 0) {
                return;
            }
        } else if (pppoe_type.getVisibility() == View.VISIBLE) {
            String user_name = pppoe_user_name.getInputTextNoTrim();
            String user_password = pppoe_password.getInputTextNoTrim();
            if (user_name.length() <= 0 || user_password.length() <= 0) {
                return;
            }
        }

        if (setting != null && "network".equals(setting)) {
            /*if ((int) selectMode.getTag() != 0) {
                setRouterWizardCfg();
            } else {
                finish();
            }*/
            setRouterWizardCfg();
        } else {
            routerInternetMode(); // 设置网络模式相关配置
        }
    }

    // 判断输入的IP是否合法
    private boolean checkIP(String str) {
        Pattern pattern = Pattern
                .compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]"
                        + "|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
        return pattern.matcher(str).matches();
    }

    private String getRouterAdminPassword() {
        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        return prefs.getRouterAdminPassword();
    }

    private void setRouterWizardCfg() {
        if (getOpModeJson == null) {
            return;
        }
        showLoadingDialog();
        String[] netInfo = new String[]{"", "", "", "", "", "", ""};
        if ((int) selectMode.getTag() == 1) {
            netInfo[0] = static_ip.getInputText();
            netInfo[1] = static_mask.getInputText();
            netInfo[2] = static_gateway.getInputText();
            netInfo[3] = static_primary_dns.getInputText();
            netInfo[4] = static_secondary_dns.getInputText();
        } else if ((int) selectMode.getTag() == 2) {
            netInfo[5] = pppoe_user_name.getInputText();
            netInfo[6] = pppoe_password.getInputText();
        }

        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.setWizardCfg("UTC" + timeZoneConversion(),
                    (int) selectMode.getTag() == 0 ? "1" : ((int) selectMode.getTag() == 1 ? "0" : "3"),
                    (int) selectMode.getTag() == 1 ? "1" : "0", getOpModeJson.getString("merge"),
                    getOpModeJson.getString("wifiOff"), getOpModeJson.getString("hssid"),
                    getOpModeJson.getString("wpaMode"), getOpModeJson.getString("ssid"),
                    getOpModeJson.getString("key"),
                    getOpModeJson.getString("wifiOff5g"), getOpModeJson.getString("hssid5g"),
                    getOpModeJson.getString("wpaMode5g"), getOpModeJson.getString("ssid5g"),
                    getOpModeJson.getString("key5g"), getRouterAdminPassword(), "0",
                    netInfo[0], netInfo[1], netInfo[2], netInfo[3], netInfo[4], netInfo[5], netInfo[6]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取路由器模式
     */
    private void getWizardCfg() {
        showLoadingDialog();
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.getWizardCfg();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void discoverWan() {
        showLoadingDialog();
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.discoverWan();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try {
                if (msg.what == 2 || msg.what == 1) {
                    setNetMode(msg.what);
                } else {
                    EventBus.getDefault().post(new RouterOnLineStateEvent());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void routerReturnInfo(String info, String topicurlString) {
        hideLoadingDialog();
        try {
            Message message = new Message();
            if (!"error".equals(info) && "discoverWan".equals(topicurlString)) {
                getOpModeJson = new JSONObject(info);
                autoDetect = getOpModeJson.getString("discoverProto");
                message.what = 1;
                handler.sendMessage(message);
            } else if (!"error".equals(info) && "getWizardCfg".equals(topicurlString)) {
                getOpModeJson = new JSONObject(info);
                autoDetect = getOpModeJson.getString("proto");
                message.what = 2;
                handler.sendMessage(message);
            } else if (!"error".equals(info) && "setWizardCfg".equals(topicurlString)) {
                finish();
            } else {
                message.what = 0;
                handler.sendMessage(message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*private void selectMode(){
        if (TextUtils.isEmpty("")){
            return;
        }
        wanMode = jsonObject.getString("wanMode");

    }*/

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

    /**
     * 校验IP 地址
     */
    private boolean checFinalIp(String ip) {
        if (TextUtils.isEmpty(ip)) {
            return false;
        }

        if (!ip.contains(".")) {
            return false;
        }

        String[] ipArray = ip.split("\\.");
        if (ipArray.length == 4) {
            return true;
        }

        return false;

    }
}
