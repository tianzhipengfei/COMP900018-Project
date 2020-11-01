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
/*
Puzzle for slider pop-up window
 */

public class SlideValidationView extends androidx.appcompat.widget.AppCompatImageView {

    /**
     *Constructor for puzzle
     * @param context global information about slide validation view
     */
    public SlideValidationView(Context context) {
        super(context);
        init(context);
    }

    /**
     *Constructor of puzzle, accept special attributes to change style of puzzle
     * @param context global information about puzzle
     * @param attrs  attibutes about style of puzzle
     */
    public SlideValidationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray=context.obtainStyledAttributes(attrs, R.styleable.SlideValidationView);
        validationSize = (int) typedArray.getDimension(R.styleable.SlideValidationView_validationSize, 0);
        typedArray.recycle();
        init(context);
    }

    /**
     * set context value to a class variable
     * @param context global information about puzzle
     */
    private void init(Context context) {
        this.mContext = context;
    }
    Context mContext;
    /*
    width and height of thumb of seekbar
     */
    int width = 0;
    int height = 0;
    //size of validation area
    int validationSize = 0;
    int circleSize = 0;
    Path validationPath;
    //the offset of the slider
    float offsetX = 0;
    int animaOffsetX = 0;
    //the starting point of slider
    int startX;
    int startY;
    boolean first = true;
    boolean success = false;
    SlideListener mListener;

    /**
     * Customs  the view of puzzle.
     * @param widthMeasureSpec customized  width of puzzle
     * @param heightMeasureSpec customized height of puzzle
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     *Assign a size and position to all of children of slide validation view.
     * @param changed a new size or position for this view
     * @param left Left position, relative to parent
     * @param top Top position, relative to parent
     * @param right Right position, relative to parent
     * @param bottom Bottom position, relative to parent
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    /**
     * Draw the shader and verification slider on the validation image view to create
     * puzzle
     * @param canvas the validation image to be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (success) {
            return;
        } else {
            //painting brush of shadow of verification slider
            Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            mPaint.setColor(0xff444444);//0x99000000
            //Set brush mask filter
            mPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.SOLID));
            //painting brush of verification slide
            Paint mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            Paint mMaskShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            mMaskShadowPaint.setColor(0xff444444);
            mMaskShadowPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.SOLID));
            if (first) {
                creatValidationPath();
                first = false;
            }
            //draw the verification slider
            canvas.drawPath(validationPath, mPaint);
            //draw the shadow of verification slide
            craeteMask(canvas, mMaskPaint, mMaskShadowPaint);
        }


    }

    /**
     * Change the size of image to produce puzzle
     * @param w new width
     * @param h new height
     * @param oldw old width
     * @param oldh old height
     */
//    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    /**
     * Create border of validation slider
     */
    private void creatValidationPath() {
        validationPath = new Path();
        //size of validation slider
        if (validationSize == 0) {
            validationSize = width/6;
        }
        circleSize = validationSize / 3;
        //staring point of the validation slider
        startX = new Random().nextInt(width - validationSize * 2 - circleSize * 2 - 10) + circleSize + validationSize + 10;
        startY = new Random().nextInt(height - validationSize - circleSize * 2) + circleSize;
        validationPath.moveTo(startX, startY);
        validationPath.lineTo(startX + circleSize, startY);
        //create the upper border of vadliation slider
        creatRandomArc(validationPath, startX + circleSize, startY, false, 0);
        validationPath.lineTo(startX + validationSize, startY);
        validationPath.lineTo(startX + validationSize, startY + circleSize);
        //create the right border of validation slider
        creatRandomArc(validationPath, startX + validationSize, startY + circleSize, true, 0);
        validationPath.lineTo(startX + validationSize, startY + validationSize);
        validationPath.lineTo(startX + circleSize * 2, startY + validationSize);
        //create the bottom border of validation slider
        creatRandomArc(validationPath, startX + circleSize, startY + validationSize, false, 1);
        validationPath.lineTo(startX, startY + validationSize);
        validationPath.lineTo(startX, startY + circleSize * 2);
        //create the left border of validation slider
        creatRandomArc(validationPath, startX, startY + circleSize, true, 1);
        validationPath.lineTo(startX, startY);
    }

    /**
     * add Semicircle effect of border of the verification slider
     * @param validationPath the border of verficatrion slide
     * @param beginX x value of starting point
     * @param beginY y value of starintg point
     * @param isleftRight true if the border is left or right border
     * @param type 0 if right corner, 1 if left corner
     */
    private void creatRandomArc(Path validationPath, int beginX, int beginY, boolean isleftRight, int type) {
        RectF rectF;
        //decide if the border is right or left border to add different semicircle effect
        if (isleftRight) {
            rectF = new RectF(beginX - circleSize / 2, beginY, beginX + circleSize / 2, beginY + circleSize);
        } else {
            rectF = new RectF(beginX, beginY - circleSize / 2, beginX + circleSize, beginY + circleSize / 2);
        }
        //random choose protruding semicircle or recessed semicircle
        //draw protruding semicircle
        if (new Random().nextInt(10) > 5) {
            if (isleftRight) {
                validationPath.arcTo(rectF, -90 + type * 180, 180);
            } else {
                validationPath.arcTo(rectF, -180 + type * 180, 180);
            }
            //draw recessed semicircle
        } else {
            if (isleftRight) {
                validationPath.arcTo(rectF, -90 + type * 180, -180);
            } else {
                validationPath.arcTo(rectF, -180 + type * 180, -180);
            }
        }
    }

    /**
     * draw the verification slide
     * @param canvas painting place
     * @param paint   the  brush to draw
     * @param mMaskShadowPaint the shadow of verification slide
     */
    //
    private void craeteMask(Canvas canvas, Paint paint, Paint mMaskShadowPaint) {
        Bitmap mMaskBitmap = getMaskBitmap(((BitmapDrawable) getDrawable()).getBitmap(), validationPath, paint);
        Bitmap mMaskShadowBitmap = mMaskBitmap.extractAlpha();
        canvas.drawBitmap(mMaskShadowBitmap, offsetX - startX + circleSize / 2, 0, mMaskShadowPaint);
        canvas.drawBitmap(mMaskBitmap, offsetX - startX + circleSize / 2, 0, null);
    }

    /**
     * create mask of verification slider on image
     * @param mBitmap image of puzzle
     * @param mask the border of the verification slider
     * @param mMaskPaint painter to draw the mask
     * @return
     */
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
     * Set the moving distance of the slider
     * @param howMuch show as %
     */
    public void setOffsetX(float howMuch) {
        offsetX = (width - validationSize - circleSize) / 100f * howMuch;
        invalidate();
    }

    /**
     * Reset verification area location
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
        //there is a delta value for verification to allow for deviation
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
    /**
     * Set listener to see if user movement is success or not
     * @param listener motion listener to trace the movement of seekbar
     */
    public void setListener(SlideListener listener) {
        mListener = listener;
    }

    /**
     * change dp to px, based on mobile resolution
     *  @param context global information of validation view
     *  @param dp resolution unit of image
     */
    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5F);
    }

    /**
     * change from px to dp, based on mobile resolution.
     *  @param context global information of validation view
     *  @param px resolution unit of image
     */
    public static int px2dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5F);
    }
}
