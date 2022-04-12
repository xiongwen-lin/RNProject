package com.afar.osaio.smart.electrician.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
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
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.log.NooieLog;

import java.lang.ref.WeakReference;

/**
 * DialogUtil
 *
 * @author Administrator
 * @date 2019/2/18
 */
public class DialogUtil {
    public static final int DIALOG_OUT_SIDE_LENGTH = 78;

    public enum ShowFromType {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM,
    }

    public interface OnClickShareDeviceHandleButtonListener {
        void onClickIgnore();

        void onClickReject();

        void onClickAgree();
    }

    public interface OnClickSingleButtonListener {
        void onClick();
    }

    public interface OnClickSelectAddDeviceListener {
        void onClickAddPowerStrip();

        void onClickAddDevice();
    }

    public interface OnClickConfirmButtonListener {
        void onClickRight();

        void onClickLeft();
    }

    public interface OnClickListConfirmButtonListener {
        void onClickRight();

        void onClickLeft();
    }

    public interface OnClickInputDialogListener {
        void onClickCancel();

        void onClickSave(String text);
    }

    public interface OnClickWifiDialogListener {
        void onClickCancel();

        void onClickCOnfirm(String ssid, String password);
    }

    public interface OnSelectItemListener<T> {
        void onSelectItem(T t);
    }

    public interface OnClickDeviceUpadteListener {
        void onClickLeft(boolean checked);

        void onClickRight(boolean checked);
    }

    public interface OnClickActiveMsgListener {
        void onCloseClick();

        void onOkClick();
    }

    public interface OnClickLrcListener {
        void onClose();

        void onClickRight();

        void onClickLeft();
    }

    public static AlertDialog showLoadingDialog(Context ctx) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_loading, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();

        ImageView iv = (ImageView) view.findViewById(R.id.ivLoading);
        iv.setBackgroundResource(R.drawable.loading_anim);

