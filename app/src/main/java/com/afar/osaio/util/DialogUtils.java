package com.afar.osaio.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.widget.FButton;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.log.NooieLog;

import java.lang.ref.WeakReference;

/**
 * Created by victor on 2018/7/4
 * Email is victor.qiao.0604@gmail.com
 */
public class DialogUtils {
    public enum ShowFromType {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM,
    }

    public static final int INPUT_DIALOG_TYPE_TEXT = 1;
    public static final int INPUT_DIALOG_TYPE_EDIT = 2;
    public static final int INPUT_DIALOG_TYPE_TEXT_EDIT = 3;

    public static final int DIALOG_OUT_SIDE_LENGTH = 78;

    public interface OnClickShareDeviceHandleButtonListener {
        void onClickIgnore();

        void onClickReject();

        void onClickAgree();
    }

    public interface OnClickConfirmButtonListener {
        void onClickLeft();

        void onClickRight();
    }

    public interface OnClickInputDialogListener {
        void onClickCancel();

        void onClickSave(String text);
    }

    public interface OnSelectItemListener<T> {
        void onSelectItem(T t);
    }

    public interface OnClickInformationDialogLisenter {
        void onConfirmClick();
    }

    public interface OnClickBleAuthorizationCodeDialogListener {
        void onClickCancel();

        void onClickConfirm(int inputType, String text);
    }

    public static Context getSafeContext(Context context) {
        WeakReference<Context> weakReference = new WeakReference<>(context);
        return weakReference.get() != null ? weakReference.get() : new WeakReference<Context>(context).get();
    }

    public static AlertDialog showLoadingDialog(Context ctx) {
        WeakReference<Context> weakReference = new WeakReference<>(ctx);
        Context context = weakReference.get();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.transparent_background_dialog).setView(view).create();

