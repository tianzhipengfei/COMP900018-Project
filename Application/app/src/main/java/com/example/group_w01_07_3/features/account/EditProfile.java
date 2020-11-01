package com.example.group_w01_07_3.features.account;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.SignIn;
import com.example.group_w01_07_3.features.create.CreateCapsule;
import com.example.group_w01_07_3.features.discover.DiscoverCapsule;
import com.example.group_w01_07_3.features.history.OpenedCapsuleHistory;
import com.example.group_w01_07_3.util.DensityUtil;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.ImageUtil;
import com.example.group_w01_07_3.util.UserUtil;
import com.example.group_w01_07_3.widget.BottomDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class EditProfile extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    boolean doubleBackToExitPressedOnce = false;

    private Toolbar mToolbar;

    private DrawerLayout drawerLayout;
    View headerview;
    TextView headerUsername;
    ShapeableImageView headerAvatar;

    NavigationView navigationView;


    private BottomDialog bottomDialog;

    private ImageView avatarDisplay;
    private TextView usernameDisplay;
    private TextView emailDisplay;
    private TextView dobDisplay;


    private File newAvatarFile;
    private String newAvatarString;

    private String usernameProfileString;
    private String avatarProfileString;
    private String emailProfileString;
    private String dobProfileString;

    private Handler handler;

    private boolean snackbarShowFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set transition
        // make sure to do this before setContentView or else the app will crash
//        Window window = getWindow();
//        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
//        window.setEnterTransition(new Slide());
//        window.setExitTransition(new Slide());
//
//
        setContentView(R.layout.activity_edit_profile);

        drawerLayout = findViewById(R.id.edit_profile_drawer_layout);

        //apply alert sound
        final MediaPlayer mediaPlayer = MediaPlayer.create(EditProfile.this, R.raw.alert);

        avatarDisplay = (ImageView) findViewById(R.id.edit_profile_avatar_display);
        usernameDisplay = (TextView) findViewById(R.id.edit_profile_username_display);
        emailDisplay = (TextView) findViewById(R.id.edit_profile_email_display);
        dobDisplay = (TextView) findViewById(R.id.edit_profile_dob_display);

        //设置主Activity的toolbar, 以及初始化侧滑菜单栏
        Toolbar toolbar = findViewById(R.id.toolbar_edit_profile);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("User Profile");

        drawerLayout = findViewById(R.id.edit_profile_drawer_layout);

        //handle the hamburger menu. remember to create two strings
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //设置侧滑菜单栏
        navigationView = findViewById(R.id.nav_view_edit_profile);
        navigationView.getMenu().getItem(3).setChecked(true); //setChecked myself
        navigationView.setNavigationItemSelectedListener(this);

        //the logic to find the header, then update the username from server user profile
        headerview = navigationView.getHeaderView(0);
        headerUsername = headerview.findViewById(R.id.header_username);
        headerAvatar = headerview.findViewById(R.id.header_avatar);

        MaterialButton changePasswordBtn = (MaterialButton) findViewById(R.id.edit_profile_btn_change_password);
        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditProfile.this, ChangePassword.class);
                startActivity(intent);
