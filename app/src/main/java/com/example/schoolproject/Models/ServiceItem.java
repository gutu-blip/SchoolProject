package com.example.schoolproject.Models;

import java.util.ArrayList;

public class ServiceItem {

    private Object object;
    private ArrayList<Listing> imageServices;
    private ArrayList<Listing> imagelessServices;
    private int type;

    public ServiceItem() {
    }

    public ServiceItem(Object object, int type) {
        this.object = object;
        this.type = type;
    }

    public ArrayList<Listing> getImageServices() {
        return imageServices;
    }

    public void setImageServices(ArrayList<Listing> imageServices) {
        this.imageServices = imageServices;
    }

    public ArrayList<Listing> getImagelessServices() {
        return imagelessServices;
    }

    public void setImagelessServices(ArrayList<Listing> imagelessServices) {
        this.imagelessServices = imagelessServices;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
