package com.example.myapplication;

public class Author {
    public String name;
    public int photoResId;
    public String photoPath; // Путь к файлу изображения

    public Author(String name, int photoResId) {
        this.name = name;
        this.photoResId = photoResId;
        this.photoPath = null;
    }

    public Author(String name, int photoResId, String photoPath) {
        this.name = name;
        this.photoResId = photoResId;
        this.photoPath = photoPath;
    }
}
