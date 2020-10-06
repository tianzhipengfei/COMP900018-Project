package com.example.group_w01_07_3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.progressindicator.ProgressIndicator;

public class SignUp extends AppCompatActivity {

    private MaterialDatePicker<Long> picker;
    private Button dobPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //implemented Material Date Picker, with constrained set with bound [1920/1/1, today]
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Please Select Your Birthday");

        Calendar cStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cStart.set(1920, 1, 1);

        Calendar cEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cEnd.set(cEnd.get(Calendar.YEAR), cEnd.get(Calendar.MONTH),cEnd.get(Calendar.DATE));
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();

        constraintsBuilder.setStart(cStart.getTimeInMillis());
        constraintsBuilder.setEnd(cEnd.getTimeInMillis());

        builder.setCalendarConstraints(constraintsBuilder.build());

        picker = builder.build();

        dobPicker = (Button) findViewById(R.id.button_sign_up_birthday);
        dobPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picker.show(getSupportFragmentManager(), picker.toString());
            }
        });

        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                dobPicker.setText(picker.getHeaderText());
            }
        });

        Button signUpButton = (Button) findViewById(R.id.button_sign_up);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressIndicator progress = (ProgressIndicator) findViewById(R.id.progressCircleDeterminate_signup);
                progress.show();
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                View pageLayout = findViewById(R.id.sign_up_mega_layout);
                View root = pageLayout.getRootView();
                root.setBackgroundColor(ContextCompat.getColor(SignUp.this, R.color.colorGreyOut));

                //Replace this part to login successful or fail, which hide progress bar and display message
                //will change background to indicate the process
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something here
                        ProgressIndicator progress = (ProgressIndicator) findViewById(R.id.progressCircleDeterminate_signup);
                        progress.hide();
                        View pageLayout = findViewById(R.id.sign_up_mega_layout);
                        View root = pageLayout.getRootView();
                        root.setBackgroundColor(ContextCompat.getColor(SignUp.this, R.color.colorResetWhite));
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        Toast.makeText(SignUp.this, "checked state and implement logic accordingly", Toast.LENGTH_SHORT).show();
                    }
                }, 3000);
            }
        });

        ImageView avatarImageBtn = (ImageView) findViewById(R.id.sign_up_avatar);
        avatarImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        TextView backToSignInText = (TextView) findViewById(R.id.text_back_sign_in);
        backToSignInText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUp.this, SignIn.class);

                SignUp.super.finish();
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

}