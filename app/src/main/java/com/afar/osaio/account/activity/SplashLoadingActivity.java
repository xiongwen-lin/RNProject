package com.afar.osaio.account.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashLoadingActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
        finish();
    }
}
