package cc.ibooker.zphotochoose.zview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.LinearLayout;

import cc.ibooker.zphotochoose.R;


/**
 * 自定义最大ViewLayout
 *
 * @author 邹峰立
 */
public class MaxHeightViewLayout extends LinearLayout {
    private float maxRadio = 0;
    private int maxHeight = 0;

    public MaxHeightViewLayout(Context context) {
        this(context, null);
    }

    public MaxHeightViewLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaxHeightViewLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    // 初始化
    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightViewLayout);
        maxRadio = typedArray.getFloat(R.styleable.MaxHeightViewLayout_maxRadio, maxRadio);
        maxHeight = (int) typedArray.getDimension(R.styleable.MaxHeightViewLayout_maxHeight, maxHeight);
        typedArray.recycle();

        // 计算最大高度
        if (maxRadio > 0) {
            int screenHeight = getScreenHeight(context);
            int maxUseHeight = (int) (screenHeight * maxRadio);
            if ((maxHeight > maxUseHeight && maxUseHeight > 0) || maxHeight <= 0) {
                maxHeight = maxUseHeight;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (maxHeight > 0) {
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            heightSize = heightSize <= maxHeight ? heightSize : maxHeight;
            int maxHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
            super.onMeasure(widthMeasureSpec, maxHeightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    // 获取屏幕高度
    private int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            return wm.getDefaultDisplay().getHeight();
        }
        return 0;
    }
}
