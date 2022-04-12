package com.afar.osaio.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.afar.osaio.R;

public class SquareImageView extends AppCompatImageView {

    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    public void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        //圆角剪切
        int connerRadius = (int)(getResources().getDimension(R.dimen.dp_3) + 0.5);
        int topLeftRadius = connerRadius;
        int topRightRadius = connerRadius;
        int bottomLeftRadius = connerRadius;
        int bottomRightRadius = connerRadius;
        Path path = new Path();
        path.moveTo(0, topLeftRadius);
        path.arcTo(new RectF(0, 0, topLeftRadius * 2, topLeftRadius * 2), -180, 90);
        path.lineTo(width - topRightRadius, 0);
        path.arcTo(new RectF(width - 2 * topRightRadius, 0, width, topRightRadius * 2), -90, 90);
        path.lineTo(width, height - bottomRightRadius);
        path.arcTo(new RectF(width - 2 * bottomRightRadius, height - 2 * bottomRightRadius, width, height), 0, 90);
        path.lineTo(bottomLeftRadius, height);
        path.arcTo(new RectF(0, height - 2 * bottomLeftRadius, bottomLeftRadius * 2, height), 90, 90);
        path.close();
        canvas.clipPath(path);
        //灰色背景
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(getResources().getColor(R.color.gray_d8dde6));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, width, height, paint);
        //内容
        super.onDraw(canvas);
    }
}
