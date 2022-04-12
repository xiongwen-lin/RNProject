package com.afar.osaio.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.nooie.common.utils.log.NooieLog;

/**
 * InputFrameView
 *
 * @author Administrator
 * @date 2019/3/14
 */
public class InputFrameView extends LinearLayout {

    public static final int INPUT_THEME_DEFAULT = 0;
    public static final int INPUT_THEME_1 = 1;
    public static final int INPUT_THEME_2 = 2;
    public static final int INPUT_BTN_TYPE_ICON = 1;
    public static final int INPUT_BTN_TYPE_TEXT = 2;

    private int INPUT_THEME_2_FLAG = 0;

    TextView tvInputTitle;
    AutoCompleteTextView etInput;
    ImageView ivInputBtn;
    TextView tvInputBtn;
    TextView tvWrong;

    private int mThemeType;

    OnInputFrameClickListener mListener;
    private TextWatcher mTextWatcher;
    private OnFocusChangeListener mEtInputFocusChangeListener;

    private boolean mIsOpenInputToggle = false;
    private boolean mIsShowBtn = true;
    private int mInputBtnType = INPUT_BTN_TYPE_ICON;

    private int titleColor;

    public InputFrameView(Context context) {
        this(context, null);
    }

    public InputFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.InputFrameView, 0, 0);
        mThemeType = a.getInt(R.styleable.InputFrameView_input_theme, INPUT_THEME_DEFAULT);
        initAttr();
        init(context);
    }

    private void init(Context context) {
        titleColor = R.color.theme_white;
        View view = LayoutInflater.from(context).inflate(getLayoutId(), this, false);
        tvInputTitle = (TextView) view.findViewById(R.id.tvInputTitle);
        etInput = (AutoCompleteTextView) view.findViewById(R.id.etInput);
        ivInputBtn = (ImageView) view.findViewById(R.id.btnInput);
        tvInputBtn = (TextView) view.findViewById(R.id.btnTxtInput);
        tvWrong = (TextView) view.findViewById(R.id.tvWrong);

        ivInputBtn.setImageResource(mEyeCloseResId);
        tvInputBtn.setText("");
        displayInputBtn();
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (mTextWatcher != null) {
                    mTextWatcher.beforeTextChanged(s, start, count, after);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                displayInputBtn();

                if (mTextWatcher != null) {
                    mTextWatcher.onTextChanged(s, start, before, count);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mTextWatcher != null) {
                    mTextWatcher.afterTextChanged(s);
                }
            }
        });

        etInput.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    tvInputTitle.setTextColor(getResources().getColor(titleColor));
                } else {
                    tvInputTitle.setTextColor(getResources().getColor(titleColor));
                }
                if (mEtInputFocusChangeListener != null) {
                    mEtInputFocusChangeListener.onFocusChange(view, hasFocus);
                }
            }
        });

        ivInputBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePwInputBtn();
                if (mListener != null) {
                    mListener.onInputBtnClick();
                }
            }
        });

        tvInputBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onInputBtnClick();
                }
            }
        });

        etInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_GO) {
                    if (mListener != null) {
                        mListener.onEditorAction();
                    }
                    return true;
                }
                return false;
            }
        });

        etInput.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onEtInputClick();
                }
            }
        });
        //etInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(ConstantValue.MAX_NAME_LENGTH)});

        addView(view);
    }

    private int mEyeOpenResId;
    private int mEyeCloseResId;

    public void initAttr() {
        switch (mThemeType) {
            case INPUT_THEME_1:
                mEyeOpenResId = R.drawable.eye_open_icon_state_list;
                mEyeCloseResId = R.drawable.eye_close_icon_state_list;
                break;
            case INPUT_THEME_2:
                mEyeOpenResId = R.drawable.eye_open_icon_state_list;
                mEyeCloseResId = R.drawable.eye_close_icon_state_list;
                break;
            default:
                mEyeOpenResId = R.drawable.eye_open_icon_white_state_list;
                mEyeCloseResId = R.drawable.eye_close_icon_white_state_list;
                break;
        }
    }

    public int getLayoutId() {
        //int layoutId = R.layout.layout_input_frame;
        int layoutId = R.layout.osaio_layout_input_frame;
        switch (mThemeType) {
            case INPUT_THEME_1:
                //layoutId = R.layout.layout_input_frame_1;
                layoutId = R.layout.osaio_layout_input_frame_1;
                break;
            case INPUT_THEME_2:
                layoutId = R.layout.layout_input_frame2;
                //layoutId = R.layout.osaio_layout_input_frame2;
                break;
        }

        return layoutId;
    }

    public InputFrameView setHintTexe(String text) {
        etInput.setHint(text);
        return this;
    }

    public InputFrameView setBtnInputView(boolean isShow) {
        if (isShow) {
            INPUT_THEME_2_FLAG = INPUT_THEME_2;
            etInput.setGravity(Gravity.START);
            ivInputBtn.setVisibility(VISIBLE);
            etInput.setInputType(getPwInputType(etPwInputType, false));
        }
        return this;
    }

    public InputFrameView setPPOEPasswordTextAlign() {
        etInput.setGravity(Gravity.END);
        return this;
    }

    public AutoCompleteTextView getEtInput() {
        return etInput;
    }

    public String getInputText() {
        return etInput != null ? etInput.getText().toString().trim() : "";
    }

    public String getInputTextNoTrim() {
        return etInput != null ? etInput.getText().toString() : "";
    }

    public InputFrameView setInputTitle(String title) {
        if (tvInputTitle != null) {
            tvInputTitle.setText(title);
        }
        return this;
    }

    public InputFrameView setInputTitleVisible(int visible) {
        if (tvInputTitle != null) {
            tvInputTitle.setVisibility(visible);
        }
        return this;
    }

    public InputFrameView setWrongText(String wrongText) {
        if (tvWrong != null) {
            tvWrong.setText(wrongText);
        }
        return this;
    }

    public InputFrameView setWrongTextVisible(int visible) {
        if (tvWrong != null) {
            tvWrong.setVisibility(visible);
        }
        return this;
    }

    public void setEtInputBackground(int background) {
        if (etInput != null) {
            etInput.setBackgroundResource(background);
        }
    }

    public void setEtInputText(String text) {
        if (etInput != null) {
            etInput.setText(text);
        }
    }

    public void setEtSelection(int position) {
        if (etInput != null) {
            etInput.setSelection(position);
        }
    }

    public InputFrameView setEtInputHint(String hint) {
        if (etInput != null) {
            etInput.setHint(hint);
        }
        return this;
    }

    private int etPwInputType = InputType.TYPE_CLASS_TEXT;

    public InputFrameView setEtPwInputType(int type) {
        etPwInputType = type;
        return this;
    }

    public InputFrameView setEtInputType(int type) {
        if (etInput != null) {
            etInput.setInputType(type);
            if (isPasswordInputType(type)) {
                ivInputBtn.setImageResource(mEyeCloseResId);
            } else {
                ivInputBtn.setImageResource(mEyeOpenResId);
            }
        }
        return this;
    }

    public InputFrameView setTextAlign(int textAlign) {
        if (etInput != null) {
            etInput.setGravity(textAlign);
        }
        return this;
    }

    private static int dp2px(float dp) {
        Resources r = Resources.getSystem();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public InputFrameView setEtInputGravity(int gravity) {
        if (gravity == Gravity.CENTER) {
            etInput.setPadding(dp2px(30), 0, dp2px(30), dp2px(20));
            ivInputBtn.setPaddingRelative(0, 0, 0, dp2px(20));
        }
        if (etInput != null) {
            etInput.setGravity(gravity);
        }
        return this;
    }

    public InputFrameView setEtInputMaxLength(int maxLength) {
        if (etInput != null) {
            etInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        }
        return this;
    }


    public InputFrameView setInputBtn(int resId) {
        if (ivInputBtn != null) {
            ivInputBtn.setImageResource(resId);
        }
        return this;
    }

    public InputFrameView setTextInputBtn(String text) {
        if (tvInputBtn != null) {
            tvInputBtn.setText(text);
        }
        return this;
    }

    public InputFrameView setTextInputBtnColor(int color) {
        if (tvInputBtn != null) {
            tvInputBtn.setTextColor(getResources().getColor(color));
        }
        return this;
    }

    public InputFrameView setTextInputBtnBg(int resId) {
        if (tvInputBtn != null) {
            tvInputBtn.setBackgroundResource(resId);
        }
        return this;
    }

    public InputFrameView setTextInputBtnBg(Drawable drawable) {
        if (tvInputBtn != null) {
            tvInputBtn.setBackground(drawable);
        }
        return this;
    }

    public InputFrameView setInputBtnType(int type) {
        mInputBtnType = type;
        return this;
    }

    public InputFrameView setInputBtnIsShow(boolean isshow) {
        mIsShowBtn = isshow;
        displayInputBtn();
        return this;
    }

    public InputFrameView setEtInputToggle(boolean open) {
        mIsOpenInputToggle = open;
        return this;
    }

    public InputFrameView setCursorVisilbe(boolean visilbe) {
        if (etInput != null) {
            etInput.setCursorVisible(visilbe);
        }
        return this;
    }

    public InputFrameView setIpvFocusable(boolean focusable) {
        if (etInput != null) {
            etInput.setFocusable(focusable);
            etInput.setFocusableInTouchMode(focusable);
            if (focusable) {
                etInput.requestFocus();
            }
        }
        return this;
    }

    public InputFrameView setIpvEnable(boolean enable) {
        if (etInput != null) {
            etInput.setEnabled(enable);
        }
        return this;
    }

    public InputFrameView setIpvAutoLink(boolean auto) {
        if (etInput != null) {
            etInput.setAutoLinkMask(auto ? Linkify.ALL : 0);
        }
        return this;
    }

    private void togglePwInputBtn() {
        if (mIsOpenInputToggle && etInput != null && ivInputBtn != null) {
            if (!isPasswordInputType(etInput.getInputType())) {
                etInput.setInputType(getPwInputType(etPwInputType, false));
                ivInputBtn.setImageResource(mEyeCloseResId);
            } else {
                etInput.setInputType(getPwInputType(etPwInputType, true));
                ivInputBtn.setImageResource(mEyeOpenResId);
            }
            etInput.setSelection(etInput.getText().toString().trim().length());
        }
    }

    private void displayInputBtn() {
        if (!mIsShowBtn) {
            ivInputBtn.setVisibility(GONE);
            tvInputBtn.setVisibility(GONE);
            return;
        }

        switch (mInputBtnType) {
            case INPUT_BTN_TYPE_ICON:
                if (mIsShowBtn) {
                    tvInputBtn.setVisibility(GONE);
                    if (mThemeType != INPUT_THEME_2) {
                        ivInputBtn.setVisibility(TextUtils.isEmpty(etInput.getText().toString().trim()) ? View.GONE : View.VISIBLE);
                    }
                    if (INPUT_THEME_2_FLAG == INPUT_THEME_2) {
                        ivInputBtn.setVisibility(TextUtils.isEmpty(etInput.getText().toString().trim()) ? View.GONE : View.VISIBLE);
                    }
                }
                break;
            case INPUT_BTN_TYPE_TEXT:
                if (mIsShowBtn) {
                    ivInputBtn.setVisibility(GONE);
                    tvInputBtn.setVisibility(View.VISIBLE);
                }
                break;
            default:
                ivInputBtn.setVisibility(GONE);
                tvInputBtn.setVisibility(GONE);
                break;
        }
    }

    public InputFrameView setInputTextChangeListener(TextWatcher textWatcher) {
        mTextWatcher = textWatcher;
        return this;
    }

    public InputFrameView setOnClickListener(OnInputFrameClickListener listener) {
        mListener = listener;
        return this;
    }

    public InputFrameView setEtInputFocusChangeListener(OnFocusChangeListener listener) {
        mEtInputFocusChangeListener = listener;
        return this;
    }

    public static final int INPUT_FRAME_THEME_TYPE_DEFAULT = 1;
    public static final int INPUT_FRAME_THEME_TYPE_TEXT_CENTER = 2;

    public InputFrameView setTheme(int themeType) {
        setTheme(R.color.theme_text_color, R.color.theme_text_color, themeType);
        return this;
    }

    public InputFrameView setTheme(int themeType, int etInputColor) {
        setTheme(R.color.theme_text_color, etInputColor, themeType);
        return this;
    }

    public void setTheme(int titleColor, int textColor, int themeType) {
        switch (themeType) {
            case INPUT_FRAME_THEME_TYPE_TEXT_CENTER: {
                if (etInput != null) {
                    etInput.setPadding(etInput.getPaddingLeft(), etInput.getPaddingTop(), dp2px(4), etInput.getPaddingBottom());
                }
                break;
            }
            default:
                break;
        }
        this.titleColor = titleColor;
        if (tvInputTitle != null) {
            tvInputTitle.setTextColor(getResources().getColor(this.titleColor));
        }
        if (etInput != null) {
            etInput.getContext().setTheme(getInputThemeStyleByType(themeType));
            etInput.setTextColor(getResources().getColor(textColor));
        }
    }

    public int getInputThemeStyleByType(int themeType) {
        int themeStyle = R.style.VictureAutocomplete;
        switch (themeType) {
            case INPUT_FRAME_THEME_TYPE_TEXT_CENTER:
                themeStyle = R.style.VictureInputFrameTextCenter;
                break;
        }
        return themeStyle;
    }

    public void release() {
        tvInputTitle = null;
        tvWrong = null;
        if (etInput != null) {
            etInput.addTextChangedListener(null);
            etInput.setOnClickListener(null);
            etInput.setOnFocusChangeListener(null);
            etInput.setOnEditorActionListener(null);
            etInput = null;
        }

        if (ivInputBtn != null) {
            ivInputBtn.setOnClickListener(null);
            ivInputBtn = null;
        }

        if (tvInputBtn != null) {
            tvInputBtn.setOnClickListener(null);
            tvInputBtn = null;
        }

        mTextWatcher = null;
        mListener = null;
        removeAllViews();
    }

    public static boolean isPasswordInputType(int inputType) {
        final int variation =
                inputType & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_MASK_VARIATION);
        return variation
                == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)
                || variation
                == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD)
                || variation
                == (EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD);
    }

    public static int getPwInputType(int type, boolean isVisible) {
        if (isVisible) {
            if (type == InputType.TYPE_CLASS_NUMBER) {
                return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
            } else {
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
            }
        } else {
            if (type == InputType.TYPE_CLASS_NUMBER) {
                return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD;
            } else {
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
            }
        }
    }

    // 禁止输入空格
    public InputFrameView setTextWatcher() {
        etInput.addTextChangedListener(textChanged);
        return this;
    }

    // 禁止输入空格
    TextWatcher textChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            // 禁止EditText输入空格
            if (charSequence.toString().contains(" ")) {
                String[] str = charSequence.toString().split(" ");
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < str.length; i++) {
                    sb.append(str[i]);
                }
                etInput.setText(sb.toString());
                etInput.setSelection(start);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    // 禁止输入空格
    public InputFrameView setTextPasswordWatcher() {
        etInput.addTextChangedListener(textPasswordChanged);
        return this;
    }

    // 禁止输入空格
    TextWatcher textPasswordChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            // 禁止EditText输入空格
            int flag = 0;
            String[] str = new String[]{};
            if (charSequence.toString().contains(" ")) {
                str = charSequence.toString().split(" ");
            } else if (charSequence.toString().contains("`")) {
                str = charSequence.toString().split("`");
            } else if (charSequence.toString().contains("\"")) {
                str = charSequence.toString().split("\"");
            } else if (charSequence.toString().contains(",")) {
                str = charSequence.toString().split(",");
            } else if (charSequence.toString().contains("=")) {
                str = charSequence.toString().split("=");
            } else if (charSequence.toString().contains("!")) {
                str = charSequence.toString().split("!");
            } else if (charSequence.toString().contains(";")) {
                str = charSequence.toString().split(";");
            } else if (charSequence.toString().contains("\\")) {
                str = charSequence.toString().split("\\\\");
            } else if (charSequence.toString().contains("-")) {
                str = charSequence.toString().split("-");
            } else if (charSequence.toString().contains("|")) {
                str = charSequence.toString().split("\\|");
            } else {
                flag = -1;
            }

            if (flag == 0) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < str.length; i++) {
                    sb.append(str[i]);
                }
                etInput.setText(sb.toString());
                etInput.setSelection(start);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    // 禁止输入空格
    public InputFrameView setTextSSIDWatcher() {
        etInput.addTextChangedListener(textSSIDChanged);
        return this;
    }

    // 禁止输入空格
    TextWatcher textSSIDChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            // 禁止EditText输入空格
            int flag = 0;
            String[] str = new String[]{};
            // 如果用start = 0，当获取ssid时，会进入判断使得展示出的ssid不包含空格
            if (charSequence.toString().contains(" ") && charSequence.toString().substring(0, 1).equals(" ")/*&& start == 0*/) {
                str = charSequence.toString().split(" ");
            } else if (charSequence.toString().contains("`")) {
                str = charSequence.toString().split("`");
            } else if (charSequence.toString().contains("\"")) {
                str = charSequence.toString().split("\"");
            } else if (charSequence.toString().contains(",")) {
                str = charSequence.toString().split(",");
            } else if (charSequence.toString().contains(":")) {
                str = charSequence.toString().split(":");
            } else if (charSequence.toString().contains("~")) {
                str = charSequence.toString().split("~");
            } else if (charSequence.toString().contains("\\")) {
                str = charSequence.toString().split("\\\\");
            } else if (charSequence.toString().contains("$")) {
                str = charSequence.toString().split("\\$");
            } else if (charSequence.toString().contains("%")) {
                str = charSequence.toString().split("%");
            } else if (charSequence.toString().contains("<")) {
                str = charSequence.toString().split("<");
            } else if (charSequence.toString().contains(">")) {
                str = charSequence.toString().split(">");
            } else if (charSequence.toString().contains("/")) {
                str = charSequence.toString().split("/");
            } else if (charSequence.toString().contains("'")) {
                str = charSequence.toString().split("'");
            } else if (charSequence.toString().contains(";")) {
                str = charSequence.toString().split(";");
            } else {
                flag = -1;
            }

            if (flag == 0) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < str.length; i++) {
                    sb.append(str[i]);
                }
                etInput.setText(sb.toString());
                etInput.setSelection(start);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public interface OnInputFrameClickListener {
        void onInputBtnClick();

        /**
         * remember to call hideInputMethod()
         */
        void onEditorAction();

        void onEtInputClick();
    }
}
