package com.example.group_w01_07_3.features.history;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.group_w01_07_3.R;

public class DetailedCapsuleHistoryItem extends AppCompatActivity {

    private Toolbar mToolbar;

    TextView title;
    TextView date;
    TextView tag;
    TextView content;
    TextView username;
    ImageView image;
    ImageView avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_capsule_history_item);

        //set up toolbar
        mToolbar = findViewById(R.id.detail_history_capsule_back_toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Capsule Review");

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailedCapsuleHistoryItem.super.onBackPressed();
            }
        });


        title = findViewById(R.id.history_detail_title);
        date = findViewById(R.id.history_detail_date);

        image = findViewById(R.id.history_detail_image);
        tag = findViewById(R.id.history_detail_capsule_private_public_tag);
        content = findViewById(R.id.history_detail_content);

        avatar = findViewById(R.id.history_detail_capsule_original_user_avatar);

        username = findViewById(R.id.history_detail_username);

        //get the capsule object
        OpenedCapsule item = (OpenedCapsule) getIntent().getExtras().getSerializable("capsuleObject");

        loadCapsule(item);
    }


    //这里设置的部分都会被update,不管那一块写了transition没有
    private void loadCapsule(OpenedCapsule item){
        String titleString  = item.getCapsule_title();
        String dateString = item.getOpened_date();
        title.setText(titleString);
        date.setText(dateString);

        int imageLocation = item.getCapsule_image(); //TODO: This is for testing purpose only. 真正实现的时候你应该用的是Picasso或者Glide从URL load
        image.setImageResource(imageLocation);

        String tagString = item.getTag();
        tag.setText(tagString);

        String contentString = item.getContent();
        content.setText(contentString);

        int avatarLocation = item.getAvatar(); //TODO: This is for testing purpose only. 真正实现的时候你应该用的是Picasso或者Glide从URL load
        avatar.setImageResource(avatarLocation);

        String usernameString = item.getUsername();
        username.setText(usernameString);
    }
}