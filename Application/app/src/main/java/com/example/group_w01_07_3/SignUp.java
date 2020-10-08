package com.example.group_w01_07_3;

import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group_w01_07_3.util.DensityUtil;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.ImageUtil;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Response;

/*
1. dob不能是今天以后的日期
2. 3 <= username <= 20，只能由字母数字_组成，只能以字母开头，字母不区分大小写,不能重复
3. email满足基本格式
4. password至少8个字符，至少1个大写字母，1个小写字母，1个数字和1个特殊字符
5. 处理疯狂点击的问题：dobPicker（解决）
6. 处理没网的情况
*/

public class SignUp extends AppCompatActivity {

    private MaterialDatePicker<Long> picker;
    private Button dobPicker;
    private String dob;

    private EditText usernameET;
    private String username;

    private EditText emailET;
    private String email;

    private EditText passwordET;
    private String password;

    private EditText reEnterPasswordET;
    private String reEnterPassword;

    private ImageView avatarImageBtn;
    private BottomDialog bottomDialog;
    private Button signUpButton;

    private File avatarFile = null;
    private String avatarFileLink = null;

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
        cEnd.set(cEnd.get(Calendar.YEAR), cEnd.get(Calendar.MONTH), cEnd.get(Calendar.DATE));
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();

        constraintsBuilder.setStart(cStart.getTimeInMillis());
        constraintsBuilder.setEnd(cEnd.getTimeInMillis());

        builder.setCalendarConstraints(constraintsBuilder.build());

        picker = builder.build();

        dobPicker = (Button) findViewById(R.id.button_sign_up_birthday);
        dobPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dobPicker.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dobPicker.setEnabled(true);
                            }
                        });
                    }
                }).start();
                picker.show(getSupportFragmentManager(), picker.toString());
            }
        });

        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                dobPicker.setText(picker.getHeaderText());
            }
        });

        usernameET = (EditText) findViewById(R.id.edittext_sign_up_username);
        emailET = (EditText) findViewById(R.id.edittext_sign_up_email);
        passwordET = (EditText) findViewById(R.id.edittext_sign_up_password);
        reEnterPasswordET = (EditText) findViewById(R.id.edittext_sign_up_re_enter_password);

        signUpButton = (Button) findViewById(R.id.button_sign_up);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpButton.setEnabled(false);

