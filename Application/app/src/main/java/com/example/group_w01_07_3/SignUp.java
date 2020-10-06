package com.example.group_w01_07_3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.progressindicator.ProgressIndicator;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class SignUp extends AppCompatActivity {

    private MaterialDatePicker<Long> picker;
    private Button dobPicker;
    private EditText usernameE;
    private EditText emailE;
    private EditText passwordE;
    private EditText reEnterPasswordE;

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

        usernameE = (EditText) findViewById(R.id.edittext_sign_up_username);
        emailE = (EditText) findViewById(R.id.edittext_sign_up_email);
        passwordE = (EditText) findViewById(R.id.edittext_sign_up_password);
        reEnterPasswordE = (EditText) findViewById(R.id.edittext_sign_up_re_enter_password);

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

                String username = (String) usernameE.getText().toString();
                Log.d("SIGNUP", "username: " + username);
                String email = (String) emailE.getText().toString();
                Log.d("SIGNUP", "email: " + email);
                String password = (String) passwordE.getText().toString();
                Log.d("SIGNUP", "password: " + password);
                String reEnterPassword = (String) reEnterPasswordE.getText().toString();
                Log.d("SIGNUP", "reEnterPassword: " + reEnterPassword);
                String dob = (String) dobPicker.getText();
                Log.d("SIGNUP", "dob: " + dob);

                HttpUtil.signUp(new String[] {"c", "c", "c@c.c", "2020-10-06", null}, new okhttp3.Callback() {
                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String responseData = response.body().string();
                        Log.d("SIGNUP", responseData);
                    }

                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {

                    }
                });
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