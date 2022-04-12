package com.afar.osaio.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.widget.base.BaseBottomSheetDialog;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class NormalTextAndIconInputDialog extends BaseBottomSheetDialog {

    private static final int DEFAULT_INPUT_TEXT_MAX_LEN = 20;

    FButton btnPresetPointCancel;
    FButton btnPresetPointConfirm;
    ImageView ivPresetPointEditIcon;
    InputFrameView ipvPresetPointEdit;
    private String mIconPath;

    public NormalTextAndIconInputDialog(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setContentView(R.layout.layout_preset_point_edit);
        setContainerTransparentBackground();
        ivPresetPointEditIcon = findViewById(R.id.ivPresetPointEditIcon);
        ipvPresetPointEdit = findViewById(R.id.ipvPresetPointEdit);
        btnPresetPointCancel = findViewById(R.id.btnPresetPointCancel);
        btnPresetPointConfirm = findViewById(R.id.btnPresetPointConfirm);
        setupView();
    }

    private void setupView() {
        if (ipvPresetPointEdit != null) {
            ipvPresetPointEdit.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                    .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                    .setTextInputBtn(getTextLengthTip(0, DEFAULT_INPUT_TEXT_MAX_LEN))
                    .setTextInputBtnColor(R.color.gray_cc616161)
                    .setEtInputMaxLength(DEFAULT_INPUT_TEXT_MAX_LEN)
                    .setInputBtnIsShow(true);
        }
        setInputFrameTextChangeListener(null);
    }

    @Override
    public void dismiss() {
        hideInputMethod(ipvPresetPointEdit);
        super.dismiss();
    }

    public void setPresetPointEditIcon(String path) {
        mIconPath = path;
        if (TextUtils.isEmpty(path)) {
            return;
        }
        if (ivPresetPointEditIcon != null) {
            Glide.with(NooieApplication.mCtx)
                    .load(path)
                    .apply(new RequestOptions()
                                    //.dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 10))))
                                    .placeholder(R.drawable.default_preview)
                                    .format(DecodeFormat.PREFER_RGB_565).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                    )
                    .transition(withCrossFade())
                    .into(ivPresetPointEditIcon);
        }
    }

    public void setButtonListener(View.OnClickListener listener) {
        if (btnPresetPointCancel != null) {
            btnPresetPointCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if (listener != null) {
                        listener.onClick(v);
                    }
                }
            });
        }
        if (btnPresetPointConfirm != null) {
            btnPresetPointConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //dismiss();
                    if (listener != null) {
                        listener.onClick(v);
                    }
                }
            });
        }
    }

    public void setInputFrameOnClickListener(InputFrameView.OnInputFrameClickListener listener) {
        if (ipvPresetPointEdit != null) {
            ipvPresetPointEdit.setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                @Override
                public void onInputBtnClick() {
                    if (listener != null) {
                        listener.onInputBtnClick();
                    }
                }

                @Override
                public void onEditorAction() {
                    //dismiss();
                    if (listener != null) {
                        listener.onEditorAction();
                    }
                }

                @Override
                public void onEtInputClick() {
                    if (listener != null) {
                        listener.onEtInputClick();
                    }
                }
            });
        }
    }

    public void setInputFrameTextChangeListener(TextWatcher listener) {
        if (ipvPresetPointEdit != null) {
            ipvPresetPointEdit.setInputTextChangeListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if (listener != null) {
                        listener.beforeTextChanged(s, start, count, after);
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (listener != null) {
                        listener.onTextChanged(s, start, before, count);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int currentLength = s != null ? s.length() : 0;
                    updateInputFrameTextLengthTip(currentLength);
                    if (listener != null) {
                        listener.afterTextChanged(s);
                    }
                }
            });
        }
    }

    public void updateInputFrameTextLengthTip(int currentLength) {
        if (ipvPresetPointEdit != null) {
            ipvPresetPointEdit.setTextInputBtn(getTextLengthTip(currentLength, DEFAULT_INPUT_TEXT_MAX_LEN));
        }
    }

    public void setInputFrameHint(String text) {
        if (ipvPresetPointEdit != null) {
            ipvPresetPointEdit.setEtInputHint(text);
        }
    }

    public String getInputText() {
        return ipvPresetPointEdit != null ? ipvPresetPointEdit.getInputText() : new String();
    }

    public String getIconPath() {
        return mIconPath;
    }

    private String getTextLengthTip(int currentLength, int maxLength) {
        if (currentLength < 0 || maxLength < 1) {
            return new String();
        }
        if (currentLength > maxLength) {
            return new StringBuilder().append(maxLength).append("/").append(maxLength).toString();
        }
        return new StringBuilder().append(currentLength).append("/").append(maxLength).toString();
    }

    protected void hideInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void checkButtonEnable(boolean enable) {
        if (btnPresetPointConfirm == null) {
            return;
        }
        btnPresetPointConfirm.setEnabled(enable);
    }
}
