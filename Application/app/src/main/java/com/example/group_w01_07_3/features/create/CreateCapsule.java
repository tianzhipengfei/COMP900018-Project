package com.example.group_w01_07_3.features.create;

import android.content.ContentUris;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.group_w01_07_3.features.account.EditProfile;
import com.example.group_w01_07_3.features.history.OpenedCapsuleHistory;
import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.features.discover.DiscoverCapsule;
import com.example.group_w01_07_3.util.DensityUtil;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.ImageUtil;
import com.example.group_w01_07_3.util.LocationUtil;
import com.example.group_w01_07_3.util.RecordAudioUtil;
import com.example.group_w01_07_3.util.UserUtil;
import com.example.group_w01_07_3.widget.BottomDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Response;

public class CreateCapsule extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private long mLastClickTime = 0;

    boolean doubleBackToExitPressedOnce = false;

    private boolean mStartRecording = true;
    boolean mStartPlaying = true;

    private DrawerLayout drawerLayout;
    View headerview;
    TextView headerUsername;
    private String usernameProfileString;

    NavigationView navigationView;

    ExtendedFloatingActionButton floatingActionButton;

    private RecordAudioUtil recorderUtil;
    private LocationUtil locationUtil;
    private int permission = 1;
    private String token;

    private BottomDialog bottomDialog;
    private File imageFile;

    JSONObject capsuleInfo = new JSONObject();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_capsule);
        recorderUtil = new RecordAudioUtil(this);
        locationUtil = new LocationUtil(this);
        UserUtil userUtil = new UserUtil();
        token = userUtil.getToken(this.getApplicationContext());

        //don't pop up keyboard automatically when entering the screen.
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        //设置主Activity的toolbar, 以及初始化侧滑菜单栏
        Toolbar toolbar = findViewById(R.id.toolbar_create);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Create Memory Capsule");

        drawerLayout = findViewById(R.id.create_drawer_layout);

        //handle the hamburger menu. remember to create two strings
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //设置侧滑菜单栏
        navigationView = findViewById(R.id.nav_view_create);
        navigationView.getMenu().getItem(1).setChecked(true);
        navigationView.setNavigationItemSelectedListener(this);

        //the logic to find the header, then update the username from server user profile
        headerview = navigationView.getHeaderView(0);
        headerUsername = headerview.findViewById(R.id.header_username);

        updateHeaderUsername();
    }


    public void whetherPublic(View v) {
        SwitchMaterial permiSwitch = (SwitchMaterial) findViewById(R.id.create_capsule_permission);
        if (permiSwitch.isChecked()) {
            permission = 0;
        } else {
            permission = 1;
        }

        if (permission == 1) {
            permiSwitch.setText("Create Public Memory Capsule");
        } else {
            permiSwitch.setText("Create Private Memory Capsule");
        }
    }

    private boolean getLocation() throws JSONException {

        Location location = locationUtil.getLocation();
        if (location != null) {
            try {
                Log.i("GOT LCOATION", "GOT LCOATION");
                Log.i("LAT", String.valueOf(location.getLatitude()));
                Log.i("LON", String.valueOf(location.getLongitude()));
                capsuleInfo.put("lat", location.getLatitude());
                capsuleInfo.put("lon", location.getLongitude());
                return true;
            } catch (JSONException e) {
                System.out.print("Problems happen during parsing json objects");
                return false;
            }
        } else {
            Log.e("NULL POSITION", "NULL LOCATION");

            capsuleInfo.put("lat", 37.4219983);
            capsuleInfo.put("lon", -122.084);
            return true;
        }

    }

    private boolean getOtherInfo() {
        EditText capsuleTitle = findViewById(R.id.create_capsule_title);
        EditText capsuleContent = findViewById(R.id.create_capsule_content);
        if (capsuleTitle.getText().toString().isEmpty()) {
            Toast.makeText(CreateCapsule.this, "Please enter the title",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (capsuleContent.getText().toString().isEmpty()) {
            Toast.makeText(CreateCapsule.this, "Please enter the content",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            capsuleInfo.put("title", capsuleTitle.getText());
            capsuleInfo.put("content", capsuleContent.getText());
            capsuleInfo.put("time", Calendar.getInstance().getTime());
            capsuleInfo.put("permission", permission);
            capsuleInfo.put("tkn", this.token);
            //for testing

        } catch (JSONException e) {
            System.out.print("Problems happen during parsing json objects");
        }
        return true;
    }

    // for recording and playing audio
    public void recordAudio(View v) {
        // check permission
        if (recorderUtil.checkPermission()) {
            MaterialButton recordButton = findViewById(R.id.record_button);
            recorderUtil.onRecord(mStartRecording);
            if (mStartRecording) {
                recordButton.setIconResource(R.drawable.voice_stop);
                recordButton.setText("STOP");
            } else {
                recordButton.setIconResource(R.drawable.voice_record);
                recordButton.setText("RECORD");
            }
            mStartRecording = !mStartRecording;
        } else {
            Toast.makeText(getApplicationContext(), "Need permission", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    public void playAudio(View v) {

        MaterialButton playButton = findViewById(R.id.play_button);
        recorderUtil.onPlay(mStartPlaying);
        if (mStartPlaying) {
            playButton.setIconResource(R.drawable.voice_stop);
            playButton.setText("STOP");
        } else {
            playButton.setIconResource(R.drawable.voice_play);
            playButton.setText("PLAY");
        }
        mStartPlaying = !mStartPlaying;

    }

    public void onStop() {

        super.onStop();
        recorderUtil.onStop();
    }

    // For user upload/take picture
    public void takePicture(View v) {

        bottomDialog = new BottomDialog(this);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) bottomDialog
                .getContentView().getLayoutParams();
        params.width = getResources().getDisplayMetrics().widthPixels -
                DensityUtil.dp2px(this, 16f);


        params.bottomMargin = DensityUtil.dp2px(this, 8f);
        bottomDialog.getContentView().setLayoutParams(params);
        bottomDialog.setCanceledOnTouchOutside(true);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView placeholder = (ImageView) findViewById(R.id.create_capsule_piture_preview);
        switch (requestCode) {
            case BottomDialog.TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(BottomDialog.imageUri));
                        placeholder.setImageBitmap(bitmap);
                        imageFile = ImageUtil.compressImage(this, bitmap,
                                "create_capsule_output_photo_compressed.jpg");
                        bottomDialog.dismiss();
                        Toast.makeText(this, "Take the photo successfully",
                                Toast.LENGTH_SHORT).show();
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
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
        ImageView placeholder = (ImageView) findViewById(R.id.create_capsule_piture_preview);
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageFile = ImageUtil.compressImage(this, bitmap,
                    "create_capsule_image_compressed.jpg");
            placeholder.setImageBitmap(bitmap);
            bottomDialog.dismiss();
            Toast.makeText(this, "Select the image successfully",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadAudio(){
        File audioFile = recorderUtil.getAudioFile();
        //RequestSend requestSend = new RequestSend(token, capsuleInfo, this);
        //requestSend.execute(imageFile, audioFile);
        if(audioFile!=null){
            HttpUtil.uploadAudio(token, audioFile, new okhttp3.Callback() {

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response)
                        throws IOException {
                    String responseData = response.body().string();
                    Log.i("AudioHTTP", responseData);
                    try {
                        JSONObject responseJSON = new JSONObject(responseData);
                        if (responseJSON.has("success")) {
                            String status = responseJSON.getString("success");
                            Log.i("AudioUrl", status);
                            capsuleInfo.put("audio",responseJSON.getString("file"));
                            uploadImg();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                }
            });

        }else{
            uploadImg();
        }
    }

    private void uploadImg(){
        if(imageFile!=null) {
            HttpUtil.uploadImage(token, imageFile, new okhttp3.Callback() {

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseData = response.body().string();
                    Log.i("CREATECAPSULE", responseData);
                    try {
                        JSONObject responseJSON = new JSONObject(responseData);
                        if (responseJSON.has("success")) {
                            String status = responseJSON.getString("success");
                            Log.i("ImageUrl", status);
                            capsuleInfo.put("img", responseJSON.getString("file"));
                            uploadOther();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {

                }
            });
        }else{
            uploadOther();
        }
    }

    private void uploadOther(){
        Log.i("CapsuleInfo", capsuleInfo.toString());

        HttpUtil.createCapsule(capsuleInfo, new okhttp3.Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Toast.makeText(CreateCapsule.this, "connection fail", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseData = response.body().string();
                Log.i("CREATECAPSULE", responseData);
                try {
                    JSONObject responseJSON = new JSONObject(responseData);
                    if (responseJSON.has("success")) {
                        String status = responseJSON.getString("success");
                        Log.i("CREATECAPSULE", status);
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              Toast.makeText(
                                                      CreateCapsule.this,
                                                      "Create Capsule successfully",
                                                      Toast.LENGTH_SHORT).show();
//
                                          }
                                      }
                        );
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void createCapsule(View v) throws JSONException {

        // Preventing multiple clicks, using threshold of 1 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        if (getLocation() && getOtherInfo()) {
            //collect info;
            uploadAudio();


        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawers();

        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.discover_capsule_tab:
                intent = new Intent(CreateCapsule.this, DiscoverCapsule.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            case R.id.create_capsule_tab:
                //main activity cannot start itself again
                return true;
            case R.id.capsule_history_tab:
                intent = new Intent(CreateCapsule.this, OpenedCapsuleHistory.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            case R.id.edit_profile_tab:
                intent = new Intent(CreateCapsule.this, EditProfile.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
        }


        return false;
    }

    private void updateHeaderUsername() {
        if (!UserUtil.getToken(CreateCapsule.this).isEmpty()) {
            HttpUtil.getProfile(UserUtil.getToken(CreateCapsule.this), new okhttp3.Callback() {
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
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    headerUsername.setText(usernameProfileString);
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
                }
            });
        }
    }


    //double backpressed to exit app
    //The logic is borrowed from https://stackoverflow.com/questions/8430805/clicking-the-back-button-twice-to-exit-an-activity
    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }
}
