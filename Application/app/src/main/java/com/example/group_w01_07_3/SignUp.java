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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Response;

/**
 * SignUp Activity
 */
public class SignUp extends AppCompatActivity {

    // App view
    private ConstraintLayout constraintLayout;
    // username
    private EditText usernameET;
    private String username;
    // email address
    private EditText emailET;
    private String email;
    // password
    private EditText passwordET;
    private String password;
    // reEnter password
    private EditText reEnterPasswordET;
    private String reEnterPassword;
    // date of birth
    private MaterialDatePicker<Long> picker;
    private Button dobPicker;
    private String dob;
    // avatar
    private ImageView avatarImageBtn;
    private BottomDialog bottomDialog;
    private File avatarFile = null;
    private String avatarFileLink = null;
    // help
    private ImageView helpImageButton;
    // sign up
    private Button signUpButton;

    // back to SignIn
    private TextView backToSignInText;
    private ImageView sideBackShape;

    // message section
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initView();
    }

    /**
     * initialize the view
     */
    private void initView() {
        constraintLayout = findViewById(R.id.sign_up_mega_layout);

        //don't pop uo keyboard when entering the screen.
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        usernameET = (EditText) findViewById(R.id.edittext_sign_up_username);
        emailET = (EditText) findViewById(R.id.edittext_sign_up_email);
        passwordET = (EditText) findViewById(R.id.edittext_sign_up_password);
        reEnterPasswordET = (EditText) findViewById(R.id.edittext_sign_up_re_enter_password);

        initDOBPicker(); // dob picker
        initHelpButton(); // help button
        initAvatarButton(); // avatar button
        initSignUpButton(); // sign up button
        initJumpToSignInPage(); // jump to sign in page
    }

    /**
     * initialize dob picker
     */
    private void initDOBPicker() {
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
                dobPicker.setEnabled(false); // prevent crazy clicks
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
    }

    /**
     * initialize help button
     */
    private void initHelpButton() { // help button is for the user to see the sign up requirements
        helpImageButton = (ImageView) findViewById(R.id.imageButton_sign_up_question);
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
    }

    /**
     * initialize avatar button
     */
    private void initAvatarButton() {
        avatarImageBtn = (ImageView) findViewById(R.id.sign_up_avatar);
        avatarImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // BottomDialog: take a photo, choose a photo from the gallery, cancel
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
    }

    /**
     * initialize sign up button
     */
    private void initSignUpButton() {
        signUpButton = (Button) findViewById(R.id.button_sign_up);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Preventing multiple clicks, using threshold of 2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                setAllEnabledFalse();

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

                if (username.isEmpty() || !isValidUsername(username)) { // check username
                    usernameWrapper.setError("Not a valid Username");
                } else {
                    usernameWrapper.setErrorEnabled(false);
                }
                if (email.isEmpty() || !isValidEmail(email)) { // check email address
                    emailWrapper.setError("Not a valid Email Address");
                } else {
                    emailWrapper.setErrorEnabled(false);
                }
                if (password.isEmpty() || !isValidPassword(password)) { // check password
                    passwordWrapper.setError("Not a valid Password");
                } else {
                    passwordWrapper.setErrorEnabled(false);
                }
                if (reEnterPassword.isEmpty() || !password.equals(reEnterPassword)) { // check reEnter password
                    rePasswordWrapper.setError(("Re-enter Password does not match with previous input"));
                } else {
                    rePasswordWrapper.setErrorEnabled(false);
                }

                if (allRequiredFinished(username, email, password, reEnterPassword, dob)) {
                    boolean internetFlag = HttpUtil.isNetworkConnected(getApplicationContext());
                    if (!internetFlag) { // check internet connection
                        Snackbar snackbar = Snackbar
                                .make(constraintLayout, "Oops. Looks like you lost Internet connection\n Please connect to Internet and try again...", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        setAllEnabledTrue();
                        return;
                    }
                    if (avatarFile != null) {
                        internetFlag = HttpUtil.isNetworkConnected(getApplicationContext());
                        if (!internetFlag) { // check internet connection
                            Snackbar snackbar = Snackbar
                                    .make(constraintLayout, "Oops. Looks like you lost Internet connection\n Please connect to Internet and try again...", Snackbar.LENGTH_LONG);
                            snackbar.show();
                            setAllEnabledTrue();
                            return;
                        }
                        onUploadAvatar();
                    } else {
                        onSignUp();
                    }
                }
            }
        });
    }

    /**
     * upload avatar logic and then sign up
     */
    private void onUploadAvatar() {
        HttpUtil.uploadAvatar(username, avatarFile, new okhttp3.Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseData = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject responseJSON = new JSONObject(responseData);
                    String code = responseJSON.getString("code");
                    if (code.equalsIgnoreCase("success")) {
                        JSONObject data = responseJSON.getJSONObject("data");
                        avatarFileLink = data.getString("url");
                        onSignUp();
                    } else if (code.equalsIgnoreCase("image_repeated")) {
                        avatarFileLink = responseJSON.getString("images");
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
    }

    /**
     * jump to sign in page
     */
    private void initJumpToSignInPage() {
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

    /**
     * check is ready to sign up or not
     *
     * @param username        username
     * @param email           email address
     * @param password        password
     * @param reEnterPassword reEnter password
     * @param dob             date of birth
     * @return ready to sign up or not
     */
    private boolean allRequiredFinished(String username, String email, String password, String reEnterPassword, String dob) {
        if (username.isEmpty()) {
            setAllEnabledTrue();
            return false;
        } else if (email.isEmpty()) {
            setAllEnabledTrue();
            return false;
        } else if (password.isEmpty()) {
            setAllEnabledTrue();
            return false;
        } else if (reEnterPassword.isEmpty()) {
            setAllEnabledTrue();
            return false;
        } else if (dob.equalsIgnoreCase("Select Birthday")) {
            MessageUtil.displaySnackbar(constraintLayout, "Sign up failed. Date of Birth is required.", Snackbar.LENGTH_SHORT);
            setAllEnabledTrue();
            return false;
        }

        if (!isValidUsername(username)) {
            setAllEnabledTrue();
            return false;
        } else if (!isValidEmail(email)) {
            setAllEnabledTrue();
            return false;
        } else if (!isValidPassword(password)) {
            setAllEnabledTrue();
            return false;
        } else if (!password.equals(reEnterPassword)) {
            setAllEnabledTrue();
            return false;
        }

        return true;
    }

    /**
     * check the username
     *
     * @param username username
     * @return the username is valid or not
     */
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

    /**
     * check the email address
     *
     * @param email email address
     * @return the email address is valid or not
     */
    private boolean isValidEmail(String email) {
        Pattern p = Pattern.compile("\\w+@\\w+(\\.\\w+)+");
        Matcher m = p.matcher(email);
        if (!m.matches()) {
            return false;
        }
        return true;
    }

    /**
     * check the password
     *
     * @param password password
     * @return the password is valid or not
     */
    private boolean isValidPassword(String password) {
        Pattern p = Pattern.compile("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[\\.#?!@$%^&*-_=+]).{8,}$");
        Matcher m = p.matcher(password);
        if (!m.matches()) {
            return false;
        }
        return true;
    }

    /**
     * return result of take photo or choose photo
     *
     * @param requestCode request code
     * @param resultCode  result code
     * @param data        intent data
     */
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

    /**
     * check permission
     *
     * @param requestCode  request code
     * @param permissions  permissions
     * @param grantResults grant results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    MessageUtil.displaySnackbar(constraintLayout, "You denied the permission.", Snackbar.LENGTH_SHORT);
                }
                break;
            default:
                break;
        }
    }

    /**
     * open album to choose a photo
     */
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, BottomDialog.CHOOSE_PHOTO);
    }

    /**
     * phone version > 4.4 process picture
     *
     * @param data intent data
     */
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

    /**
     * get image path
     *
     * @param uri       uri
     * @param selection selection
     * @return image path
     */
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

    /**
     * display image to the avatar
     *
     * @param imagePath image path
     */
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            avatarFile = ImageUtil.compressImage(SignUp.this, bitmap, "image_compressed.jpg");
            avatarImageBtn.setImageBitmap(bitmap);
            bottomDialog.dismiss();
        } else {
            MessageUtil.displaySnackbar(constraintLayout, "Failed to take the picture. Please try again", Snackbar.LENGTH_SHORT);
        }
    }

    /**
     * sign up logic
     */
    private void onSignUp() {
        boolean internetFlag = HttpUtil.isNetworkConnected(getApplicationContext());
        if (!internetFlag) {
            MessageUtil.displaySnackbar(constraintLayout, "Sign up Failed. Please turn on the internet.", Snackbar.LENGTH_LONG);
            setAllEnabledTrue();
            return;
        }
        HttpUtil.signUp(new String[]{username, password, email, dob, avatarFileLink}, new okhttp3.Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseData = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject responseJSON = new JSONObject(responseData);
                    if (responseJSON.has("success")) { // success
                        String status = responseJSON.getString("success");
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              MessageUtil.displayToast(SignUp.this, "Sign up successfully", Toast.LENGTH_SHORT);
                                              finish();
                                              overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                          }
                                      }
                        );
                    } else if (responseJSON.has("error")) { // error
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              usernameET.setText("");
                                              emailET.setText("");
                                              MessageUtil.displaySnackbar(constraintLayout, "Sign up failed. Username or Email Address already exists.", Snackbar.LENGTH_SHORT);
                                              setAllEnabledTrue();
                                          }
                                      }
                        );
                    } else {
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              MessageUtil.displaySnackbar(constraintLayout, "Sign up Failed. Please try again later.", Snackbar.LENGTH_SHORT);
                                              setAllEnabledTrue();
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
                        MessageUtil.displaySnackbar(constraintLayout, "Sign up timeout. Please check Internet connection.", Snackbar.LENGTH_LONG);
                        setAllEnabledTrue();
                    }
                });
            }
        });
    }

    /**
     * set all enabled true
     */
    private void setAllEnabledTrue() {
        signUpButton.setEnabled(true);
        backToSignInText.setEnabled(true);
        sideBackShape.setEnabled(true);
        avatarImageBtn.setEnabled(true);
        helpImageButton.setEnabled(true);
        dobPicker.setEnabled(true);
    }

    /**
     * set all enabled false
     */
    private void setAllEnabledFalse() {
        signUpButton.setEnabled(false);
        backToSignInText.setEnabled(false);
        sideBackShape.setEnabled(false);
        avatarImageBtn.setEnabled(false);
        helpImageButton.setEnabled(false);
        dobPicker.setEnabled(false);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

}