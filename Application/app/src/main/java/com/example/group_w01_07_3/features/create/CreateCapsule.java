package com.example.group_w01_07_3.features.create;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.os.Looper;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.group_w01_07_3.R;
import com.example.group_w01_07_3.SignIn;
import com.example.group_w01_07_3.features.account.EditProfile;
import com.example.group_w01_07_3.features.discover.DiscoverCapsule;
import com.example.group_w01_07_3.features.history.OpenedCapsuleHistory;
import com.example.group_w01_07_3.util.DensityUtil;
import com.example.group_w01_07_3.util.HttpUtil;
import com.example.group_w01_07_3.util.ImageUtil;
import com.example.group_w01_07_3.util.MessageUtil;
import com.example.group_w01_07_3.util.RecordAudioUtil;
import com.example.group_w01_07_3.util.UserUtil;
import com.example.group_w01_07_3.widget.BottomDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Create Capsule Activity. User will create their Geo-capsule in this page. The input filed including:
 * Title, Capsule text content with content moderation at server end. Plus one image & audio file.
 * Internet connectivity handling has been concentrated to handling as many issue as we discovered.
 */
public class CreateCapsule extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    //parameters
    private final int REQUEST_PERMISSION_COARSE_LOCATION = 2;
    private final int REQUEST_PERMISSION_FINE_LOCATION = 1;
    View headerView;
    TextView headerUsername;
    ShapeableImageView headerAvatar;
    NavigationView navigationView;
    ExtendedFloatingActionButton floatingActionButton;
    boolean mStartPlaying = true;
    // capsule content
    JSONObject capsuleInfo = new JSONObject();
    boolean doubleBackToExitPressedOnce = false;
    private String token;
    // APP view
    private DrawerLayout drawerLayout;
    private BottomDialog bottomDialog;
    private ProgressDialog progressbar;
    // location & audio utility section
    private FusedLocationProviderClient fusedLocationClient;
    private boolean mStartRecording = true;
    private String usernameProfileString, avatarProfileString;
    private RecordAudioUtil recorderUtil;
    private File imageFile;
    private int permission = 1;

    // message section
    private long mLastClickTime = SystemClock.elapsedRealtime();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_capsule);

        recorderUtil = new RecordAudioUtil(this);
        token = UserUtil.getToken(this.getApplicationContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        deleteAudioFile();

        //don't pop up keyboard automatically when entering the screen.
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        initView();

        updateHeader();
    }

    /**
     * setup navigation view for the activity
     */
    private void initView() {
        //setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_create);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Geo-Capsule");

        drawerLayout = findViewById(R.id.create_drawer_layout);

        //handle the hamburger menu. remember to create two strings
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //setup navigation view
        navigationView = findViewById(R.id.nav_view_create);
        navigationView.getMenu().getItem(1).setChecked(true);
        navigationView.setNavigationItemSelectedListener(this);

        //the logic to find the header, then update the username from server user profile
        headerView = navigationView.getHeaderView(0);
        headerUsername = headerView.findViewById(R.id.header_username);
        headerAvatar = headerView.findViewById(R.id.header_avatar);

        floatingActionButton = findViewById(R.id.create_fab);
    }

    /**
     * create public capsule or private capsule
     *
     * @param v view
     */
    public void whetherPublic(View v) {
        SwitchMaterial permiSwitch = (SwitchMaterial) findViewById(R.id.create_capsule_permission);
        if (permiSwitch.isChecked()) {
            permission = 0;
        } else {
            permission = 1;
        }

        if (permission == 1) {
            permiSwitch.setText("Create publicly visible Geo-Capsule");
        } else {
            permiSwitch.setText("Create your private Geo-Capsule");
        }
    }

    /**
     * get mStartPlaying
     * @return mStartPlaying
     */
    public boolean getmStartPlaying() {
        return mStartPlaying;
    }

    /**
     * set mStartPlaying
     * @param mStartPlaying mStartPlaying
     */
    public void setmStartPlaying(boolean mStartPlaying) {
        this.mStartPlaying = mStartPlaying;
    }

    /**
     * for recording and playing audio
     *
     * @param v view
     */
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
            MessageUtil.displaySnackbarWithAnchor(drawerLayout, "Record audio failed. Need your permission", Snackbar.LENGTH_SHORT, floatingActionButton);
        }
    }

    /**
     * play audio
     *
     * @param v view
     */
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

    /**
     * stop the recorder and the the player
     */
    public void onStop() {
        super.onStop();
        recorderUtil.onStop();
    }

    /**
     * For user upload/take picture
     *
     * @param v view
     */
    public void takePicture(View v) {
        bottomDialog = new BottomDialog(this);
        // Preventing multiple clicks, using threshold of 1 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 300) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

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

    /**
     * return result of take photo or choose photo.
     * Photo is compressed automatically 5% each time via a 500kb interval to reduce server load
     *
     * @param requestCode request code
     * @param resultCode  result code
     * @param data        intent data
     */
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
     * check permission of accessing device storage
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
                    MessageUtil.displaySnackbarWithAnchor(drawerLayout, "You denied the permission", Snackbar.LENGTH_SHORT, floatingActionButton);
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
     * display image
     *
     * @param imagePath image path
     */
    private void displayImage(String imagePath) {
        ImageView placeholder = (ImageView) findViewById(R.id.create_capsule_piture_preview);
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageFile = ImageUtil.compressImage(this, bitmap,
                    "create_capsule_image_compressed.jpg");
            placeholder.setImageBitmap(bitmap);
            bottomDialog.dismiss();
        } else {
            MessageUtil.displaySnackbarWithAnchor(drawerLayout, "Failed to take the photo. Please take another picture.", Snackbar.LENGTH_SHORT, floatingActionButton);
        }
    }

    /**
     * Upload functions
     * check text info are filled in
     *
     * @return text info are filled in or not
     */
    private boolean getOtherInfo() {
        EditText capsuleTitle = findViewById(R.id.create_capsule_title);
        EditText capsuleContent = findViewById(R.id.create_capsule_content);
        if (capsuleTitle.getText().toString().isEmpty()) {
            MessageUtil.displaySnackbarWithAnchor(drawerLayout, "Create Geo-capsule failed. Please give your capsule a title.", Snackbar.LENGTH_SHORT, floatingActionButton);
            return false;
        }
        if (capsuleContent.getText().toString().isEmpty()) {
            MessageUtil.displaySnackbarWithAnchor(drawerLayout, "Create Geo-capsule failed. Please fill in some content.", Snackbar.LENGTH_SHORT, floatingActionButton);
            return false;
        }
        try {
            capsuleInfo.put("title", capsuleTitle.getText());
            capsuleInfo.put("content", capsuleContent.getText());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            capsuleInfo.put("time", sdf.format(Calendar.getInstance().getTime()));

            capsuleInfo.put("permission", permission);
            capsuleInfo.put("tkn", this.token);
        } catch (JSONException e) {
            System.out.print("Problems happen during parsing json objects");
        }
        return true;
    }

    /**
     * check location permission
     *
     * @return has location permission or not
     */
    public boolean checkLocationPermission() {
        int fineLocation = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (fineLocation != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_FINE_LOCATION);
        }
        int corseLocation = ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (corseLocation != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSION_COARSE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * get location is a compulsory step , since Geo-capsule will be created to current user location.
     *
     * @throws JSONException JSONException
     */
    private void getLocation() throws JSONException {
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                Log.i("location", location.toString());
                                try {
                                    capsuleInfo.put("lat", location.getLatitude());
                                    capsuleInfo.put("lon", location.getLongitude());
                                    uploadAudio();
                                } catch (JSONException json) {
                                    Log.i("Fail to put in json", "Location cannot write " +
                                            "in json");
                                    MessageUtil.displaySnackbarWithAnchor(drawerLayout, "Cannot get your location. Please turn on GPS.", Snackbar.LENGTH_SHORT, floatingActionButton);
                                    progressbar.dismiss();
                                }
                            } else {
                                MessageUtil.displaySnackbarWithAnchor(drawerLayout, "Cannot get your location. Please turn on GPS.", Snackbar.LENGTH_SHORT, floatingActionButton);
                                progressbar.dismiss();
                                Log.i("location error", "Please turn on GPS and try again");
                            }
                        }
                    });
        }
    }

    /**
     * Upload audio url to server. If successful, continue to upload image
     */
    private void uploadAudio() {
        final File audioFile = recorderUtil.getAudioFile();
        if (audioFile != null && audioFile.exists()) {
            HttpUtil.uploadAudio(token, audioFile, new okhttp3.Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response)
                        throws IOException {
                    String responseData = response.body().string();
                    try {
                        JSONObject responseJSON = new JSONObject(responseData);
                        if (responseJSON.has("success")) {
                            String status = responseJSON.getString("success");
                            capsuleInfo.put("audio", responseJSON.getString("file"));
                            audioFile.delete();
                            uploadImg();
                        } else if (responseJSON.has("error")) {
                            String status = responseJSON.getString("error");
                            if (status.equalsIgnoreCase("Not logged in")) {
                                CreateCapsule.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        UserUtil.clearToken(CreateCapsule.this);
                                        MessageUtil.displayToast(CreateCapsule.this, "Not logged in", Toast.LENGTH_SHORT);
                                        Intent intent = new Intent(CreateCapsule.this, SignIn.class);
                                        startActivity(intent);
                                        finish();
                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    }
                                });
                            }
                        }
                    } catch (JSONException e) {
                        MessageUtil.displaySnackbarWithAnchor(drawerLayout, "Upload audio failed. Corrupted audio file, please record again.", Snackbar.LENGTH_SHORT, floatingActionButton);
                        e.printStackTrace();
                        progressbar.dismiss();
                    }
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            MessageUtil.displaySnackbarWithAnchor(drawerLayout, "Upload audio failed. Please check internet connection.", Snackbar.LENGTH_LONG, floatingActionButton);
                            progressbar.dismiss();
                        }
                    });
                }
            });
        } else {
            uploadImg();
        }
    }

    /**
     * Upload img url to server. If successful, continue to upload text information
     */
    private void uploadImg() {
        if (imageFile != null && imageFile.exists()) {
            HttpUtil.uploadImage(token, imageFile, new okhttp3.Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseData = response.body().string();
                    try {
                        JSONObject responseJSON = new JSONObject(responseData);
                        if (responseJSON.has("success")) {
                            String status = responseJSON.getString("success");
                            if (status == "false") {
                                System.out.println(responseJSON.getString("images"));
                                capsuleInfo.put("img", responseJSON.getString("images"));
                                uploadOther();
                            } else {
                                JSONObject data = responseJSON.getJSONObject("data");
                                System.out.println(data.getString("url"));
                                capsuleInfo.put("img", data.getString("url"));
                                uploadOther();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        MessageUtil.displaySnackbarWithAnchor(drawerLayout, "Upload image failed. Please check internet connection.", Snackbar.LENGTH_LONG, floatingActionButton);
                        progressbar.dismiss();
                    }
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            MessageUtil.displaySnackbarWithAnchor(drawerLayout, "Create Geo-capsule failed. Please check internet connection.", Snackbar.LENGTH_LONG, floatingActionButton);
                            progressbar.dismiss();
                        }
                    });
                }
            });
        } else {
            uploadOther();
        }
    }

    /**
     * Upload all text info to server
     */
    private void uploadOther() {
        HttpUtil.createCapsule(capsuleInfo, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        MessageUtil.displaySnackbarWithAnchor(drawerLayout, "Connection fail. Please check your internet.",
                                Snackbar.LENGTH_SHORT, floatingActionButton);
                        progressbar.dismiss();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseData = response.body().string();
                try {
                    JSONObject responseJSON = new JSONObject(responseData);
                    if (responseJSON.has("success")) {
                        String status = responseJSON.getString("success");
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              progressbar.dismiss();
                                              MessageUtil.displaySnackbarWithAnchor(drawerLayout, "Create Geo-capsule Successfully.",
                                                      Snackbar.LENGTH_SHORT, floatingActionButton);
                                              TextInputEditText capsuleTitle = findViewById(R.id.create_capsule_title);
                                              TextInputEditText capsuleContent = findViewById(R.id.create_capsule_content);
                                              capsuleTitle.setText("");
                                              capsuleContent.setText("");
                                              capsuleInfo = new JSONObject();

                                              if (imageFile != null) {
                                                  ImageView placeholder = (ImageView) findViewById
                                                          (R.id.create_capsule_piture_preview);
                                                  imageFile.delete();
                                                  placeholder.setImageResource(R.drawable.ic_camera);
                                                  placeholder.setOnClickListener(new View.OnClickListener() {
                                                      public void onClick(View v) {
                                                          takePicture(v);
                                                      }
                                                  });
                                              }
                                              deleteAudioFile();
                                          }
                                      }
                        );
                    } else if (responseJSON.has("error")) {
                        String status = responseJSON.getString("error");
                        if (status.equalsIgnoreCase("Not logged in")) {
                            CreateCapsule.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    UserUtil.clearToken(CreateCapsule.this);
                                    deleteAudioFile();
                                    Intent intent = new Intent(CreateCapsule.this, SignIn.class);
                                    startActivity(intent);
                                    finish();
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                }
                            });
                        } else if (status.equalsIgnoreCase("profanity text")) {
                            progressbar.dismiss();
                            MessageUtil.displaySnackbarWithAnchor(drawerLayout, "Create Geo-capsule failed. Please remove profanity text and try again.",
                                    Snackbar.LENGTH_SHORT, floatingActionButton);
                        }
                    } else {
                        progressbar.dismiss();
                        MessageUtil.displaySnackbarWithAnchor(drawerLayout, "Create Geo-capsule failed. Please try again later.",
                                Snackbar.LENGTH_SHORT, floatingActionButton);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * create capsule and dismiss progress bar.
     *
     * @param v view
     * @throws JSONException JSONException
     */
    public void createCapsule(View v) throws JSONException {
        // Preventing multiple clicks, using threshold of 1 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        if (getOtherInfo()) {
            progressbar = new ProgressDialog(CreateCapsule.this);
            progressbar.setTitle("Uploading");
            progressbar.setMessage("Creating capsule, please wait....");
            progressbar.setCancelable(false);
            progressbar.show();
            if (HttpUtil.isOnline(this)) {
                //collect info;
                getLocation();
            } else {
                progressbar.dismiss();
                MessageUtil.displaySnackbarWithAnchor(drawerLayout, "Create Geo-capsule failed. Please connect to internet first.", Snackbar.LENGTH_LONG, floatingActionButton);
            }
        }
    }

    /**
     * Handle navigation drawer item click event, which navigates user to the destination
     *
     * @param item the top level-destination listed at the navigation drawer
     * @return a boolean indicates if menu item click is done
     */
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

    /**
     * Update drawer layout header username & avatar. Supports auto retry via OKHTTP
     */
    private void updateHeader() {
        if (!UserUtil.getToken(CreateCapsule.this).isEmpty()) {
            HttpUtil.getProfile(UserUtil.getToken(CreateCapsule.this), new okhttp3.Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseData = response.body().string();
                    try {
                        JSONObject responseJSON = new JSONObject(responseData);
                        if (responseJSON.has("success")) {
                            String status = responseJSON.getString("success");
                            String userInfo = responseJSON.getString("userInfo");
                            JSONObject userInfoJSON = new JSONObject(userInfo);
                            usernameProfileString = userInfoJSON.getString("uusr");
                            avatarProfileString = userInfoJSON.getString("uavatar");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!CreateCapsule.this.isDestroyed()) {
                                        headerUsername.setText(usernameProfileString);
                                        if (!(avatarProfileString == "null")) {
                                            Picasso.with(CreateCapsule.this)
                                                    .load(avatarProfileString)
                                                    .fit()
                                                    .placeholder(R.drawable.logo)
                                                    .into(headerAvatar);
                                        }
                                    } else {
                                        Log.d("FINISHED", "run: Activity has been finished, don't load Glide for update header avatar & username");
                                    }
                                }
                            });
                        } else if (responseJSON.has("error")) {
                            String status = responseJSON.getString("error");
                            if (status.equalsIgnoreCase("Not logged in")) {
                                CreateCapsule.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        UserUtil.clearToken(CreateCapsule.this);
                                        MessageUtil.displayToast(CreateCapsule.this, "Not logged in", Toast.LENGTH_SHORT);
                                        Intent intent = new Intent(CreateCapsule.this, SignIn.class);
                                        startActivity(intent);
                                        finish();
                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    }
                                });
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                    //retry to update every 3 seconds. handle the case that enter the activity
                    //with no internet at all(which okHTTP will not retry for you)
                    if (!CreateCapsule.this.isDestroyed()) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateHeader();
                            }
                        }, 3000);
                    }
                }
            });
        }
    }

    /**
     * Double back pressed to exit app
     * The logic is borrowed from https://stackoverflow.com/questions/8430805/clicking-the-back-button-twice-to-exit-an-activit
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                deleteAudioFile();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            MessageUtil.displayToast(this, "Press back again to exit", Toast.LENGTH_SHORT);

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    /**
     * destroy delete the recorded audio file
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteAudioFile();
    }

    /**
     * delete the recorded audio file audio file
     */
    public void deleteAudioFile() {
        if (recorderUtil.getAudioFile().exists()) {
            recorderUtil.getAudioFile().delete();
        }
    }

}