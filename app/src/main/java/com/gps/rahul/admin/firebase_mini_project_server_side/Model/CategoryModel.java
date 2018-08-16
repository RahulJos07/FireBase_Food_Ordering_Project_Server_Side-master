package com.gps.rahul.admin.firebase_mini_project_server_side.Model;

public class CategoryModel {
    private String Name;
    private String Image;

    public CategoryModel() {
    }

    public CategoryModel(String name, String image) {
        Name = name;
        Image = image;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }
}
