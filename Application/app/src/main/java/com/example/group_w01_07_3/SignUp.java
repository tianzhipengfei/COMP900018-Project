package com.example.group_w01_07_3;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.group_w01_07_3.util.DensityUtil;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.ImageUtil;
import com.example.group_w01_07_3.util.MessageUtil;
import com.example.group_w01_07_3.widget.BottomDialog;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.snackbar.Snackbar;
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

public class SignUp extends AppCompatActivity {

    TextView backToSignInText;
    ImageView sideBackShape;
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
    private ConstraintLayout constraintLayout;
    // message section
    private Toast toast = null;
    private Snackbar snackbar = null;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        constraintLayout = findViewById(R.id.sign_up_mega_layout);

        //don't pop uo keyboard when entering the screen.
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

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

        ImageView helpImageButton = (ImageView) findViewById(R.id.imageButton_sign_up_question);
        helpImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String guideline = getString(R.string.registration_help);
                AlertDialog dialog = new AlertDialog.Builder(SignUp.this)
                        .setIcon(R.drawable.sign_up_rules)
                        .setTitle("Account Registration Guideline")
                        .setMessage(guideline)
                        .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
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

                // Preventing multiple clicks, using threshold of 2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                signUpButton.setEnabled(false);

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

                if (username.isEmpty() || !isValidUsername(username)) {
                    usernameWrapper.setError("Not a valid Username");
                } else {
                    usernameWrapper.setErrorEnabled(false);
                }
                if (email.isEmpty() || !isValidEmail(email)) {
                    emailWrapper.setError("Not a valid Email Address");
                } else {
                    emailWrapper.setErrorEnabled(false);
                }
                if (password.isEmpty() || !isValidPassword(password)) {
                    passwordWrapper.setError("Not a valid Password");
                } else {
                    passwordWrapper.setErrorEnabled(false);
                }
                if (reEnterPassword.isEmpty() || !password.equals(reEnterPassword)) {
                    rePasswordWrapper.setError(("Re-enter Password does not match with previous input"));
                } else {
                    rePasswordWrapper.setErrorEnabled(false);
                }

