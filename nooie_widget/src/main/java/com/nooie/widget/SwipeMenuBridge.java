/*
 * Copyright 2017 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nooie.widget;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by YanZhenjie on 2017/7/20.
 */
public class SwipeMenuBridge {

    private final Controller mController;
    private final int mDirection;
    private final int mPosition;

    private TextView txtView;
    private ImageView imgView;

    private boolean isDefaultShowImg;
    private int flag;

    public SwipeMenuBridge(Controller controller, int direction, int position, TextView txtView, ImageView imgView, boolean isDefaultShowImg) {
        this.mController = controller;
        this.mDirection = direction;
        this.mPosition = position;
        this.txtView = txtView;
        this.imgView = imgView;
        this.flag = 0;
        this.isDefaultShowImg = isDefaultShowImg;
    }

    @NooieSwipeRecyclerView.DirectionMode
    public int getDirection() {
        return mDirection;
    }

    /**
     * Get the position of button in the menu.
     */
    public int getPosition() {
        return mPosition;
    }

    public void closeMenu() {
        mController.smoothCloseMenu();
    }

    public void setImg(Context ctx, @DrawableRes int res) {
        if (this.imgView != null) {
            this.imgView.setVisibility(View.VISIBLE);
            this.imgView.setImageDrawable(ContextCompat.getDrawable(ctx, res));
            if (this.txtView != null) {
                this.txtView.setVisibility(View.GONE);
            }
        }
    }

    public void setText(@StringRes int res) {
        if (this.txtView != null) {
            this.txtView.setVisibility(View.VISIBLE);
            this.txtView.setText(res);
            if (this.imgView != null) {
                this.imgView.setVisibility(View.GONE);
            }
        }
    }

    public String getText() {
        if (txtView != null) return txtView.getText().toString();
        return null;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return this.flag;
    }

    public void reset() {
        this.flag = 0;
        imgView.setVisibility(isDefaultShowImg ? View.VISIBLE : View.GONE);
        txtView.setVisibility(isDefaultShowImg ? View.GONE : View.VISIBLE);
    }
}