        ImageView iv = (ImageView) view.findViewById(R.id.ivLoading);
        //iv.setBackgroundResource(R.drawable.loading_anim);
        //WeakReference<AnimationDrawable> animationWeakRf = new WeakReference<AnimationDrawable>((AnimationDrawable) iv.getBackground());
        iv.setImageResource(R.drawable.loading_anim);
        WeakReference<AnimationDrawable> animationWeakRf = new WeakReference<AnimationDrawable>((AnimationDrawable) iv.getDrawable());
        if (animationWeakRf != null && animationWeakRf.get() != null && !animationWeakRf.get().isRunning()) {
            animationWeakRf.get().start();
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (animationWeakRf != null && animationWeakRf.get() != null && animationWeakRf.get().isRunning()) {
                    animationWeakRf.get().stop();
                }
            }
        });

        dialog.show();
        return dialog;
    }

    private static final long DIALOG_SHOW_MAX_TIME = 3 & 1000L;
    public static AlertDialog showLoadingDialog(Context ctx, boolean cancel) {
        WeakReference<Context> weakReference = new WeakReference<>(ctx);
        Context context = weakReference.get();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.transparent_background_dialog).setView(view).create();


        ImageView iv = (ImageView) view.findViewById(R.id.ivLoading);
        //iv.setBackgroundResource(R.drawable.loading_anim);
        //WeakReference<AnimationDrawable> animationWeakRf = new WeakReference<AnimationDrawable>((AnimationDrawable) iv.getBackground());
        iv.setImageResource(R.drawable.loading_anim);
        WeakReference<AnimationDrawable> animationWeakRf = new WeakReference<AnimationDrawable>((AnimationDrawable) iv.getDrawable());
        if (animationWeakRf != null && animationWeakRf.get() != null && !animationWeakRf.get().isRunning()) {
            animationWeakRf.get().start();
        }

        if (!cancel) {
            dialog.setCancelable(cancel);
            /*
            WeakReference<Long> startTimeRef = new WeakReference<>(System.currentTimeMillis());
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInf, int keyCode, KeyEvent event) {
                    NooieLog.d("-->> DialogUtils showLoadingDialog onKey show time=" + (startTimeRef != null ? System.currentTimeMillis() - startTimeRef.get() : System.currentTimeMillis()));
                    if (startTimeRef != null && System.currentTimeMillis() - startTimeRef.get() > DIALOG_SHOW_MAX_TIME) {
                        dialogInf.dismiss();
                    }
                    return false;
                }
            });
            */
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (animationWeakRf != null && animationWeakRf.get() != null && animationWeakRf.get().isRunning()) {
                    animationWeakRf.get().stop();
                }
            }
        });

        dialog.show();
        return dialog;
    }

    public static AlertDialog showConfirmWithSubMsgDialog(Context ctx, int title, int message, int leftBtn, int rightBtn, final OnClickConfirmButtonListener listener) {
        return showConfirmWithSubMsgDialog(ctx, ctx.getString(title), ctx.getString(message), leftBtn, rightBtn, listener);
    }

    public static AlertDialog showConfirmWithSubMsgDialog(Context ctx, String title, int message, int leftBtn, int rightBtn, final OnClickConfirmButtonListener listener) {
        return showConfirmWithSubMsgDialog(ctx, title, ctx.getString(message), leftBtn, rightBtn, listener);
    }

    public static AlertDialog showConfirmWithSubMsgDialog(Context ctx, String title, String message, int leftBtn, int rightBtn, final OnClickConfirmButtonListener listener) {
        return showConfirmWithSubMsgDialog(ctx, title, message, ctx.getString(leftBtn), ctx.getString(rightBtn), listener);
    }

    public static AlertDialog showConfirmWithSubMsgDialog(Context ctx, String title, String message, String leftBtn, String rightBtn, final OnClickConfirmButtonListener listener) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_confirm_with_submessage, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);
        isSingleLine(ctx, message, tvMessage.getTextSize(), DIALOG_OUT_SIDE_LENGTH);
        tvMessage.setGravity(isSingleLine(ctx, message, tvMessage.getTextSize(), DIALOG_OUT_SIDE_LENGTH) ? Gravity.CENTER : Gravity.START);

        Button btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setText(leftBtn);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickLeft();
                dialog.dismiss();
            }
        });

        Button btnOk = view.findViewById(R.id.btnOk);
        btnOk.setText(rightBtn);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickRight();
                dialog.dismiss();
            }
        });

        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();  //screen size
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        //params.width = metrics.widthPixels;
        //params.height = (int) (metrics.heightPixels * 0.8);
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);

        return dialog;
    }

    public static AlertDialog showConfirmWithImageDialog(Context ctx, String title, String message, int imgResId, String leftBtn, String rightBtn, boolean showTitle, final OnClickConfirmButtonListener listener) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_confirm_with_image, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setVisibility(showTitle ? View.VISIBLE : View.GONE);
        tvTitle.setText(title);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);
        ImageView ivImage = view.findViewById(R.id.ivImage);
        isSingleLine(ctx, message, tvMessage.getTextSize(), DIALOG_OUT_SIDE_LENGTH);
        tvMessage.setGravity(isSingleLine(ctx, message, tvMessage.getTextSize(), DIALOG_OUT_SIDE_LENGTH) ? Gravity.CENTER : Gravity.START);
        ivImage.setImageResource(imgResId);

        Button btnCancel = view.findViewById(R.id.btnLeft);
        btnCancel.setText(leftBtn);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClickLeft();
                }
                dialog.dismiss();
            }
        });

        Button btnOk = view.findViewById(R.id.btnRight);
        btnOk.setText(rightBtn);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClickRight();
                }
                dialog.dismiss();
            }
        });

        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();  //screen size
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);

        return dialog;
    }

    public static AlertDialog showForceLogoutDialog(Context ctx, final OnClickConfirmButtonListener listener) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_information, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.kick_out);

        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(R.string.kick_out_info);

        Button btnOK = view.findViewById(R.id.btnOk);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickRight();
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();  //screen size
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        //params.width = metrics.widthPixels;
        //params.height = (int) (metrics.heightPixels * 0.8);
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;

    }

    public static AlertDialog showConfirmWithSubMsgDialog(Context ctx, int title, int message, final OnClickConfirmButtonListener listener) {
        return showConfirmWithSubMsgDialog(ctx, title, message, R.string.cancel, R.string.confirm_upper, listener);
    }

    public static AlertDialog showConfirmDialog(Context ctx, int title, final OnClickConfirmButtonListener listener) {
        return showConfirmDialog(ctx, title, R.string.cancel, R.string.confirm_upper, listener);
    }

    public static AlertDialog showConfirmDialog(Context ctx, int title, int leftBtn, int rightBtn, final OnClickConfirmButtonListener listener) {
        return showConfirmDialog(ctx, NooieApplication.get().getString(title), leftBtn, rightBtn, true, listener);
    }

    public static AlertDialog showConfirmDialog(Context ctx, int title, int leftBtn, int rightBtn, boolean cancel, final OnClickConfirmButtonListener listener) {
        return showConfirmDialog(ctx, NooieApplication.get().getString(title), leftBtn, rightBtn, cancel, listener);
    }

    public static AlertDialog showConfirmDialog(Context ctx, String title, int leftBtn, int rightBtn, boolean cancel, final OnClickConfirmButtonListener listener) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_confirm, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        Button btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setText(leftBtn);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickLeft();
                dialog.dismiss();
            }
        });

        Button btnOK = view.findViewById(R.id.btnOk);
        btnOK.setText(rightBtn);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickRight();
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(cancel);
        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();  //screen size
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        //params.width = metrics.widthPixels;
        //params.height = (int) (metrics.heightPixels * 0.8);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    /**
     *
     * @param context
     * @param text
     * @param textSize
     * @param outSize 水平总边距
     * @return
     */
    public static boolean isSingleLine(Context context, String text, float textSize, int outSize) {
        if (text == null) {
            return true;
        }
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(textSize);
        //NooieLog.d("-->> DialogUtils isSingleLine textlen=" + textPaint.measureText(text) + " w=" + (DisplayUtil.SCREEN_WIDTH_PX - DisplayUtil.dpToPx(context, outSize)));
        return textPaint.measureText(text) < (DisplayUtil.SCREEN_WIDTH_PX - DisplayUtil.dpToPx(context, outSize));
    }

    public static AlertDialog showInformationDialog(Context ctx, String title, String message, boolean isLinkText) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_information, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setAutoLinkMask(isLinkText ? Linkify.ALL : 0);
        tvMessage.setMovementMethod(isLinkText ? LinkMovementMethod.getInstance() : ScrollingMovementMethod.getInstance());
        tvMessage.setText(message);
        tvMessage.setGravity(isSingleLine(ctx, message, tvMessage.getTextSize(), DIALOG_OUT_SIDE_LENGTH) ? Gravity.CENTER : Gravity.START);

        Button btnOK = view.findViewById(R.id.btnOk);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();  //screen size
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        //params.width = metrics.widthPixels;
        //params.height = (int) (metrics.heightPixels * 0.8);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static AlertDialog showInformationNormalDialog(Context ctx, String title, String message, boolean isLinkText, final OnClickInformationDialogLisenter lisenter) {
        return showInformationNormalDialog(ctx, title, message, true, isLinkText, lisenter);
    }

    public static AlertDialog showInformationNormalDialog(Context ctx, String title, String message, boolean cancel, boolean isLinkText, final OnClickInformationDialogLisenter lisenter) {
        return showInformationDialog(ctx, title, message, null, cancel, isLinkText, lisenter);
    }

    public static AlertDialog showInformationDialog(Context ctx, String title, String message, String confirmBtn, boolean cancel, boolean isLinkText, final OnClickInformationDialogLisenter lisenter) {
        Context context = getSafeContext(ctx);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_information, null);
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.transparent_background_dialog).setView(view).create();
        dialog.setCancelable(cancel);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setAutoLinkMask(isLinkText ? Linkify.ALL : 0);
        tvMessage.setMovementMethod(isLinkText ? LinkMovementMethod.getInstance() : ScrollingMovementMethod.getInstance());
        tvMessage.setText(message);
        tvMessage.setGravity(isSingleLine(ctx, message, tvMessage.getTextSize(), DIALOG_OUT_SIDE_LENGTH) ? Gravity.CENTER : Gravity.START);
        //tvMessage.setMovementMethod(LinkMovementMethod.getInstance());

        Button btnOK = view.findViewById(R.id.btnOk);
        if (!TextUtils.isEmpty(confirmBtn)) {
            btnOK.setText(confirmBtn);
        }
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (lisenter != null) {
                    lisenter.onConfirmClick();
                }
            }
        });

        //dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        //set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        //screen size
        Display d = m.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static AlertDialog showShareDeviceHandleDialog(Context ctx, String title, String message, String subMessage, boolean cancel, final OnClickShareDeviceHandleButtonListener listener) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_share_device_handle, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);
        tvMessage.setGravity(isSingleLine(ctx, message, tvMessage.getTextSize(), DIALOG_OUT_SIDE_LENGTH) ? Gravity.CENTER : Gravity.START);

        TextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        tvSubMessage.setText(subMessage);

        view.findViewById(R.id.btnIgnore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickIgnore();
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.btnReject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickReject();
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.btnAgree).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickAgree();
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(cancel);
        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();  //screen size
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        //params.width = metrics.widthPixels;
        //params.height = (int) (metrics.heightPixels * 0.8);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static AlertDialog showOneInputDialog(final Activity activity, String title, String hint, String content, String leftBtnTxt, String rightBtnTxt, final OnClickInputDialogListener listener) {
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_one_input, null);
        final AlertDialog dialog = new AlertDialog.Builder(activity, R.style.transparent_background_dialog).setView(view).create();


        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        final ImageView ivClearName = view.findViewById(R.id.ivClearName);

        ivClearName.setVisibility(View.GONE);
        final AutoCompleteTextView etInput = view.findViewById(R.id.etInput);
        etInput.setHint(hint);
        etInput.setText(content);
        ivClearName.setVisibility(TextUtils.isEmpty(etInput.getText().toString()) ? View.GONE : View.VISIBLE);
        setInputBg(etInput);
        etInput.setSelection(content == null ? 0 : content.length());

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ivClearName.setVisibility(TextUtils.isEmpty(etInput.getText().toString()) ? View.GONE : View.VISIBLE);
                setInputBg(etInput);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ivClearName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etInput.setText(new String());
            }
        });

        Button cancel = view.findViewById(R.id.btnCancel);
        cancel.setText(leftBtnTxt);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickCancel();
                dialog.dismiss();
            }
        });

        Button ok = view.findViewById(R.id.btnOk);
        ok.setText(rightBtnTxt);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etInput.getText().toString().trim().length() < 1) {
                    ToastUtil.showToast(activity, R.string.name_device_enter_custom_name);
                    return;
                }

                if (listener != null) listener.onClickSave(etInput.getText().toString());
                dialog.dismiss();
            }
        });
        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();  //screen size
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        //params.width = metrics.widthPixels;
        //params.height = (int) (metrics.heightPixels * 0.8);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static AlertDialog showUpdatesDialog(Context ctx, String version, String tips, final OnClickConfirmButtonListener listener, CompoundButton.OnCheckedChangeListener cbListener) {
        Context context = getSafeContext(ctx);
        String content = String.format(context.getString(R.string.camera_settings_cam_firmware_update_info), version, tips);
        return showUpdatesDialog(ctx, context.getString(R.string.camera_settings_cam_firmware_update), content, context.getString(R.string.cancel), context.getString(R.string.continue_upper), listener, cbListener);
    }

    public static AlertDialog showUpdatesDialog(Context ctx, String title, String content, String leftBtn, String rightBtn, final OnClickConfirmButtonListener listener, CompoundButton.OnCheckedChangeListener cbListener) {
        Context context = getSafeContext(ctx);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_upgrade, null);
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(content);
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());

        CheckBox cbTip = view.findViewById(R.id.cbTip);
        cbTip.setChecked(false);
        if (cbListener != null) {
            cbTip.setVisibility(View.VISIBLE);
            cbTip.setOnCheckedChangeListener(cbListener);
        }

        FButton btnLeft = (FButton)view.findViewById(R.id.btnCancel);
        btnLeft.setText(leftBtn);
        FButton btnRight = (FButton)view.findViewById(R.id.btnOk);
        btnRight.setText(rightBtn);

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickLeft();
                dialog.dismiss();
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickRight();
                dialog.dismiss();
            }
        });

        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();  //screen size
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        //params.width = metrics.widthPixels;
        //params.height = (int) (metrics.heightPixels * 0.8);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static AlertDialog connectRouterFailDialog(Context ctx, final OnClickConfirmButtonListener listener, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        Context context = getSafeContext(ctx);
        String content = String.format(ctx.getString(R.string.router_router_connect_to_get_router_info));
        return showConnectRouterFailDialog(ctx, ctx.getString(R.string.router_router_get_info_fail), content, ctx.getString(R.string.router_exit), ctx.getString(R.string.router_retry), listener);

    }

    public static AlertDialog showConnectRouterFailDialog(Context ctx, String title, String content, String leftBtn, String rightBtn, final OnClickConfirmButtonListener listener) {
        Context context = getSafeContext(ctx);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_upgrade, null);
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.transparent_background_dialog).setView(view).create();



        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();  //screen size
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        //params.width = metrics.widthPixels;
        //params.height = (int) (metrics.heightPixels * 0.8);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(content);
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());

        FButton btnLeft = (FButton)view.findViewById(R.id.btnCancel);
        btnLeft.setText(leftBtn);
        FButton btnRight = (FButton)view.findViewById(R.id.btnOk);
        btnRight.setText(rightBtn);

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickLeft();
                dialog.dismiss();
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickRight();
                dialog.dismiss();
            }
        });
        return dialog;
    }

    public static AlertDialog removeRouterDialog(Context ctx, final OnClickConfirmButtonListener listener, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        Context context = getSafeContext(ctx);
        String content = String.format(ctx.getString(R.string.router_remove_device_dialog_info));
        return showRemoveRouterDialog(ctx, ctx.getString(R.string.router_detail_setting_remove_device), content, ctx.getString(R.string.cancel), ctx.getString(R.string.confirm), listener);

    }

    public static AlertDialog updataRouterDialog(Context ctx, final OnClickConfirmButtonListener listener, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        Context context = getSafeContext(ctx);
        String content = String.format(ctx.getString(R.string.router_firmware_upgrade_dialog_info));
        return showRemoveRouterDialog(ctx, ctx.getString(R.string.router_update), content, ctx.getString(R.string.cancel), ctx.getString(R.string.confirm), listener);

    }

    public static AlertDialog showRemoveRouterDialog(Context ctx, String title, String content, String leftBtn, String rightBtn, final OnClickConfirmButtonListener listener) {
        Context context = getSafeContext(ctx);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_upgrade, null);
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(content);
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());

        FButton btnLeft = (FButton)view.findViewById(R.id.btnCancel);
        btnLeft.setText(leftBtn);
        FButton btnRight = (FButton)view.findViewById(R.id.btnOk);
        btnRight.setText(rightBtn);

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickLeft();
                dialog.dismiss();
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickRight();
                dialog.dismiss();
            }
        });

        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();  //screen size
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        //params.width = metrics.widthPixels;
        //params.height = (int) (metrics.heightPixels * 0.8);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static AlertDialog rebootRouterDialog(Context ctx, final OnClickConfirmButtonListener listener, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        Context context = getSafeContext(ctx);
        String content = String.format(ctx.getString(R.string.router_reboot_dialog_msg));
        return showRebootRouterDialog(ctx, ctx.getString(R.string.router_reboot_dialog_info), content, ctx.getString(R.string.cancel), ctx.getString(R.string.confirm), listener);
    }

    public static AlertDialog showRebootRouterDialog(Context ctx, String title, String content, String leftBtn, String rightBtn, final OnClickConfirmButtonListener listener) {
        Context context = getSafeContext(ctx);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_upgrade, null);
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(content);
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());

        FButton btnLeft = (FButton)view.findViewById(R.id.btnCancel);
        btnLeft.setText(leftBtn);
        FButton btnRight = (FButton)view.findViewById(R.id.btnOk);
        btnRight.setText(rightBtn);

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickLeft();
                dialog.dismiss();
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickRight();
                dialog.dismiss();
            }
        });

        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();  //screen size
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        //params.width = metrics.widthPixels;
        //params.height = (int) (metrics.heightPixels * 0.8);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static AlertDialog accessRouterModeDialog(Context ctx, final OnClickConfirmButtonListener listener, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        Context context = getSafeContext(ctx);
        String content = String.format(ctx.getString(R.string.router_turn_off_remote_off_dialog_msg));
        return showAccessRouterModeDialog(ctx, "", content, ctx.getString(R.string.cancel), ctx.getString(R.string.confirm), listener);
    }

    public static AlertDialog showAccessRouterModeDialog(Context ctx, String title, String content, String leftBtn, String rightBtn, final OnClickConfirmButtonListener listener) {
        Context context = getSafeContext(ctx);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_upgrade, null);
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        if ("".equals(title)) {
            tvTitle.setVisibility(View.GONE);
        }
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(content);
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());

        FButton btnLeft = (FButton)view.findViewById(R.id.btnCancel);
        btnLeft.setText(leftBtn);
        FButton btnRight = (FButton)view.findViewById(R.id.btnOk);
        btnRight.setText(rightBtn);

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickLeft();
                dialog.dismiss();
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickRight();
                dialog.dismiss();
            }
        });

        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();  //screen size
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        //params.width = metrics.widthPixels;
        //params.height = (int) (metrics.heightPixels * 0.8);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static AlertDialog noConnectRouterDialog(Context ctx, final OnClickConfirmButtonListener listener, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        Context context = getSafeContext(ctx);
        String content = String.format(ctx.getString(R.string.router_connect_router_to_access_dialog_msg));
        return showNoConnectRouterDialog(ctx, ctx.getString(R.string.add_camera_connected_failed), content, ctx.getString(R.string.cancel), ctx.getString(R.string.add_camera_go_to_connect), listener);
    }

    public static AlertDialog showNoConnectRouterDialog(Context ctx, String title, String content, String leftBtn, String rightBtn, final OnClickConfirmButtonListener listener) {
        Context context = getSafeContext(ctx);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_upgrade/*dialog_no_connect_router*/, null);
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.transparent_background_dialog).setView(view).create();

        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(content);
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());

        FButton btnLeft = (FButton)view.findViewById(R.id.btnCancel);
        btnLeft.setText(leftBtn);
        FButton btnRight = (FButton)view.findViewById(R.id.btnOk);
        btnRight.setText(rightBtn);

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickLeft();
                dialog.dismiss();
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickRight();
                dialog.dismiss();
            }
        });

        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();  //screen size
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        //params.width = metrics.widthPixels;
        //params.height = (int) (metrics.heightPixels * 0.8);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }



    public static AlertDialog forgotPasswordDialog(Context ctx, final OnClickConfirmButtonListener listener) {
        Context context = getSafeContext(ctx);
        String content = String.format(ctx.getString(R.string.router_config_dialog_forget_msg));
        return showAccessRouterModeDialog(ctx, ctx.getString(R.string.router_forget_password), content,
                ctx.getString(R.string.router_i_got_it), ctx.getString(R.string.router_how_to_reset), listener);
    }

    public static AlertDialog connectionRouterFairDialog(Context ctx, final OnClickConfirmButtonListener listener, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        Context context = getSafeContext(ctx);
        String content = String.format(ctx.getString(R.string.router_not_connect));
        return showBackupRouterDialog(ctx, ctx.getString(R.string.router_connected_fail), content, ctx.getString(R.string.cancel), ctx.getString(R.string.add_camera_go_to_connect), listener);
    }

    public static AlertDialog showBackupRouterDialog(Context ctx, String titleString, String content, String leftBtn, String rightBtn, final OnClickConfirmButtonListener listener) {
        Context context = getSafeContext(ctx);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_upgrade, null);
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(titleString);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(content);
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());

        FButton btnLeft = (FButton)view.findViewById(R.id.btnCancel);
        btnLeft.setText(leftBtn);
        FButton btnRight = (FButton)view.findViewById(R.id.btnOk);
        btnRight.setText(rightBtn);

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickLeft();
                dialog.dismiss();
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClickRight();
                dialog.dismiss();
            }
        });

        /*FButton btnCenter = (FButton)view.findViewById(R.id.btnOk);
        btnCenter.setText(centerBtn);

        btnCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onConfirmClick();
                dialog.dismiss();
            }
        });*/

        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();  //screen size
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        //params.width = metrics.widthPixels;
        //params.height = (int) (metrics.heightPixels * 0.8);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static AlertDialog showRequestingDialog(Context ctx, int title) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_request_verify_code, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        dialog.show();
        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();  //screen size
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = metrics.widthPixels;
        //params.height = (int) (metrics.heightPixels * 0.8);
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static AlertDialog showPushActiveDialog(Context ctx, String title, String message, String confirmBtn, boolean cancel, final OnClickInformationDialogLisenter lisenter) {
        Context context = getSafeContext(ctx);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_push_active, null);
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.transparent_background_dialog).setView(view).create();
        dialog.setCancelable(cancel);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);
        tvMessage.setGravity(isSingleLine(ctx, message, tvMessage.getTextSize(), DIALOG_OUT_SIDE_LENGTH) ? Gravity.CENTER : Gravity.START);
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());

        Button btnOK = view.findViewById(R.id.btnOk);
        if (!TextUtils.isEmpty(confirmBtn)) {
            btnOK.setText(confirmBtn);
        }
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (lisenter != null) {
                    lisenter.onConfirmClick();
                }
            }
        });

        ImageView btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        //dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        //set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        //screen size
        Display d = m.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static AlertDialog showListViewDialog(Context ctx, String title, RecyclerView.Adapter adapter, boolean cancel) {
        Context context = getSafeContext(ctx);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_list_view, null);
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.transparent_background_dialog).setView(view).create();
        dialog.setCancelable(cancel);

        TextView tvTitle = (TextView)view.findViewById(R.id.tvListTitle);
        tvTitle.setText(title);

        RecyclerView rcvList = (RecyclerView)view.findViewById(R.id.rcvList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        rcvList.setLayoutManager(layoutManager);
        rcvList.setAdapter(adapter);

        dialog.show();

        //set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        //screen size
        Display d = m.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static AlertDialog showBleAuthorizationCodeDialog(Context ctx, String title, String message, String hint, String cancelBtn, String confirmBtn, boolean cancel, int inputType, final OnClickBleAuthorizationCodeDialogListener lisenter) {
        Context context = getSafeContext(ctx);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_ble_authorization_code, null);
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.transparent_background_dialog).setView(view).create();
        dialog.setCancelable(cancel);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        TextView tvMessage = view.findViewById(R.id.tvAuthorizationCode);
        tvMessage.setText(message);
        /*
        tvMessage.setGravity(isSingleLine(ctx, message, tvMessage.getTextSize(), DIALOG_OUT_SIDE_LENGTH) ? Gravity.CENTER : Gravity.START);
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
        */
        AutoCompleteTextView etName = (AutoCompleteTextView)view.findViewById(R.id.etName);
        etName.setHint(hint);

        Button btnCancel = view.findViewById(R.id.btnCancel);
        if (!TextUtils.isEmpty(cancelBtn)) {
            btnCancel.setText(cancelBtn);
        }

        Button btnOK = view.findViewById(R.id.btnOk);
        if (!TextUtils.isEmpty(confirmBtn)) {
            btnOK.setText(confirmBtn);
        }

        switch (inputType) {
            case INPUT_DIALOG_TYPE_TEXT:
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setTextSize(40);
                etName.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                break;
            case INPUT_DIALOG_TYPE_EDIT:
                tvMessage.setVisibility(View.GONE);
                etName.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                break;
            case INPUT_DIALOG_TYPE_TEXT_EDIT:
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setTextSize(20);
                etName.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                break;
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (lisenter != null) {
                    lisenter.onClickCancel();
                }
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (lisenter != null) {
                    String content = etName != null ? etName.getText().toString() : "";
                    lisenter.onClickConfirm(inputType, content);
                }
            }
        });

        //dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        //set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        //screen size
        Display d = m.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static AlertDialog showInfoWithCheckboxDialog(Context ctx, String title, String content, String tip, String btn, final OnClickInformationDialogLisenter listener, CompoundButton.OnCheckedChangeListener cbListener) {
        Context context = getSafeContext(ctx);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_info_with_checkbox, null);
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        tvTitle.setText(title);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        tvMessage.setText(content);
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
        CheckBox cbTip = view.findViewById(R.id.cbDialogTip);
        cbTip.setText(tip);
        cbTip.setChecked(false);
        if (cbListener != null) {
            cbTip.setVisibility(View.VISIBLE);
            cbTip.setOnCheckedChangeListener(cbListener);
        }

        FButton btnDialogOk = (FButton)view.findViewById(R.id.btnDialogOk);
        btnDialogOk.setText(btn);
        btnDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onConfirmClick();
                dialog.dismiss();
            }
        });
        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();
        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();  //screen size
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static AlertDialog showPrivacyPolicyDialog(Context ctx, String title, String content, String tip, String btn, String termText, String privacyText, final OnClickInformationDialogLisenter listener, CompoundButton.OnCheckedChangeListener cbListener, ClickableSpan termClickableSpan, ClickableSpan privacyClickableSpan) {
        Context context = getSafeContext(ctx);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_info_with_checkbox, null);
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        tvTitle.setText(title);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        //tvMessage.setText(content);
        setupPrivacyClickableTv(context, tvMessage, R.color.theme_green, termText, privacyText, content, termClickableSpan, privacyClickableSpan);
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
        CheckBox cbTip = view.findViewById(R.id.cbDialogTip);
        cbTip.setText(tip);
        cbTip.setChecked(false);
        if (cbListener != null) {
            cbTip.setVisibility(View.VISIBLE);
            cbTip.setOnCheckedChangeListener(cbListener);
        }

        FButton btnDialogOk = (FButton)view.findViewById(R.id.btnDialogOk);
        btnDialogOk.setText(btn);
        btnDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onConfirmClick();
                dialog.dismiss();
            }
        });
        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();
        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();  //screen size
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static void showWithAlphaAnim(View view) {
        showWithAlphaAnim(view, null);
    }

    public static void showWithAlphaAnim(View view, Animation.AnimationListener listener) {
        AnimationSet showAnim = new AnimationSet(true);
        showAnim.setDuration(500);

        AlphaAnimation alphaAnim = new AlphaAnimation(0.f, 1.f);
        showAnim.addAnimation(alphaAnim);
        showAnim.setAnimationListener(listener);

        view.setVisibility(View.VISIBLE);
        view.startAnimation(showAnim);
    }

    public static void hideWithAlphaAnim(View view) {
        hideWithAlphaAnim(view, 500, null);
    }

    public static void hideWithAlphaAnim(View view, int duration) {
        hideWithAlphaAnim(view, duration, null);
    }

    public static void hideWithAlphaAnim(View view, int duration, Animation.AnimationListener listener) {
        AnimationSet showAnim = new AnimationSet(true);
        showAnim.setDuration(duration);

        AlphaAnimation alphaAnim = new AlphaAnimation(1.f, 0.f);
        showAnim.addAnimation(alphaAnim);
        showAnim.setAnimationListener(listener);
        view.startAnimation(showAnim);
        view.setVisibility(View.GONE);
    }

    public static void showWithPositionAnim(View view, ShowFromType from) {
        showWithPositionAnim(view, from, 500, null);
    }

    public static void showWithPositionAnim(View view, ShowFromType from, int duration) {
        showWithPositionAnim(view, from, duration, null);
    }

    public static void showWithPositionAnim(View view, ShowFromType from, int duration, Animation.AnimationListener listener) {
        AnimationSet showAnim = new AnimationSet(true);
        showAnim.setDuration(duration);

        AlphaAnimation alphaAnim = new AlphaAnimation(0, 1);
        showAnim.addAnimation(alphaAnim);

        float fromXValue = 0.0f;
        float toXValue = 0.0f;
        float fromYValue = 0.0f;
        float toYValue = 0.0f;

        if (from == ShowFromType.LEFT) {
            fromXValue = -1.0f;
            toXValue = 0.0f;
            fromYValue = 0.0f;
            toYValue = 0.0f;
        } else if (from == ShowFromType.TOP) {
            fromXValue = 0.0f;
            toXValue = 0.0f;
            fromYValue = -1.0f;
            toYValue = 0.0f;
        } else if (from == ShowFromType.RIGHT) {
            fromXValue = 1.0f;
            toXValue = 0.0f;
            fromYValue = 0.0f;
            toYValue = 0.0f;
        } else if (from == ShowFromType.BOTTOM) {
            fromXValue = 0.0f;
            toXValue = 0.0f;
            fromYValue = 1.0f;
            toYValue = 0.0f;
        }

        TranslateAnimation translateAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, fromXValue,
                Animation.RELATIVE_TO_SELF, toXValue,
                Animation.RELATIVE_TO_SELF, fromYValue,
                Animation.RELATIVE_TO_SELF, toYValue);
        showAnim.addAnimation(translateAnim);

        showAnim.setAnimationListener(listener);

        view.setVisibility(View.VISIBLE);
        view.startAnimation(showAnim);
    }

    public static void hideWithPositionAnim(final View view, ShowFromType from) {
        hideWithPositionAnim(view, from, 500);
    }

    public static void hideWithPositionAnim(final View view, ShowFromType from, int duration) {
        AnimationSet hideAnim = new AnimationSet(true);
        hideAnim.setDuration(duration);

        AlphaAnimation alphaAnim = new AlphaAnimation(1, 0);
        hideAnim.addAnimation(alphaAnim);

        float fromXValue = 0.0f;
        float toXValue = 0.0f;
        float fromYValue = 0.0f;
        float toYValue = 0.0f;

        if (from == ShowFromType.LEFT) {
            fromXValue = 0.0f;
            toXValue = -1.0f;
            fromYValue = 0.0f;
            toYValue = 0.0f;
        } else if (from == ShowFromType.TOP) {
            fromXValue = 0.0f;
            toXValue = 0.0f;
            fromYValue = 0.0f;
            toYValue = -1.0f;
        } else if (from == ShowFromType.RIGHT) {
            fromXValue = 0.0f;
            toXValue = 1.0f;
            fromYValue = 0.0f;
            toYValue = 0.0f;
        } else if (from == ShowFromType.BOTTOM) {
            fromXValue = 0.0f;
            toXValue = 0.0f;
            fromYValue = 0.0f;
            toYValue = 1.0f;
        }

        TranslateAnimation translateAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, fromXValue,
                Animation.RELATIVE_TO_SELF, toXValue,
                Animation.RELATIVE_TO_SELF, fromYValue,
                Animation.RELATIVE_TO_SELF, toYValue);
        hideAnim.addAnimation(translateAnim);
        hideAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.startAnimation(hideAnim);
    }

    /**
     * 设置EditText的背景色
     *
     * @param editText
     */
    public static void setInputBg(EditText editText) {
        if (editText.getText().toString().length() == 0) {
            editText.setBackgroundResource(R.drawable.edit_text_no_content_bg);
        } else {
            editText.setBackgroundResource(R.drawable.edit_text_content_bg);
        }
    }

    public static void setupPrivacyClickableTv(Context ctx, TextView tvContent, int spanTextColor, String termsText, String privacyText, String content, ClickableSpan termClickableSpan, ClickableSpan privacyClickableSpan) {
        if (ctx == null || tvContent == null || TextUtils.isEmpty(termsText) || TextUtils.isEmpty(privacyText) || TextUtils.isEmpty(content)) {
            return;
        }
        Context context = getSafeContext(ctx);
        SpannableStringBuilder style = new SpannableStringBuilder();
        try {
            String text = String.format(content, termsText, privacyText);
            //设置文字
            style.append(text);
            int termsStart = text.indexOf(termsText);
            int termsEnd = termsStart + termsText.length();
            boolean termSetStyleValid = termsStart >= 0 && termsEnd > termsStart;
            if (termSetStyleValid) {
                //设置部分文字点击事件
                if (termClickableSpan != null) {
                    style.setSpan(termClickableSpan, termsStart, termsEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                tvContent.setText(style);
                //设置部分文字颜色
                ForegroundColorSpan conditionForegroundColorSpan = new ForegroundColorSpan(CompatUtil.getColor(context, spanTextColor));
                style.setSpan(conditionForegroundColorSpan, termsStart, termsEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            int privacyStart = text.indexOf(privacyText);
            int privacyEnd =  privacyStart + privacyText.length();
            boolean privacySetStyleValid = privacyStart >= 0 && termsEnd > termsStart;
            if (privacySetStyleValid) {
                //设置部分文字点击事件
                if (privacyClickableSpan != null) {
                    style.setSpan(privacyClickableSpan, privacyStart, privacyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                tvContent.setText(style);
                //设置部分文字颜色
                ForegroundColorSpan privacyForegroundColorSpan = new ForegroundColorSpan(CompatUtil.getColor(context, spanTextColor));
                style.setSpan(privacyForegroundColorSpan, privacyStart, privacyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        //配置给TextView
        tvContent.setText(style);
    }
}
