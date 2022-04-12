package com.afar.osaio.smart.electrician.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
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

/**
 * InputFrameView
 *
 * @author Administrator
 * @date 2019/3/14
 */
public class InputFrameView extends LinearLayout {

    TextView tvInputTitle;
    AutoCompleteTextView etInput;
    ImageView ivInputBtn;

    OnInputFrameClickListener mListener;
    private TextWatcher mTextWatcher;

    private boolean mIsOpenInputToggle = false;
    private boolean mIsShowBtn = true;

    public InputFrameView(Context context) {
        this(context, null);
    }

    public InputFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        //View view = LayoutInflater.from(context).inflate(R.layout.teckin_layout_input_frame, this, false);
        View view = LayoutInflater.from(context).inflate(R.layout.teckin_layout_input_frame_1, this, false);
        tvInputTitle = (TextView) view.findViewById(R.id.tvInputTitle);
        etInput = (AutoCompleteTextView) view.findViewById(R.id.etInput);
        ivInputBtn = (ImageView) view.findViewById(R.id.btnInput);

        ivInputBtn.setImageResource(R.drawable.eye_close_icon_state_list);
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (mTextWatcher != null) {
                    mTextWatcher.beforeTextChanged(s, start, count, after);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mIsShowBtn) {
                    ivInputBtn.setVisibility(TextUtils.isEmpty(etInput.getText().toString().trim()) ? View.GONE : View.VISIBLE);
                }

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

        /*etInput.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    tvInputTitle.setTextColor(getResources().getColor(R.color.teckin_text_black));
                } else {
                    tvInputTitle.setTextColor(getResources().getColor(R.color.theme_text_gray));
                }
            }
        });*/

        ivInputBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePwInputBtn();
                if (mListener != null) {
                    mListener.onInputBtnClick();
                }
            }
        });

        etInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_GO) {
//                        hideInputMethod();
//                        onViewClick(btnDone);
                    if (mListener != null) {
                        mListener.onEditorAction();
                    }
                    return true;
                }
                return false;
            }
        });

//        etInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(ConstantValue.MAX_NAME_LENGTH)});

        addView(view);
    }


    public String getInputText() {
        return etInput != null ? etInput.getText().toString().trim() : "";
    }

    public InputFrameView setInputTitle(String title) {
        if (tvInputTitle != null) {
            tvInputTitle.setText(title);
        }
        return this;
    }

    public InputFrameView setInputTitleStyle(Typeface typeface) {
        if (tvInputTitle != null) {
            tvInputTitle.setTypeface(typeface);
        }
        return this;
    }

    public InputFrameView setInputTitleVisible(int visible) {
        if (tvInputTitle != null) {
            tvInputTitle.setVisibility(visible);
        }
        return this;
    }

    public void setEtInputText(String text) {
        if (etInput != null) {

            if (TextUtils.isEmpty(text)) {
                etInput.setText("");
                etInput.setSelection(0);
            } else {
                etInput.setText(text);
                etInput.setSelection(text.length());
            }

        }
    }

    public void setInputMaxLen(int maxLen) {
        if (etInput != null) {
            etInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
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

    public InputFrameView setEtInputType(int type) {
        if (etInput != null) {
            etInput.setInputType(type);

            if (type == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                ivInputBtn.setImageResource(R.drawable.eye_open_icon_state_list);
            } else if (type == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                ivInputBtn.setImageResource(R.drawable.eye_close_icon_state_list);
            }
        }

        return this;
    }

    private static int dp2px(float dp) {
        Resources r = Resources.getSystem();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public InputFrameView setEtInputGravity(int gravity) {
        if (gravity == Gravity.CENTER) {
            etInput.setPadding(dp2px(30), 0, dp2px(30), 0);
            ivInputBtn.setPaddingRelative(0, 0, 0, 0);
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

    public InputFrameView setInputBtnIsShow(boolean isshow) {
        mIsShowBtn = isshow;
        return this;
    }

    public InputFrameView setEtInputToggle(boolean open) {
        mIsOpenInputToggle = open;
        return this;
    }

    public InputFrameView setTextAlign(int textAlign) {
        if (etInput != null) {
            etInput.setTextAlignment(textAlign);
        }
        return this;
    }

    public String getInputTextNoTrim() {
        return etInput != null ? etInput.getText().toString() : "";
    }

    public void release() {
        tvInputTitle = null;
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

        mTextWatcher = null;
        mListener = null;
        removeAllViews();
    }

    private void togglePwInputBtn() {
        if (mIsOpenInputToggle && etInput != null && ivInputBtn != null) {
            if (etInput.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                etInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivInputBtn.setImageResource(R.drawable.eye_close_icon_state_list);
            } else {
                etInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivInputBtn.setImageResource(R.drawable.eye_open_icon_state_list);
            }
            etInput.setSelection(etInput.getText().toString().trim().length());
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

    public interface OnInputFrameClickListener {
        void onInputBtnClick();

        /**
         * remember to call hideInputMethod()
         */
        void onEditorAction();
    }
}
