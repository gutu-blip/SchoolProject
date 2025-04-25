package com.example.schoolproject.Models;

public class Item {

    private Boolean mSelected = false;
    private String item;

    public Item() {
    }

    public Item(String item, boolean selected) {
        this.item = item;
        this.mSelected = Boolean.valueOf(selected);
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public void setSelected(boolean selected) {
        this.mSelected = Boolean.valueOf(selected);
    }

    public boolean isSelected() {
        return this.mSelected.booleanValue();
    }

}
