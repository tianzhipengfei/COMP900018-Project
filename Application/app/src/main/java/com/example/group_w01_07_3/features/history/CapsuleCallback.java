package com.example.group_w01_07_3.features.history;

import android.widget.TextView;

public interface CapsuleCallback {

    void onCapsuleItemClick(
            int pos,
            TextView title,
            TextView date
    );
}
