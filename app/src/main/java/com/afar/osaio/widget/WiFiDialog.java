package com.afar.osaio.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.afar.osaio.R;

/**
 * Create by Raymond  on 2021/6/16
 * Description: wifi 断开提示 对话框
 */
public class WiFiDialog extends Dialog {

    private Context context;
    private boolean isHide;


    public WiFiDialog(@NonNull Context context,Boolean isHide) {
        super(context, R.style.Dialog);
        this.context = context;
        this.isHide = isHide;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(context, R.layout.dialog_wifi_connect_state, null);
        setContentView(view);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();

        WindowManager m = dialogWindow.getWindowManager();
        Display d = m.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(params);

        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnOk = view.findViewById(R.id.btnOk);

        btnCancel.setOnClickListener(view1 -> {
            dismiss();
        });


        btnOk.setOnClickListener(view1 -> {
            Intent intent = new Intent();
            intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
            context.startActivity(intent);
            if (isHide){
                dismiss();
            }
        });

    }


    public interface CancelListener {
        void isCancel();
    }


}
