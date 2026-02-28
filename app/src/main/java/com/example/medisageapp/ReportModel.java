package com.example.medisageapp;

public class ReportModel {
    public String name;
    public String url;

    public ReportModel() {} // required for Firebase

    public ReportModel(String name, String url) {
        this.name = name;
        this.url = url;
    }
}
