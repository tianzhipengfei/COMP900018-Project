package com.example.group_w01_07_3.features.discover;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
public class VerificationSeekBar extends AppCompatSeekBar {
    private boolean isInterception=true;

    /**
     * constructor of seekbar
     * @param context the global information of the context
     */
    public VerificationSeekBar(@NonNull Context context) {
        super(context);
    }

    /**
     * constructor of seekbar
     * @param context the global information of the context
     * @param attrs the style attribute of the capsule
     */
    public VerificationSeekBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public VerificationSeekBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * listener to the movement of thumb of the seekbar, to avoid the click rather than
     * drag, limit the bound of distance each movement
     * @param event the motion event of thumb
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            if(event.getX()<getThumb().getBounds().left||event.getX()>getThumb().getBounds().right||
                    event.getY()>getThumb().getBounds().bottom||event.getY()<getThumb().getBounds().top){
                return false;
            }
        }
        return super.onTouchEvent(event);
    }
}
