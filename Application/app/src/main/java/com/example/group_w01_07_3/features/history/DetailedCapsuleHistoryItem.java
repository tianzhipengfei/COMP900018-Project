package com.example.group_w01_07_3.features.history;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.group_w01_07_3.R;

public class DetailedCapsuleHistoryItem extends AppCompatActivity {

    TextView title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_capsule_history_item);

        title = findViewById(R.id.history_detail_title);

        //get the capsule object
        OpenedCapsule item = (OpenedCapsule) getIntent().getExtras().getSerializable("capsuleObject");

        loadCapsule(item);
    }

    private void loadCapsule(OpenedCapsule item){
        String titleString  = item.getCapsule_title();
        title.setText(titleString);
    }
}