//                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

        MaterialButton changeAvatarButton = (MaterialButton) findViewById(R.id.edit_profile_btn_change_avatar);
        changeAvatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // (Modified) From: https://github.com/jianjunxiao/BottomDialog
                bottomDialog = new BottomDialog(EditProfile.this);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) bottomDialog.getContentView().getLayoutParams();
                params.width = getResources().getDisplayMetrics().widthPixels - DensityUtil.dp2px(EditProfile.this, 16f);
                params.bottomMargin = DensityUtil.dp2px(EditProfile.this, 8f);
                bottomDialog.getContentView().setLayoutParams(params);
                bottomDialog.setCanceledOnTouchOutside(true);
                bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
                bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
                bottomDialog.show();
            }
        });

        final MaterialButton signOutButton = (MaterialButton) findViewById(R.id.button_acct_sign_out);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(EditProfile.this);
                builder.setIcon(R.drawable.warning);
                builder.setTitle("Sign Out Confirmation");
                builder.setMessage("Do you want to sign out?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signOutButton.setEnabled(false);
                        String token = UserUtil.getToken(EditProfile.this);
                        if (token.isEmpty()) {
                            Log.d("SIGNOUT", "Error: no token");
                            Toast.makeText(EditProfile.this, "Error: no token", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EditProfile.this, SignIn.class);
                            startActivity(intent);
                            finish();
                        } else {
                            boolean internetFlag = HttpUtil.isNetworkConnected(getApplicationContext());
                            if(!internetFlag){
                                Snackbar snackbar = Snackbar
                                        .make(drawerLayout, "Oops. Looks like you lost Internet connection\n Please connect to Internet and try again...", Snackbar.LENGTH_LONG);
                                snackbar.show();
                                signOutButton.setEnabled(true);
                                Log.d("SIGNOUT", "No Internet Connection");
                                return;
                            }
                            HttpUtil.signOut(token, new okhttp3.Callback() {
                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                    Log.d("SIGNOUT", "***** signOut onResponse *****");
                                    String responseData = response.body().string();
                                    Log.d("SIGNOUT", "signOut: " + responseData);
                                    try {
                                        JSONObject responseJSON = new JSONObject(responseData);
                                        if (responseJSON.has("success")) {
                                            String status = responseJSON.getString("success");
                                            Log.d("SIGNOUT", "signOut success: " + status);
                                            EditProfile.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    UserUtil.clearToken(EditProfile.this);
                                                    Toast.makeText(EditProfile.this, "Sign out successfully", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(EditProfile.this, SignIn.class);
                                                    startActivity(intent);
                                                    finish();
                                                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                                }
                                            });
                                        } else if (responseJSON.has("error")) {
                                            String status = responseJSON.getString("error");
                                            Log.d("SIGNOUT", "signOut error: " + status);
                                            EditProfile.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    UserUtil.clearToken(EditProfile.this);
                                                    Toast.makeText(EditProfile.this, "Not logged in", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(EditProfile.this, SignIn.class);
                                                    startActivity(intent);
                                                    finish();
                                                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                                }
                                            });
                                        } else {
                                            Log.d("SIGNOUT", "signOut: Invalid form");
                                            EditProfile.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    UserUtil.clearToken(EditProfile.this);
                                                    Toast.makeText(EditProfile.this, "Invalid form", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(EditProfile.this, SignIn.class);
                                                    startActivity(intent);
                                                    finish();
                                                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                                }
                                            });
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                    e.printStackTrace();
                                    EditProfile.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Snackbar snackbar = Snackbar
                                                    .make(drawerLayout, "Sign out timeout, please check your Internet and try again", Snackbar.LENGTH_LONG);
                                            snackbar.show();
                                            signOutButton.setEnabled(true);
                                        }
                                    });
                                    return;
                                }
                            });
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                mediaPlayer.start();
                builder.show();
            }
        });

        snackbarShowFlag = true;
        onGetProfile();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        drawerLayout.closeDrawers();

        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.discover_capsule_tab:
                intent = new Intent(EditProfile.this, DiscoverCapsule.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
                return true;
            case R.id.create_capsule_tab:
                intent = new Intent(EditProfile.this, CreateCapsule.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
                return true;
            case R.id.capsule_history_tab:
                intent = new Intent(EditProfile.this, OpenedCapsuleHistory.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
                return true;
            case R.id.edit_profile_tab:
                //main activity cannot start itself again
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BottomDialog.TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(BottomDialog.imageUri));
//                        avatarDisplay.setImageBitmap(bitmap);
                        newAvatarFile = ImageUtil.compressImage(EditProfile.this, bitmap, "output_photo_compressed.jpg");
                        bottomDialog.dismiss();
//                        avatarDisplay.setImageBitmap(bitmap);
                        onUploadImage();
//                        Toast.makeText(this, "Take the photo successfully", Toast.LENGTH_SHORT).show();
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
            newAvatarFile = ImageUtil.compressImage(EditProfile.this, bitmap, "image_compressed.jpg");
//            avatarDisplay.setImageBitmap(bitmap);
            onUploadImage();
//            avatarDisplay.setImageBitmap(bitmap);
            bottomDialog.dismiss();
//            Toast.makeText(this, "Select the image successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    private void onGetProfile() {
        if (UserUtil.getToken(EditProfile.this).isEmpty()) {
            Toast.makeText(EditProfile.this, "No token to get profile", Toast.LENGTH_SHORT).show();
            usernameDisplay.setText("null");
            emailDisplay.setText("null");
            dobDisplay.setText("null");
        } else {
            HttpUtil.getProfile(UserUtil.getToken(EditProfile.this), new okhttp3.Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    Log.d("PROFILE", "***** getProfile onResponse *****");
                    String responseData = response.body().string();
                    Log.d("PROFILE", "getProfile: " + responseData);
                    try {
                        JSONObject responseJSON = new JSONObject(responseData);
                        if (responseJSON.has("success")) {
                            String status = responseJSON.getString("success");
                            Log.d("PROFILE", "getProfile success: " + status);
                            String userInfo = responseJSON.getString("userInfo");
                            JSONObject userInfoJSON = new JSONObject(userInfo);
                            usernameProfileString = userInfoJSON.getString("uusr");
                            avatarProfileString = userInfoJSON.getString("uavatar");
                            emailProfileString = userInfoJSON.getString("uemail");
                            dobProfileString = userInfoJSON.getString("udob");
                            runOnUiThread(new Runnable() {
                                              @Override
                                              public void run() {
                                                  // Toast.makeText(EditProfile.this, "Get profile successfully", Toast.LENGTH_SHORT).show();
                                                  Log.d("PROFILE", "avatarProfileString: " + avatarProfileString);

                                                  //only update profile if the activity is still alive
                                                  if (!EditProfile.this.isDestroyed()){
                                                      if (!(avatarProfileString == "null")) {
                                                              //TODO: use Glide solved previus problem of not loaded avatar
                                                              Glide.with(EditProfile.this)
                                                                      .load(avatarProfileString)
                                                                      .into(avatarDisplay);
                                                          Picasso.with(EditProfile.this)
                                                                  .load(avatarProfileString)
                                                                  .fit()
                                                                  .placeholder(R.drawable.logo)
                                                                  .into(headerAvatar);
                                                      } else {
                                                          Log.d("PROFILE", "avatarProfileString: (else)");
                                                          avatarDisplay.setImageResource(R.drawable.avatar_sample);
                                                      }
                                                      headerUsername.setText(usernameProfileString);

                                                      usernameDisplay.setText(usernameProfileString);
                                                      emailDisplay.setText(emailProfileString);
                                                      dobDisplay.setText(dobProfileString);

                                                      snackbarShowFlag = true;
                                                  } else {
                                                      Log.d("FINISHED", "run: Activity has been finished, don't load Glide for profile avatar & other info");
                                                  }
                                              }
                                          }
                            );
                        } else if (responseJSON.has("error")) {
                            
                            String status = responseJSON.getString("error");
                            Log.d("PROFILE", "getProfile error: " + status);
                            runOnUiThread(new Runnable() {
                                              @Override
                                              public void run() {
                                                  if (!EditProfile.this.isDestroyed()){
                                                      Toast.makeText(EditProfile.this, "Not logged in", Toast.LENGTH_SHORT).show();
                                                      usernameDisplay.setText("null");
                                                      emailDisplay.setText("null");
                                                      dobDisplay.setText("null");
                                                      snackbarShowFlag = true;
                                                  }
                                              }
                                          }
                            );
                        } else {
                            Log.d("PROFILE", "getProfile: Invalid form");
                            runOnUiThread(new Runnable() {
                                              @Override
                                              public void run() {
                                                  if (!EditProfile.this.isDestroyed()){
                                                      Toast.makeText(EditProfile.this, "Invalid form, please try again later", Toast.LENGTH_SHORT).show();
                                                      usernameDisplay.setText("null");
                                                      emailDisplay.setText("null");
                                                      dobDisplay.setText("null");
                                                      snackbarShowFlag = true;
                                                  }
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

                    //retry to get profile every 3 seconds. handle the case that enter the activity
                    //with no internet at all(which okHTTP will not retry for you)
                    // don't attempt to retry if activity has already been finished
                    if(!EditProfile.this.isDestroyed()){
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("RETRY", "run: RETRY onGetProfile");
                                if (snackbarShowFlag){
                                    Snackbar snackbar = Snackbar
                                            .make(drawerLayout, "Oops. Looks like you lost Internet connection\n Please connect to Internet and try again...", 5000);
                                    snackbar.show();
                                    snackbarShowFlag = false;
                                }
                                onGetProfile();
                            }
                        },3000);
                    }
                }
            });
        }
    }

    private void onUploadImage() {
        // token null
        HttpUtil.uploadImage(UserUtil.getToken(EditProfile.this), newAvatarFile, new okhttp3.Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.d("avatar", "1");
                // log comments
                String responseData = response.body().string();
                try {
                    JSONObject responseJSON = new JSONObject(responseData);
                    JSONObject data = responseJSON.getJSONObject("data");
                    newAvatarString = data.getString("url");
                    onChangeAvatar();
                    snackbarShowFlag = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                //retry to upload avatar every 3 seconds. handle the case that enter the activity
                //with no internet at all(which okHTTP will not retry for you)
                if (!EditProfile.this.isDestroyed()){
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (snackbarShowFlag){
                                Snackbar snackbar = Snackbar
                                        .make(drawerLayout, "Oops. Looks like you lost Internet connection\n Please connect to Internet and try again...", 3000);
                                snackbar.show();
                                snackbarShowFlag = false;
                            }

                            onUploadImage();
                        }
                    },3000);
                }

//                // If don't want to retry automatically, please comment above if condition and code and uncomment code below
//                Snackbar snackbar = Snackbar
//                        .make(drawerLayout, "Upload avatar timeout, please check your Internet and try again", Snackbar.LENGTH_LONG);
//                snackbar.show();
            }
        });
    }

    private void onChangeAvatar() {
        // token null
        HttpUtil.changeAvatar(UserUtil.getToken(EditProfile.this), newAvatarString, new okhttp3.Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // log comments
                Log.d("avatar", "2");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Update main page avatar
                        if (!EditProfile.this.isDestroyed()){
                            //TODO: use Glide solved previus problem of not loaded avatar
                            Glide.with(EditProfile.this)
                                    .load(newAvatarString)
                                    .into(avatarDisplay);
                            //also update the sliding menu header avatar
                            Glide.with(EditProfile.this)
                                    .load(newAvatarString)
                                    .into(headerAvatar);
                            snackbarShowFlag = true;
                        } else {
                            Log.d("FINISHED", "run: Activity has been finished, don't load Glide for avatar during onChangeAvatar");
                        }
                    }
                });
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                //retry to update avatar display every 3 seconds. handle the case that enter the activity
                //with no internet at all(which okHTTP will not retry for you)
                if (!EditProfile.this.isDestroyed()){
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (snackbarShowFlag){
                                Snackbar snackbar = Snackbar
                                        .make(drawerLayout, "Oops. Looks like you lost Internet connection\n Please connect to Internet and try again...", 3000);
                                snackbar.show();
                                snackbarShowFlag = false;
                            }
                            onChangeAvatar();
                        }
                    },3000);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //double backpressed to exit app
    //The logic is borrowed from https://stackoverflow.com/questions/8430805/clicking-the-back-button-twice-to-exit-an-activity
    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(navigationView)){
            drawerLayout.closeDrawer(navigationView);
        }else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }

}