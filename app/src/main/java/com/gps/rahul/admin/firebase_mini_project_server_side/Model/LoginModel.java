package com.gps.rahul.admin.firebase_mini_project_server_side.Model;

public class LoginModel {
    private String Name;
    private String Password;
    private String phone;
    private String IsStaff;

    public LoginModel() {
    }

    public LoginModel(String name, String password, String phone, String isStaff) {
        Name = name;
        Password = password;
        this.phone = phone;
        IsStaff = isStaff;
    }

    public String getIsStaff() {
        return IsStaff;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