        final AnimationDrawable animationDrawable = (AnimationDrawable) iv.getBackground();
        if (animationDrawable != null && !animationDrawable.isRunning()) {
            animationDrawable.start();
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (animationDrawable != null && animationDrawable.isRunning()) {
                    animationDrawable.stop();
                }
            }
        });

        dialog.show();
        return dialog;
    }

    public static AlertDialog showTransparentLoadingDialog(Context ctx) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_loading, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_transparent_dialog).setView(view).create();

        ImageView iv = (ImageView) view.findViewById(R.id.ivLoading);
        iv.setBackgroundResource(R.drawable.loading_anim);

        final AnimationDrawable animationDrawable = (AnimationDrawable) iv.getBackground();
        if (animationDrawable != null && !animationDrawable.isRunning()) {
            animationDrawable.start();
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (animationDrawable != null && animationDrawable.isRunning()) {
                    animationDrawable.stop();
                }
            }
        });

        dialog.show();
        return dialog;
    }

    public static AlertDialog showConfirmWithSubMsgDialog(Context ctx, int title, int message, int leftBtn, int rightBtn, final OnClickConfirmButtonListener listener) {
        return showConfirmWithSubMsgDialog(ctx, ctx.getString(title), ctx.getString(message), leftBtn, rightBtn, listener);
    }

    public static AlertDialog showConfirmWithSubMsgDialog(Context ctx, int title, String message, int leftBtn, int rightBtn, final OnClickConfirmButtonListener listener) {
        return showConfirmWithSubMsgDialog(ctx, ctx.getString(title), message, leftBtn, rightBtn, listener);
    }

    public static AlertDialog showConfirmWithSubMsgDialog(Context ctx, String title, int message, int leftBtn, int rightBtn, final OnClickConfirmButtonListener listener) {
        return showConfirmWithSubMsgDialog(ctx, title, ctx.getString(message), leftBtn, rightBtn, listener);
    }

    public static AlertDialog showConfirmWithSubMsgDialog(Context ctx, String title, String message, int leftBtn, int rightBtn, final OnClickConfirmButtonListener listener) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_confirm_with_submessage, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);

        Button btnCancel = view.findViewById(R.id.btnCancel);
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

        Button btnOk = view.findViewById(R.id.btnOk);
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
        Display d = m.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);

        return dialog;
    }

    public static AlertDialog showJoinFBDialog(Context ctx, int title, int message, int leftBtn, int rightBtn, final OnClickConfirmButtonListener listener) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_confirm_with_submessage_cancel, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);

        ImageView ivCancel = view.findViewById(R.id.ivCancel);
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        Button btnCancel = view.findViewById(R.id.btnCancel);
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

        Button btnOk = view.findViewById(R.id.btnOk);
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
        Display d = m.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);

        return dialog;
    }

    public static AlertDialog showJoinFBDialog2(Context ctx, int title, int message, int leftBtn, int rightBtn, final OnClickLrcListener listener) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_confirm_with_submessage_cancel, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);

        ImageView ivCancel = view.findViewById(R.id.ivCancel);
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClose();
                }
                dialog.dismiss();
            }
        });
        Button btnCancel = view.findViewById(R.id.btnCancel);
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

        Button btnOk = view.findViewById(R.id.btnOk);
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
        Display d = m.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);

        return dialog;
    }

    public static AlertDialog showReNameDialog(Context ctx, int leftBtn, int rightBtn, String name, final OnClickInputDialogListener listener) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_rename, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();

        final EditText editText = view.findViewById(R.id.etMsg);

        editText.setText(name);

        Button btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setText(leftBtn);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClickCancel();
                }
                dialog.dismiss();
            }
        });

        Button btnOk = view.findViewById(R.id.btnSave);
        btnOk.setText(rightBtn);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClickSave(editText.getText().toString());
                }
                dialog.dismiss();
            }
        });

        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();
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
                if (listener != null) {
                    listener.onClickRight();
                }
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;

    }


    //public static AlertDialog showConfirmWithSubMsgDialog(Context ctx, int title, int message, final OnClickConfirmButtonListener listener) {
    //    return showConfirmWithSubMsgDialog(ctx, title, message, R.string.cancel, R.string.ok, listener);
    //}

    public static AlertDialog showConfirmDialog(Context ctx, int title, final OnClickConfirmButtonListener listener) {
        return showConfirmDialog(ctx, NooieApplication.get().getString(title), R.string.cancel, R.string.ok, true, listener);
    }

    public static AlertDialog showConfirmDialog(Context ctx, int title, int leftBtn, int rightBtn, final OnClickConfirmButtonListener listener) {
        return showConfirmDialog(ctx, NooieApplication.get().getString(title), leftBtn, rightBtn, true, listener);
    }

    //public static AlertDialog showConfirmDialog(Context ctx, int title, int leftBtn, int rightBtn, final OnClickConfirmButtonListener listener) {
    //    return showConfirmDialog(ctx, NooieApplication.get().getString(title), leftBtn, rightBtn, true, listener);
    //}

    //public static AlertDialog showConfirmDialog(Context ctx, int title, int leftBtn, int rightBtn, boolean cancel, final OnClickConfirmButtonListener listener) {
    //    return showConfirmDialog(ctx, NooieApplication.get().getString(title), leftBtn, rightBtn, cancel, listener);
    //}

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
                if (listener != null) {
                    listener.onClickLeft();
                }
                dialog.dismiss();
            }
        });

        Button btnOK = view.findViewById(R.id.btnOk);
        btnOK.setText(rightBtn);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClickRight();
                }
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(cancel);
        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static AlertDialog showSingleBtnDialog(Context ctx, String title, String message, int btn, boolean cancel, final OnClickSingleButtonListener listener) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_information, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);

        Button btnOK = view.findViewById(R.id.btnOk);
        btnOK.setText(btn);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClick();
                }
                dialog.dismiss();
            }
        });

        try {
            dialog.setCanceledOnTouchOutside(cancel);
            dialog.show();

            // set attributes
            Window dialogWindow = dialog.getWindow();
            WindowManager.LayoutParams params = dialogWindow.getAttributes();

            WindowManager m = dialogWindow.getWindowManager();
            Display d = m.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            d.getMetrics(metrics);

            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            dialogWindow.setGravity(Gravity.CENTER);
            dialogWindow.setAttributes(params);
        } catch (WindowManager.BadTokenException e) {
            NooieLog.e("------------BadTokenException  " + e.getMessage());
        }
        return dialog;
    }


    public static AlertDialog showSingleBtnDialogForMsg(Context ctx, String title, String message, int btn, boolean cancel, final OnClickSingleButtonListener listener) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_information, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        final TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);

        tvMessage.post(new Runnable() {
            @Override
            public void run() {
                if (tvMessage.getLineCount() > 1) {
                    tvMessage.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                }
            }
        });

        Button btnOK = view.findViewById(R.id.btnOk);
        btnOK.setText(btn);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClick();
                }
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(cancel);
        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static AlertDialog showInformationDialog(Context ctx, String title, String message) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_information, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        final TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);

        /*ViewTreeObserver vto = tvMessage.getViewTreeObserver();
        //这个监听器看名字也知道了，就是在绘画完成之前调用的，在这里面可以获取到行数，当然也可以获取到宽高等信息。
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int lineCount = tvMessage.getLineCount();
                if (lineCount <= 1) {
                    tvMessage.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                } else {
                    tvMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                }
                return true;
            }
        });*/
        tvMessage.setTextAlignment(isSingleLine(ctx, message, tvMessage.getTextSize(), DIALOG_OUT_SIDE_LENGTH) ? View.TEXT_ALIGNMENT_CENTER : View.TEXT_ALIGNMENT_TEXT_START);
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());

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
        Display d = m.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

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
        //DisplayUtil.setInputBg(etInput);
        etInput.setSelection(content == null ? 0 : content.length());

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ivClearName.setVisibility(TextUtils.isEmpty(etInput.getText().toString()) ? View.GONE : View.VISIBLE);
                //DisplayUtil.setInputBg(etInput);
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
                if (listener != null) {
                    listener.onClickCancel();
                }
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

                if (listener != null) {
                    listener.onClickSave(etInput.getText().toString());
                }
                dialog.dismiss();
            }
        });
        dialog.show();

        // set attributes
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

    public static AlertDialog showPermissionDialog(Context ctx, int mConMode,final OnClickConfirmButtonListener listener) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_permission, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();

        Button btnCancel = view.findViewById(R.id.btnCancel);
        if (mConMode == ConstantValue.AP_MODE){
            btnCancel.setText(R.string.cancel);
            TextView tvMessage = view.findViewById(R.id.tvMessage);
            tvMessage.setText(R.string.location_permision_ap);
        }
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClickLeft();
                }
                dialog.dismiss();
            }
        });

        Button btnOk = view.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClickRight();
                }
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(false);

        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);

        return dialog;
    }


    public static AlertDialog showGetSsidFailDialog(Context ctx, boolean cancel, final OnClickSingleButtonListener listener) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_get_ssid_fail, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();


        Button btnOK = view.findViewById(R.id.btnOk);

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClick();
                }
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(cancel);
        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);
        return dialog;
    }

    public static AlertDialog showAPPermissionDialog(Context ctx, final OnClickSingleButtonListener listener) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_ap_permission, null);
        final AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.transparent_background_dialog).setView(view).create();


        Button btnOk = view.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClick();
                }
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(false);

        dialog.show();

        // set attributes
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

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

    public static Context getSafeContext(Context context) {
        WeakReference<Context> weakReference = new WeakReference<>(context);
        return weakReference.get() != null ? weakReference.get() : new WeakReference<Context>(context).get();
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

        FButton btnLeft = (FButton) view.findViewById(R.id.btnCancel);
        btnLeft.setText(leftBtn);
        FButton btnRight = (FButton) view.findViewById(R.id.btnOk);
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

    public static AlertDialog showInformationNormalDialog(Context ctx, String title, String message, final OnClickInformationDialogLisenter lisenter) {
        return showInformationNormalDialog(ctx, title, message, true, lisenter);
    }

    public static AlertDialog showInformationNormalDialog(Context ctx, String title, String message, boolean cancel, final OnClickInformationDialogLisenter lisenter) {
       return showInformationDialog(ctx, title, message, null, cancel, lisenter);
    }

    public static AlertDialog showInformationDialog(Context ctx, String title, String message, String confirmBtn, boolean cancel, final OnClickInformationDialogLisenter lisenter) {
        Context context = getSafeContext(ctx);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_information, null);
        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.transparent_background_dialog).setView(view).create();
        dialog.setCancelable(cancel);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);
        tvMessage.setTextAlignment(isSingleLine(ctx, message, tvMessage.getTextSize(), DIALOG_OUT_SIDE_LENGTH) ? View.TEXT_ALIGNMENT_CENTER : View.TEXT_ALIGNMENT_TEXT_START);
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());

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

    /**
     * @param context
     * @param text
     * @param textSize
     * @param outSize  水平总边距
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

    public interface OnClickInformationDialogLisenter {
        void onConfirmClick();
    }
}
