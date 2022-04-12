package com.afar.osaio.smart.scan.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.util.QRCodeAsyncTask;
import com.google.gson.JsonObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by victor on 2018/7/2
 * Email is victor.qiao.0604@gmail.com
 */
public class QRCodeActivity extends BaseActivity {

    @BindView(R.id.ivQRCode)
    ImageView ivQRCode;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;

    public static void toQRCodeActivity(Context from) {
        Intent intent = new Intent(from, QRCodeActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initView() {
        tvTitle.setText(R.string.account_qr_code);
        ivRight.setVisibility(View.GONE);
    }

    private void initData() {
        JsonObject object = new JsonObject();
        object.addProperty("qr_type", 1);
        object.addProperty("qr_content", mUserAccount);

        final String content = object.toString();
        showLoading(false);
        QRCodeAsyncTask task = new QRCodeAsyncTask(new QRCodeAsyncTask.OnLoadFinishListener() {
            @Override
            public void onLoadBitmap(Bitmap bitmap) {
                if (bitmap == null) {
                    reloadArCode(content);
                } else {
                    ivQRCode.setImageBitmap(bitmap);
                    hideLoading();
                }
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, content);
    }

    private void reloadArCode(String content) {
        showLoading(false);
        QRCodeAsyncTask task = new QRCodeAsyncTask(new QRCodeAsyncTask.OnLoadFinishListener() {
            @Override
            public void onLoadBitmap(Bitmap bitmap) {
                ivQRCode.setImageBitmap(bitmap);
                hideLoading();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, content);
    }

    @OnClick(R.id.ivLeft)
    public void onViewClicked() {
        finish();
    }
}
