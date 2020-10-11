package com.example.group_w01_07_3.features.history;

public class OpenedCapsule {

    String capsule_title;
    String opened_date;
    int avatar;
    int capsule_image;

    public OpenedCapsule(){

    }

    public OpenedCapsule(String capsule_title, String opened_date, int avatar, int capsule_image) {
        this.capsule_title = capsule_title;
        this.opened_date = opened_date;
        this.avatar = avatar;
        this.capsule_image = capsule_image;
    }

    public String getCapsule_title() {
        return capsule_title;
    }

    public String getOpened_date() {
        return opened_date;
    }

    public int getAvatar() {
        return avatar;
    }

    public int getCapsule_image() {
        return capsule_image;
    }

    public void setCapsule_title(String capsule_title) {
        this.capsule_title = capsule_title;
    }

    public void setOpened_date(String opened_date) {
        this.opened_date = opened_date;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public void setCapsule_image(int capsule_image) {
        this.capsule_image = capsule_image;
    }
}
