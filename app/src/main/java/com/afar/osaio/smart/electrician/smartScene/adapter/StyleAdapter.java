package com.afar.osaio.smart.electrician.smartScene.adapter;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.util.ConstantValue;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StyleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> styles = new ArrayList<>();
    private StyleItemListener mListener;

    private int mSelect = -1;

    public StyleAdapter(List<String> list) {
        this.styles = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_style, viewGroup, false);
        return new StyleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof StyleViewHolder) {
            final StyleViewHolder holder = (StyleViewHolder) viewHolder;
            Drawable drawable = DrawableCompat.wrap(ContextCompat.getDrawable(NooieApplication.mCtx, R.drawable.style_color));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DrawableCompat.setTint(drawable, Color.parseColor(styles.get(i)));
            } else {
                drawable.mutate().setColorFilter(Color.parseColor(styles.get(i)), PorterDuff.Mode.SRC_IN);
            }
            holder.ivStyle.setImageDrawable(drawable);

            if (styles.get(i).equals(ConstantValue.SMART_SCENE_COLOR_ONE)) {
                holder.ivStyleRing.setImageResource(R.drawable.style_border_one);
            } else if (styles.get(i).equals(ConstantValue.SMART_SCENE_COLOR_TWO)) {
                holder.ivStyleRing.setImageResource(R.drawable.style_border_two);
            } else if (styles.get(i).equals(ConstantValue.SMART_SCENE_COLOR_THREE)) {
                holder.ivStyleRing.setImageResource(R.drawable.style_border_three);
            } else if (styles.get(i).equals(ConstantValue.SMART_SCENE_COLOR_FOUR)) {
                holder.ivStyleRing.setImageResource(R.drawable.style_border_four);
            } else if (styles.get(i).equals(ConstantValue.SMART_SCENE_COLOR_FIVE)) {
                holder.ivStyleRing.setImageResource(R.drawable.style_border_five);
            } else if (styles.get(i).equals(ConstantValue.SMART_SCENE_COLOR_SIX)) {
                holder.ivStyleRing.setImageResource(R.drawable.style_border_six);
            } else if (styles.get(i).equals(ConstantValue.SMART_SCENE_COLOR_SEVEN)) {
                holder.ivStyleRing.setImageResource(R.drawable.style_border_seven);
            } else if (styles.get(i).equals(ConstantValue.SMART_SCENE_COLOR_EIGHT)) {
                holder.ivStyleRing.setImageResource(R.drawable.style_border_eight);
            } else if (styles.get(i).equals(ConstantValue.SMART_SCENE_COLOR_NINE)) {
                holder.ivStyleRing.setImageResource(R.drawable.style_border_nine);
            } else if (styles.get(i).equals(ConstantValue.SMART_SCENE_COLOR_TEN)) {
                holder.ivStyleRing.setImageResource(R.drawable.style_border_ten);
            } else if (styles.get(i).equals(ConstantValue.SMART_SCENE_COLOR_ELEVEN)) {
                holder.ivStyleRing.setImageResource(R.drawable.style_border_ele);
            } else if (styles.get(i).equals(ConstantValue.SMART_SCENE_COLOR_TWELVE)) {
                holder.ivStyleRing.setImageResource(R.drawable.style_border_twelve);
            }

            if (mSelect == i) {
                holder.ivStyleRing.setVisibility(View.VISIBLE);
            }else {
                holder.ivStyleRing.setVisibility(View.GONE);
            }

            holder.flStyle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(i, styles.get(i));
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return styles.size();
    }

    public static class StyleViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.flStyle)
        FrameLayout flStyle;
        @BindView(R.id.ivStyle)
        ImageView ivStyle;
        @BindView(R.id.ivStyleRing)
        ImageView ivStyleRing;


        public StyleViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

        }
    }

    public void setListener(StyleItemListener listener) {
        mListener = listener;
    }

    public interface StyleItemListener {
        void onItemClick(int position, String color);
    }

    public void changeSelected(int position) {
        if (position != mSelect) {
            mSelect = position;
            notifyDataSetChanged();
        }
    }
}
