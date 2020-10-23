package com.example.mibasedefuego.models;

import android.graphics.Bitmap;

public class User {
    private String email;
    private String username;
    private String phone;
    private Bitmap profileImg;

    public User(String email, String username, String phone, Bitmap profileImg) {
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.profileImg = profileImg;
    }

    public User() {
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Bitmap getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(Bitmap profileImg) {
        this.profileImg = profileImg;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", phone='" + phone + '\'' +
                ", profileImg=" + profileImg +
                '}';
    }
}
