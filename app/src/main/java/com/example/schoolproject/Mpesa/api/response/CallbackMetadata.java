package com.example.schoolproject.Mpesa.api.response;

/**
 * @author Fredrick Ochieng on 02/02/2018.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CallbackMetadata {

    @SerializedName("Item")
    @Expose
    private List<Item> item = null;

    public List<Item> getItem() {
        return item;
    }

    public void setItem(List<Item> item) {
        this.item = item;
    }

}
