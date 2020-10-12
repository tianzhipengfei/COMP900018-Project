package com.example.group_w01_07_3.features.discover;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MotionEvent;
import android.widget.PopupWindow;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.group_w01_07_3.R;

class PopUpTest extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }
    public void popupWindow(View view){
        LayoutInflater inflater=(LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupview=inflater.inflate(R.layout.popup_window_layout,null);
        int width=20;
        int height=20;
        boolean focus=true;
        PopupWindow popupWindow=new PopupWindow(popupview,width,height,focus);
        popupWindow.showAtLocation(view, Gravity.CENTER,0,0);
        popupview.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Float x1 = null,x2,y1=null,y2;
                long t1 = 0,t2;
                long click_duration=5;
                float centerX=view.getX()+view.getWidth()/2;
                float centerY=view.getY()+view.getHeight()/2;
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        x1=motionEvent.getX();
                        y1=motionEvent.getY();
                        t1=System.currentTimeMillis();
                    case MotionEvent.ACTION_UP:
                        x2=motionEvent.getX();
                        y2=motionEvent.getY();
                        t2=System.currentTimeMillis();
                        if((x1==x2)&&(y1==y2)&&(t2-t1)<click_duration){
                            //handle click of user
                            Toast.makeText(getApplicationContext(), "User click", Toast.LENGTH_SHORT).show();
                            return true;
                        }else if((t2-t1)>=click_duration){
                            //handle long click by user
                            Toast.makeText(getApplicationContext(),"Long tap",Toast.LENGTH_SHORT).show();
                            return false;
                        }else if(x2>x1&&x1<centerX&&x2>centerX&&y1<centerY&&y2>centerY){
                            Toast.makeText(getApplicationContext(),"Valid move! Across center",Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + motionEvent.getAction());
                }
                return false;
            }
        }
    );
    }
}
