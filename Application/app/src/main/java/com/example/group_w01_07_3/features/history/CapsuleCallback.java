package com.example.group_w01_07_3.features.history;

import android.widget.ImageView;
import android.widget.TextView;

public interface CapsuleCallback {

    //which field we would like to support shared element transition
    void onCapsuleItemClick(
            int pos,
            TextView title,
            TextView date,
            ImageView capImage,
            TextView privateTag,
            TextView content,
            ImageView avatar,
            TextView by,
            TextView username
    );

}