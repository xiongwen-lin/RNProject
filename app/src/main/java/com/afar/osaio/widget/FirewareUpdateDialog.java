package com.afar.osaio.widget;

import android.app.Dialog;
import android.content.Context;
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
 * Create by Raymond  on 2021/6/18
 * Description: 固件升级Dialog
 */
public class FirewareUpdateDialog extends Dialog {

    private Context context;

    public FirewareUpdateDialog(@NonNull Context context) {
        super(context,R.style.Dialog);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = View.inflate(context, R.layout.dialog_router_fireware_update, null);
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
            dismiss();
        });
    }
}
