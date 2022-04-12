package com.bigkoo.convenientbanner.holder;

/**
 * Created by Sai on 15/12/14.
 *
 * @param <T> 任何你指定的对象
 */

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public abstract class Holder<T> extends RecyclerView.ViewHolder {
    public Holder(View itemView) {
        super(itemView);
        initView(itemView);
    }

    protected abstract void initView(View itemView);

    public abstract void updateUI(T data);
}
