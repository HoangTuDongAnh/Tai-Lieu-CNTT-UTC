package com.example.contactadvance;


import androidx.annotation.NonNull;

public class User {
    private int id;
    private String Name;
    private String PhoneNum;
    private String imgUri;
    private Boolean Status;

    public Boolean getStatus() {
        return Status;
    }

    public void setStatus(Boolean status) {
        this.Status = status;
    }

    public User() {
    }

    public User(int id, String name, String phoneNum, String imgUri, Boolean status) {
        this.id = id;
        Name = name;
        PhoneNum = phoneNum;
        this.imgUri = imgUri;
        this.Status = status;
    }
    public User(int id, String name, String phoneNum, String imgUri) {
        this.id = id;
        Name = name;
        PhoneNum = phoneNum;
        this.imgUri = imgUri;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {Name = name;
    }

    public void setPhoneNum(String phoneNum) {
        PhoneNum = phoneNum;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return Name;
    }

    public String getPhoneNum() {
        return PhoneNum;
    }

    public String getImgUri() {
        return imgUri;
    }

    @NonNull
    @Override
    public String toString() {
        return getName() + " " + getPhoneNum()+ " " + getStatus() ;

    }
}


