package com.example.group_w01_07_3.features.history;

import java.io.Serializable;

/**
 * This is the Geo-capsule object, containing 8 attributes
 */
public class OpenedCapsule implements Serializable {

    String capsule_title;
    String opened_date;
    String avatar_url;
    String capsule_url;
    int tag;
    String content;
    String username;
    String voice_url;

    public OpenedCapsule() {

    }

    public OpenedCapsule(String capsule_title, String opened_date, String avatar_url, String capsule_url, int tag, String content, String username, String voice_url) {
        this.capsule_title = capsule_title;
        this.opened_date = opened_date;
        this.avatar_url = avatar_url;
        this.capsule_url = capsule_url;
        this.tag = tag;
        this.content = content;
        this.username = username;
        this.voice_url = voice_url;
    }

    public String getCapsule_title() {
        return capsule_title;
    }

    public void setCapsule_title(String capsule_title) {
        this.capsule_title = capsule_title;
    }

    public String getOpened_date() {
        return opened_date;
    }

    public void setOpened_date(String opened_date) {
        this.opened_date = opened_date;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getCapsule_url() {
        return capsule_url;
    }

    public void setCapsule_url(String capsule_url) {
        this.capsule_url = capsule_url;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVoice_url() {
        return voice_url;
    }

    public void setVoice_url(String voice_url) {
        this.voice_url = voice_url;
    }

}