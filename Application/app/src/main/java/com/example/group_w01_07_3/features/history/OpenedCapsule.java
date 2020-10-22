package com.example.group_w01_07_3.features.history;

import java.io.Serializable;

public class OpenedCapsule implements Serializable {

    String capsule_title;
    String opened_date;
    int avatar;
    int capsule_image;
    String tag;
    String content;
    String username;

    public OpenedCapsule(){

    }

    public OpenedCapsule(String capsule_title, String opened_date, int avatar, int capsule_image, String tag, String content, String username) {
        this.capsule_title = capsule_title;
        this.opened_date = opened_date;
        this.avatar = avatar;
        this.capsule_image = capsule_image;
        this.tag = tag;
        this.content = content;
        this.username = username;
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

    public String getTag() {
        return tag;
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
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

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
