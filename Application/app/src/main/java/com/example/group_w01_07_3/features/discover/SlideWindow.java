package com.example.group_w01_07_3.features.discover;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.example.group_w01_07_3.R;
import com.luozm.captcha.Captcha;
import com.lxj.xpopup.animator.PopupAnimator;
import com.lxj.xpopup.core.CenterPopupView;

public class SlideWindow extends CenterPopupView {
    private Captcha captcha;
    private Context mContext;
    public SlideWindow(@NonNull Context context) {
        super(context);
        mContext = context;
    }
    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.layout_popupwindow;
    }
    @Override
    protected void onCreate() {
        super.onCreate();
        captcha = (Captcha) findViewById(R.id.captCha);
//        captcha.setMaxFailedCount(10);
        captcha.setCaptchaListener(new Captcha.CaptchaListener() {
            @Override
            public String onAccess(long time) {
                Toast.makeText(mContext,"Success! Capsule will open. ",Toast.LENGTH_SHORT).show();
                dismiss();
                return "Pass,Take "+time+"s";
            }

            @Override
            public String onFailed(int failedCount) {
                Toast.makeText(mContext,"Fail! Please try again",Toast.LENGTH_SHORT).show();
                return "Failed, has failed "+failedCount+"times";
            }

            @Override
            public String onMaxFailed() {
                return "Failed";
            }
        });
    }
    // the maximum width
    @Override
    protected int getMaxWidth() {
        return super.getMaxWidth();
    }
    // the maximum height
    @Override
    protected int getMaxHeight() {
        return super.getMaxHeight();
    }
    // animation
    @Override
    protected PopupAnimator getPopupAnimator() {
        return super.getPopupAnimator();
    }
    /**
     * width of pop-up window
     *
     * @return
     */
    protected int getPopupWidth() {
        return 0;
    }

    /**
     * the height of maximum
     *
     * @return
     */
    protected int getPopupHeight() {
        return 0;
    }
}
/**
 * at main activity, use
 * BasePopupView popup=new XPopup.Builder(this)
 *                 .asCustom(new PopupWindow(this))
 *                 .show();
 */
