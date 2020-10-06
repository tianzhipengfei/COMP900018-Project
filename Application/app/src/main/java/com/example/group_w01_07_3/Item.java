package com.example.group_w01_07_3;

public class Item {

    int background;
    String profileName;
    int ProfilePh0to;
    int nbFollowers;

    public Item(){

    }
    public Item(int background, String profileName, int profilePhito, int nbFollowers) {
        this.background = background;
        this.profileName = profileName;
        ProfilePh0to = profilePhito;
        this.nbFollowers = nbFollowers;
    }

    public int getBackground() {
        return background;
    }

    public String getProfileName() {
        return profileName;
    }

    public int getProfilePhito() {
        return ProfilePh0to;
    }

    public int getNbFollowers() {
        return nbFollowers;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public void setProfilePhito(int profilePhito) {
        ProfilePh0to = profilePhito;
    }

    public void setNbFollowers(int nbFollowers) {
        this.nbFollowers = nbFollowers;
    }

}
