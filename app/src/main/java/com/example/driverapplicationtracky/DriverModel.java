package com.example.driverapplicationtracky;

public class DriverModel {
    String name,email,busNo,password;

    public DriverModel() {
    }

    public DriverModel(String name, String email, String busNo, String password) {
        this.name = name;
        this.email = email;
        this.busNo = busNo;
        this.password = password;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getBusNo() {
        return busNo;
    }
    public void setBusNo(String busNo) {
        this.busNo = busNo;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