//                ProgressIndicator progress = (ProgressIndicator) findViewById(R.id.progressCircleDeterminate_signup);
//                progress.show();
//                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//                View pageLayout = findViewById(R.id.sign_up_mega_layout);
//                View root = pageLayout.getRootView();
//                root.setBackgroundColor(ContextCompat.getColor(SignUp.this, R.color.colorGreyOut));
//
//                //Replace this part to login successful or fail, which hide progress bar and display message
//                //will change background to indicate the process
//                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        //Do something here
//                        ProgressIndicator progress = (ProgressIndicator) findViewById(R.id.progressCircleDeterminate_signup);
//                        progress.hide();
//                        View pageLayout = findViewById(R.id.sign_up_mega_layout);
//                        View root = pageLayout.getRootView();
//                        root.setBackgroundColor(ContextCompat.getColor(SignUp.this, R.color.colorResetWhite));
//                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//
//                        Toast.makeText(SignUp.this, "checked state and implement logic accordingly", Toast.LENGTH_SHORT).show();
//                    }
//                }, 3000);

                username = usernameET.getText().toString().toLowerCase();
                email = emailET.getText().toString();
                password = passwordET.getText().toString();
                reEnterPassword = reEnterPasswordET.getText().toString();
                dob = dobPicker.getText().toString();

                //This part is validate input and set error message if any error occurred
                TextInputLayout usernameWrapper = (TextInputLayout) findViewById(R.id.sign_up_username_input_layout);
                TextInputLayout emailWrapper = (TextInputLayout) findViewById(R.id.sign_up_email_input_layout);
                TextInputLayout passwordWrapper = (TextInputLayout) findViewById(R.id.sign_up_password_input_layout);
                TextInputLayout rePasswordWrapper = (TextInputLayout) findViewById(R.id.sign_up_reenter_password_input_layout);

                if (!isValidUsername(username)){
                    usernameWrapper.setError("Not a valid username"); //请更改下这个error message,尽量简短
                }
                else {
                    usernameWrapper.setErrorEnabled(false);
                }
                if (!isValidEmail(email)){
                    emailWrapper.setError("Not a valid email address");//请更改下这个error message,尽量简短
                }
                else{
                    emailWrapper.setErrorEnabled(false);
                }
                if (!isValidPassword(password)){
                    passwordWrapper.setError("Not a valid password");//请更改下这个error message,尽量简短
                }
                else {
                    passwordWrapper.setErrorEnabled(false);
                }
                if (!password.equals(reEnterPassword)) {
                    rePasswordWrapper.setError(("Password does not match with previous input"));//请更改下这个error message,尽量简短
                }
                else {
                    rePasswordWrapper.setErrorEnabled(false);
                }

                if (allRequiredFinished(username, email, password, reEnterPassword, dob)) {
                    Log.d("SIGNUP", "username: " + username);
                    Log.d("SIGNUP", "email: " + email);
                    Log.d("SIGNUP", "password: " + password);
                    Log.d("SIGNUP", "reEnterPassword: " + reEnterPassword);
                    Log.d("SIGNUP", "dob: " + dob);
                    if (avatarFile != null) {
                        HttpUtil.uploadAvatar(username, avatarFile, new okhttp3.Callback() {
                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                Log.d("SIGNUP", "***** uploadAvatar onResponse *****");
                                String responseData = response.body().string();
                                Log.d("SIGNUP", "uploadAvatar: " + responseData);
                                try {
                                    JSONObject responseJSON = new JSONObject(responseData);
                                    if (responseJSON.has("success")) {
                                        String status = responseJSON.getString("success");
                                        Log.d("SIGNUP", "uploadAvatar success: " + status);
                                        avatarFileLink = responseJSON.getString("file");
                                        Log.d("SIGNUP", "avatarFileLink: " + avatarFileLink);
                                        onSignUp();
                                    } else if (responseJSON.has("error")) {
                                        String status = responseJSON.getString("error");
                                        Log.d("SIGNUP", "uploadAvatar error: " + status);
                                        if (status.equalsIgnoreCase("userExist - user already exist")) {
                                            runOnUiThread(new Runnable() {
                                                              @Override
                                                              public void run() {
                                                                  usernameET.setText("");
                                                                  Toast.makeText(SignUp.this, "Username exists, please change it", Toast.LENGTH_SHORT).show();
                                                                  signUpButton.setEnabled(true);
                                                              }
                                                          }
                                            );
                                        } else if (status.equalsIgnoreCase("Invalid format")) {
                                            Log.d("SIGNUP", "uploadAvatar error: " + status);
                                            runOnUiThread(new Runnable() {
                                                              @Override
                                                              public void run() {
                                                                  Toast.makeText(SignUp.this, "Invalid form, please try again later", Toast.LENGTH_SHORT).show();
                                                                  signUpButton.setEnabled(true);
                                                              }
                                                          }
                                            );
                                        }
                                    } else {
                                        Log.d("SIGNUP", "uploadAvatar: Invalid form");
                                        runOnUiThread(new Runnable() {
                                                          @Override
                                                          public void run() {
                                                              Toast.makeText(SignUp.this, "Invalid form, please try again later", Toast.LENGTH_SHORT).show();
                                                              signUpButton.setEnabled(true);
                                                          }
                                                      }
                                        );
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                e.printStackTrace();
                            }
                        });
                    } else {
                        onSignUp();
                    }
                }
            }
        });

        avatarImageBtn = (ImageView) findViewById(R.id.sign_up_avatar);
        avatarImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // (Modified) From: https://github.com/jianjunxiao/BottomDialog
                bottomDialog = new BottomDialog(SignUp.this);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) bottomDialog.getContentView().getLayoutParams();
                params.width = getResources().getDisplayMetrics().widthPixels - DensityUtil.dp2px(SignUp.this, 16f);
                params.bottomMargin = DensityUtil.dp2px(SignUp.this, 8f);
                bottomDialog.getContentView().setLayoutParams(params);
                bottomDialog.setCanceledOnTouchOutside(true);
                bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
                bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
                bottomDialog.show();
            }
        });

        TextView backToSignInText = (TextView) findViewById(R.id.text_back_sign_in);
        backToSignInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private boolean allRequiredFinished(String username, String email, String password, String reEnterPassword, String dob) {
        if (username.isEmpty()) {
            Toast.makeText(SignUp.this, "Username is required", Toast.LENGTH_SHORT).show();
            signUpButton.setEnabled(true);
            return false;
        } else if (email.isEmpty()) {
            Toast.makeText(SignUp.this, "Email Address is required", Toast.LENGTH_SHORT).show();
            signUpButton.setEnabled(true);
            return false;
        } else if (password.isEmpty()) {
            Toast.makeText(SignUp.this, "Password is required", Toast.LENGTH_SHORT).show();
            signUpButton.setEnabled(true);
            return false;
        } else if (reEnterPassword.isEmpty()) {
            Toast.makeText(SignUp.this, "Re-enter Password is required", Toast.LENGTH_SHORT).show();
            signUpButton.setEnabled(true);
            return false;
        } else if (dob.equalsIgnoreCase("Select Birthday")) {
            Toast.makeText(SignUp.this, "Date of Birth is required", Toast.LENGTH_SHORT).show();
            signUpButton.setEnabled(true);
            return false;
        }

        if (!isValidUsername(username)) {
            signUpButton.setEnabled(true);
            return false;
        } else if (!isValidEmail(email)) {
            signUpButton.setEnabled(true);
            return false;
        } else if (!isValidPassword(password)) {
            signUpButton.setEnabled(true);
            return false;
        } else if (!password.equals(reEnterPassword)) {
            Toast.makeText(SignUp.this, "Re-enter Password does not match Password", Toast.LENGTH_SHORT).show();
            signUpButton.setEnabled(true);
            return false;
        }

        return true;
    }

    private boolean isValidUsername(String username) {
        if (username.length() < 3 || username.length() > 20) {
            Toast.makeText(SignUp.this, "3 <= the length of Username <= 20", Toast.LENGTH_SHORT).show();
            return false;
        }

        Pattern p = Pattern.compile("[a-z][0-9a-z_]{2,}");
        Matcher m = p.matcher(username);
        if (!m.matches()) {
            Toast.makeText(SignUp.this, "Username: alphanumeric_, starts with alpha, not case sensitive", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        Pattern p = Pattern.compile("\\w+@\\w+(\\.\\w+)+");
        Matcher m = p.matcher(email);
        if (!m.matches()) {
            Toast.makeText(SignUp.this, "Check your Email Address format", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isValidPassword(String password) {
        Pattern p = Pattern.compile("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[\\.#?!@$%^&*-]).{8,}$");
        Matcher m = p.matcher(password);
        if (!m.matches()) {
            Toast.makeText(SignUp.this, "Invalid Password", Toast.LENGTH_SHORT).show(); // remind user
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BottomDialog.TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(BottomDialog.imageUri));
                        avatarImageBtn.setImageBitmap(bitmap);
                        avatarFile = ImageUtil.compressImage(SignUp.this, bitmap, "output_photo_compressed.jpg");
                        bottomDialog.dismiss();
                        Toast.makeText(this, "Take the photo successfully", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case BottomDialog.CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    handleImageOnKitKat(data);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, BottomDialog.CHOOSE_PHOTO);
    }

    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            avatarFile = ImageUtil.compressImage(SignUp.this, bitmap, "image_compressed.jpg");
            avatarImageBtn.setImageBitmap(bitmap);
            bottomDialog.dismiss();
            Toast.makeText(this, "Select the image successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    private void onSignUp() {
        HttpUtil.signUp(new String[]{username, password, email, dob, avatarFileLink}, new okhttp3.Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.d("SIGNUP", "***** signUp onResponse *****");
                String responseData = response.body().string();
                Log.d("SIGNUP", "signUp: " + responseData);
                try {
                    JSONObject responseJSON = new JSONObject(responseData);
                    if (responseJSON.has("success")) {
                        String status = responseJSON.getString("success");
                        Log.d("SIGNUP", "signUp success: " + status);
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              Toast.makeText(SignUp.this, "Sign up successfully", Toast.LENGTH_SHORT).show();
                                              finish();
                                              overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                          }
                                      }
                        );
                    } else if (responseJSON.has("error")) {
                        String status = responseJSON.getString("error");
                        Log.d("SIGNUP", "signUp error: " + status);
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              usernameET.setText("");
                                              emailET.setText("");
                                              Toast.makeText(SignUp.this, "Username or Email Address exists", Toast.LENGTH_SHORT).show();
                                              signUpButton.setEnabled(true);
                                          }
                                      }
                        );
                    } else {
                        Log.d("SIGNUP", "signUp: Invalid form");
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              Toast.makeText(SignUp.this, "Invalid form, please try again later", Toast.LENGTH_SHORT).show();
                                              signUpButton.setEnabled(true);
                                          }
                                      }
                        );
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // your code.
        // do what you want to do when the "back" button is pressed.
        finish();
        startActivity(new Intent(SignUp.this, SignIn.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}