                if (allRequiredFinished(username, email, password, reEnterPassword, dob)) {
                    boolean internetFlag = HttpUtil.isNetworkConnected(getApplicationContext());
                    if (!internetFlag) {
                        Snackbar snackbar = Snackbar
                                .make(constraintLayout, "Oops. Looks like you lost Internet connection\n Please connect to Internet and try again...", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        signUpButton.setEnabled(true);
                        return;
                    }
                    Log.d("SIGNUP", "username: " + username);
                    Log.d("SIGNUP", "email: " + email);
                    Log.d("SIGNUP", "password: " + password);
                    Log.d("SIGNUP", "reEnterPassword: " + reEnterPassword);
                    Log.d("SIGNUP", "dob: " + dob);
                    if (avatarFile != null) {
                        internetFlag = HttpUtil.isNetworkConnected(getApplicationContext());
                        if (!internetFlag) {
                            Snackbar snackbar = Snackbar
                                    .make(constraintLayout, "Oops. Looks like you lost Internet connection\n Please connect to Internet and try again...", Snackbar.LENGTH_LONG);
                            snackbar.show();
                            signUpButton.setEnabled(true);
                            return;
                        }
                        HttpUtil.uploadAvatar(username, avatarFile, new okhttp3.Callback() {
                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                Log.d("SIGNUP", "***** uploadAvatar onResponse *****");
                                String responseData = response.body().string();
                                Log.d("SIGNUP", "uploadAvatar: " + responseData);
                                try {
                                    JSONObject responseJSON = new JSONObject(responseData);
                                    String code = responseJSON.getString("code");
                                    if (code.equalsIgnoreCase("success")) {
                                        Log.d("SIGNUP", "uploadAvatar success: " + code);
                                        JSONObject data = responseJSON.getJSONObject("data");
                                        avatarFileLink = data.getString("url");
                                        Log.d("SIGNUP", "avatarFileLink: " + avatarFileLink);
                                        onSignUp();
                                    } else if (code.equalsIgnoreCase("image_repeated")) {
                                        Log.d("SIGNUP", "uploadAvatar success: " + code);
                                        avatarFileLink = responseJSON.getString("images");
                                        Log.d("SIGNUP", "avatarFileLink: " + avatarFileLink);
                                        onSignUp();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                e.printStackTrace();
                                Snackbar snackbar = Snackbar
                                        .make(constraintLayout, "Upload avatar timeout, please check your Internet and try again", Snackbar.LENGTH_LONG);
                                snackbar.show();
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

        backToSignInText = (TextView) findViewById(R.id.text_back_sign_in);
        backToSignInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        sideBackShape = (ImageView) findViewById(R.id.sign_up_side_back);
        sideBackShape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private boolean allRequiredFinished(String username, String email, String password, String reEnterPassword, String dob) {
        if (username.isEmpty()) {
            signUpButton.setEnabled(true);
            return false;
        } else if (email.isEmpty()) {
            signUpButton.setEnabled(true);
            return false;
        } else if (password.isEmpty()) {
            signUpButton.setEnabled(true);
            return false;
        } else if (reEnterPassword.isEmpty()) {
            signUpButton.setEnabled(true);
            return false;
        } else if (dob.equalsIgnoreCase("Select Birthday")) {
            MessageUtil.displayToast(SignUp.this, "Date of Birth is required", Toast.LENGTH_SHORT);
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
            signUpButton.setEnabled(true);
            return false;
        }

        return true;
    }

    private boolean isValidUsername(String username) {
        if (username.length() < 3 || username.length() > 20) {
            //Toast.makeText(SignUp.this, "3 <= the length of Username <= 20", Toast.LENGTH_SHORT).show();
            return false;
        }

        Pattern p = Pattern.compile("[a-z][0-9a-z_]{2,}");
        Matcher m = p.matcher(username);
        if (!m.matches()) {
            //Toast.makeText(SignUp.this, "Username: alphanumeric_, starts with alpha, not case sensitive", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        Pattern p = Pattern.compile("\\w+@\\w+(\\.\\w+)+");
        Matcher m = p.matcher(email);
        if (!m.matches()) {
            //Toast.makeText(SignUp.this, "Check your Email Address format", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isValidPassword(String password) {
        Pattern p = Pattern.compile("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[\\.#?!@$%^&*-_=+]).{8,}$");
        Matcher m = p.matcher(password);
        if (!m.matches()) {
            //Toast.makeText(SignUp.this, "Invalid Password", Toast.LENGTH_SHORT).show(); // remind user
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
                        MessageUtil.displayToast(this, "Take the photo successfully", Toast.LENGTH_SHORT);
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    MessageUtil.displayToast(this, "You denied the permission", Toast.LENGTH_SHORT);
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
            MessageUtil.displayToast(this, "Select the image successfully", Toast.LENGTH_SHORT);
        } else {
            MessageUtil.displayToast(this, "Failed to get image", Toast.LENGTH_SHORT);
        }
    }

    private void onSignUp() {
        backToSignInText.setEnabled(false);
        sideBackShape.setEnabled(false);
        avatarImageBtn.setEnabled(false);
        boolean internetFlag = HttpUtil.isNetworkConnected(getApplicationContext());
        if (!internetFlag) {
            displaySnackbar(constraintLayout, "Oops. Looks like you lost Internet connection\n Please connect to Internet and try again...", Snackbar.LENGTH_LONG);
            signUpButton.setEnabled(true);
            backToSignInText.setEnabled(true);
            sideBackShape.setEnabled(true);
            avatarImageBtn.setEnabled(true);
            return;
        }
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
                                              MessageUtil.displayToast(SignUp.this, "Sign up successfully", Toast.LENGTH_SHORT);
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
                                              MessageUtil.displayToast(SignUp.this, "Username or Email Address exists", Toast.LENGTH_SHORT);
                                              signUpButton.setEnabled(true);
                                              backToSignInText.setEnabled(true);
                                              sideBackShape.setEnabled(true);
                                              avatarImageBtn.setEnabled(true);
                                          }
                                      }
                        );
                    } else {
                        Log.d("SIGNUP", "signUp: Invalid form");
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              MessageUtil.displayToast(SignUp.this, "Invalid form, please try again later", Toast.LENGTH_SHORT);
                                              signUpButton.setEnabled(true);
                                              backToSignInText.setEnabled(true);
                                              sideBackShape.setEnabled(true);
                                              avatarImageBtn.setEnabled(true);
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displaySnackbar(constraintLayout, "Sign up timeout, please check your Internet and try again", Snackbar.LENGTH_LONG);
                        signUpButton.setEnabled(true);
                        backToSignInText.setEnabled(true);
                        sideBackShape.setEnabled(true);
                        avatarImageBtn.setEnabled(true);
                    }
                });
                return;
            }
        });
    }

    /**
     * Display snackbar in a non-overlap manner
     *
     * @param view   view where snackbar will display at
     * @param msg    the message to display
     * @param length the duration of snackbar display
     */
    private void displaySnackbar(View view, String msg, int length) {
        if (snackbar == null || !snackbar.getView().isShown()) {
            snackbar = Snackbar.make(view, msg, length);
            snackbar.show();
        }
    }

    @Override
    public void onBackPressed() {
        // go back to sign in, as it is single instance, we are not creating a new one
        finish();
        startActivity(new Intent(SignUp.this, SignIn.class));

        //(destination, origion)
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

}