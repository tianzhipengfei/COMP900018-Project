package com.example.group_w01_07_3.features.discover;
/**
 * Modified from https://github.com/SamanLan/SlideValidation
 */
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import com.example.group_w01_07_3.R;
import java.util.Random;

public class SlideValidationView extends androidx.appcompat.widget.AppCompatImageView {
    public SlideValidationView(Context context) {
        super(context);
        init(context);
    }

    public SlideValidationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.SlideValidationView);
        validationSize = (int) typedArray.getDimension(R.styleable.SlideValidationView_validationSize, 0);
        typedArray.recycle();
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
//        // 关闭硬件加速
////        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }
    Context mContext;
    int width = 0;
    int height = 0;
    int validationSize = 0;
    int circleSize = 0;
    Path validationPath;
    float offsetX = 0;
    int animaOffsetX = 0;
    int startX;
    int startY;
    boolean first = true;
    boolean success = false;
    SlideListener mListener;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (success) {
            return;
        } else {
            Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            mPaint.setColor(0xff444444);//0x99000000
            mPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.SOLID));
            Paint mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            Paint mMaskShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            mMaskShadowPaint.setColor(0xff444444);
            mMaskShadowPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.SOLID));
            if (first) {
                creatValidationPath();
                first = false;
            }
            canvas.drawPath(validationPath, mPaint);
            craeteMask(canvas, mMaskPaint, mMaskShadowPaint);
        }


    }

//    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    /**
     * 创建验证区域path
     */
    private void creatValidationPath() {
        validationPath = new Path();
        if (validationSize == 0) {
            validationSize = width/6;
        }
        circleSize = validationSize / 3;
        startX = new Random().nextInt(width - validationSize * 2 - circleSize * 2 - 10) + circleSize + validationSize + 10;
        startY = new Random().nextInt(height - validationSize - circleSize * 2) + circleSize;
        validationPath.moveTo(startX, startY);
        validationPath.lineTo(startX + circleSize, startY);
        creatRandomArc(validationPath, startX + circleSize, startY, false, 0);
        validationPath.lineTo(startX + validationSize, startY);
        validationPath.lineTo(startX + validationSize, startY + circleSize);
        creatRandomArc(validationPath, startX + validationSize, startY + circleSize, true, 0);
        validationPath.lineTo(startX + validationSize, startY + validationSize);
        validationPath.lineTo(startX + circleSize * 2, startY + validationSize);
        creatRandomArc(validationPath, startX + circleSize, startY + validationSize, false, 1);
        validationPath.lineTo(startX, startY + validationSize);
        validationPath.lineTo(startX, startY + circleSize * 2);
        creatRandomArc(validationPath, startX, startY + circleSize, true, 1);
        validationPath.lineTo(startX, startY);
    }
    private void creatRandomArc(Path validationPath, int beginX, int beginY, boolean isleftRight, int type) {
        RectF rectF;
        if (isleftRight) {
            rectF = new RectF(beginX - circleSize / 2, beginY, beginX + circleSize / 2, beginY + circleSize);
        } else {
            rectF = new RectF(beginX, beginY - circleSize / 2, beginX + circleSize, beginY + circleSize / 2);
        }
        if (new Random().nextInt(10) > 5) {
            if (isleftRight) {
                validationPath.arcTo(rectF, -90 + type * 180, 180);
            } else {
                validationPath.arcTo(rectF, -180 + type * 180, 180);
            }
        } else {
            if (isleftRight) {
                validationPath.arcTo(rectF, -90 + type * 180, -180);
            } else {
                validationPath.arcTo(rectF, -180 + type * 180, -180);
            }
        }
    }

    private void craeteMask(Canvas canvas, Paint paint, Paint mMaskShadowPaint) {
        Bitmap mMaskBitmap = getMaskBitmap(((BitmapDrawable) getDrawable()).getBitmap(), validationPath, paint);
        Bitmap mMaskShadowBitmap = mMaskBitmap.extractAlpha();
        canvas.drawBitmap(mMaskShadowBitmap, offsetX - startX + circleSize / 2, 0, mMaskShadowPaint);
        canvas.drawBitmap(mMaskBitmap, offsetX - startX + circleSize / 2, 0, null);
    }

    private Bitmap getMaskBitmap(Bitmap mBitmap, Path mask, Paint mMaskPaint) {
        Bitmap tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(tempBitmap);
        mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        mCanvas.drawPath(mask, mMaskPaint);
        mMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        mCanvas.drawBitmap(mBitmap, getImageMatrix(), mMaskPaint);
        mMaskPaint.setXfermode(null);
        return tempBitmap;
    }

    /**
     * set
     *
     * @param howMuch show as %
     */
    public void setOffsetX(float howMuch) {
        offsetX = (width - validationSize - circleSize) / 100f * howMuch;
        invalidate();
    }

    /**
     * 重置验证区域位置（重新生成）
     */
    public void restore() {
        creatValidationPath();
        offsetX = 0;
        animaOffsetX = 0;
        success = false;
        invalidate();
    }

    /**
     * see if the validation is success or not.
     */
    public void deal() {
        if (offsetX + circleSize / 2 <= startX + dp2px(mContext, 5) && offsetX + circleSize / 2 >= startX - dp2px(mContext, 5)) {
            if (mListener != null) {
                mListener.onSuccess();
            }
            success = true;
            invalidate();
        } else {
            if (mListener != null) {
                mListener.onFail();
            }
            setOffsetX(0);
        }
    }
    //Set listener to see if the check is success or not
    public void setListener(SlideListener listener) {
        mListener = listener;
    }

    /**
     * change dp to px
     */
    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5F);
    }

    /**
     * change from px to dp, based on mobile resolution.
     */
    public static int px2dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5F);
    }
}
