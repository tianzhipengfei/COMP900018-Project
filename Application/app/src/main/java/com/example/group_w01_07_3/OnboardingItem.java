package com.example.group_w01_07_3;

public class OnboardingItem {
    String title, description;
    int introImage;

    public OnboardingItem(String title, String description, int introImage) {
        this.title = title;
        this.description = description;
        this.introImage = introImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIntroImage() {
        return introImage;
    }

    public void setIntroImage(int introImage) {
        this.introImage = introImage;
    }
}
