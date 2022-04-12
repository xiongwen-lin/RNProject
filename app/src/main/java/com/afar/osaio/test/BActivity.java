package com.afar.osaio.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.widget.ImageView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by victor on 2018/8/6
 * Email is victor.qiao.0604@gmail.com
 */
public class BActivity extends BaseActivity {

    @BindView(R.id.imageView3)
    ImageView imageView3;

    public static void toBActivity(Context from) {
        Intent intent = new Intent(from, BActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a);
        ButterKnife.bind(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    @OnClick(R.id.imageView3)
    public void onViewClicked() {
        CActivity.toCActivity(this);
    }
}
