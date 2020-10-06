package com.example.group_w01_07_3;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.ProgressIndicator;

public class SignIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //先暂时不做这个
//        Button sendUsernameToEmailButton = (Button) findViewById(R.id.button_send_username_to_email);
//        sendUsernameToEmailButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(SignIn.this);
//                builder.setIcon(R.drawable.email);
//                builder.setTitle("Please enter your email:");
//                View v = LayoutInflater.from(SignIn.this).inflate(R.layout.dialog, null);
//                builder.setView(v);
//
//                final EditText email = (EditText) v.findViewById(R.id.edittext_send_username_to_email);
//
//                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        String e = email.getText().toString().trim();
//                        Toast.makeText(SignIn.this, "Send username to email successfully!", Toast.LENGTH_SHORT).show();
//                    }
//                });
//                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                });
//                builder.show();
//            }
//        });

        Button signInButton = (Button) findViewById(R.id.button_sign_in);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressIndicator progress = (ProgressIndicator) findViewById(R.id.progressCircleDeterminate_signin);
                progress.show();
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                View pageLayout = findViewById(R.id.sign_in_mega_layout);
                View root = pageLayout.getRootView();
                root.setBackgroundColor(ContextCompat.getColor(SignIn.this, R.color.colorGreyOut));

                //Replace this part to login successful or fail, which hide progress bar and display message
                //will change background to indicate the process
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something here
                        ProgressIndicator progress = (ProgressIndicator) findViewById(R.id.progressCircleDeterminate_signin);
                        progress.hide();
                        View pageLayout = findViewById(R.id.sign_in_mega_layout);
                        View root = pageLayout.getRootView();
                        root.setBackgroundColor(ContextCompat.getColor(SignIn.this, R.color.colorResetWhite));
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                }, 3000);

            }
        });

        TextView createAccountButton = (TextView) findViewById(R.id.text_create_account);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                //kill SinIn Activity
                finish();
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        TextView skipToAccountButton = (TextView) findViewById(R.id.text_skip_to_account);
        skipToAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, Account.class);

                //kill SinIn Activity
                finish();
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

    }
}