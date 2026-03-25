package com.example.songapplication;

public class Song {
    private int id;
    private String name;
    private float rating;
    private String singer;

    public Song() {
    }

    public Song(int id, String name, float rating, String singer) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.singer = singer;
    }

    public Song(String name, float rating, String singer) {
        this.name = name;
        this.rating = rating;
        this.singer = singer;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }
}