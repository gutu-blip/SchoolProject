package com.example.schoolproject.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

public class Listing implements Parcelable, Comparable<Listing>{

    private String link,name,description,category,rates,time,postKey,userID,location,hostel,room,condition,phone;
    private String imageUrl,uploadType,sellerKey,sellerName,sellerPhoto,sellerPhone,listingSource,trade;
    private String amount;
    int price;
    private Boolean mSelected = false;
    ArrayList<String> album,specs;

    public Listing() {
    }

    public Listing(String category, String name, String description, String time, String postKey,
                   String userID, String location, String amount, String sellerName, String sellerPhone,
                   String sellerPhoto,String link, int price) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.time = time;
        this.postKey = postKey;
        this.userID = userID;
        this.location = location;
        this.amount = amount;
        this.sellerName = sellerName;
        this.sellerPhone = sellerPhone;
        this.sellerPhoto = sellerPhoto;
        this.link = link;
        this.price = price;
    }

    public Listing(String name, String description, String category, String time, String postKey,
                   String listingSource,String trade, String userID, String location, String hostel,
                   String room, String condition,String imageUrl,String uploadType,
                   String sellerKey, String sellerName, String sellerPhone,String sellerPhoto,int price,
                   String rates) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.time = time;
        this.postKey = postKey;
        this.listingSource = listingSource;
        this.trade = trade;
        this.userID = userID;
        this.location = location;
        this.hostel = hostel;
        this.room = room;
        this.condition = condition;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.uploadType = uploadType;
        this.price = price;
        this.sellerKey = sellerKey;
        this.sellerName = sellerName;
        this.sellerPhone = sellerPhone;
        this.sellerPhoto = sellerPhoto;
        this.rates = rates;
    }

    //-Category,Amount,Description,College,Phone,userid,postkey,time

    public Listing(String category,String description, String time, String postKey, String userID,
                   String location, String phone, String amount,int price) {
        this.category = category;
        this.description = description;
        this.time = time;
        this.postKey = postKey;
        this.userID = userID;
        this.location = location;
        this.phone = phone;
        this.amount = amount;
        this.price = price;
    }

    protected Listing(Parcel in) {
        link = in.readString();
        name = in.readString();
        description = in.readString();
        category = in.readString();
        rates = in.readString();
        time = in.readString();
        postKey = in.readString();
        userID = in.readString();
        location = in.readString();
        hostel = in.readString();
        room = in.readString();
        condition = in.readString();
        phone = in.readString();
        imageUrl = in.readString();
        uploadType = in.readString();
        sellerKey = in.readString();
        sellerName = in.readString();
        sellerPhoto = in.readString();
        sellerPhone = in.readString();
        listingSource = in.readString();
        trade = in.readString();
        amount = in.readString();
        price = in.readInt();
        byte tmpMSelected = in.readByte();
        mSelected = tmpMSelected == 0 ? null : tmpMSelected == 1;
        album = in.createStringArrayList();
        specs = in.createStringArrayList();
    }

    public static final Creator<Listing> CREATOR = new Creator<Listing>() {
        @Override
        public Listing createFromParcel(Parcel in) {
            return new Listing(in);
        }

        @Override
        public Listing[] newArray(int size) {
            return new Listing[size];
        }
    };

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public ArrayList<String> getSpecs() {
        return specs;
    }

    public void setSpecs(ArrayList<String> specs) {
        this.specs = specs;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = trade;
    }

    public String getListingSource() {
        return listingSource;
    }

    public void setListingSource(String listingSource) {
        this.listingSource = listingSource;
    }

    public String getSellerKey() {
        return sellerKey;
    }

    public void setSellerKey(String sellerKey) {
        this.sellerKey = sellerKey;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerPhoto() {
        return sellerPhoto;
    }

    public void setSellerPhoto(String sellerPhoto) {
        this.sellerPhoto = sellerPhoto;
    }

    public String getSellerPhone() {
        return sellerPhone;
    }

    public void setSellerPhone(String sellerPhone) {
        this.sellerPhone = sellerPhone;
    }

    public String getUploadType() {
        return uploadType;
    }

    public void setUploadType(String uploadType) {
        this.uploadType = uploadType;
    }

    public ArrayList<String> getAlbum() {
        return album;
    }

    public void setAlbum(ArrayList<String> album) {
        this.album = album;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getHostel() {
        return hostel;
    }

    public void setHostel(String hostel) {
        this.hostel = hostel;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Exclude
    public void setSelected(boolean selected) {
        this.mSelected = Boolean.valueOf(selected);
    }

    @Exclude
    public boolean isSelected() {
        return this.mSelected.booleanValue();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(link);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(category);
        parcel.writeString(rates);
        parcel.writeString(time);
        parcel.writeString(postKey);
        parcel.writeString(userID);
        parcel.writeString(location);
        parcel.writeString(hostel);
        parcel.writeString(room);
        parcel.writeString(condition);
        parcel.writeString(phone);
        parcel.writeString(imageUrl);
        parcel.writeString(uploadType);
        parcel.writeString(sellerKey);
        parcel.writeString(sellerName);
        parcel.writeString(sellerPhoto);
        parcel.writeString(sellerPhone);
        parcel.writeString(listingSource);
        parcel.writeString(trade);
        parcel.writeString(amount);
        parcel.writeInt(price);
        parcel.writeByte((byte) (mSelected == null ? 0 : mSelected ? 1 : 2));
        parcel.writeStringList(album);
        parcel.writeStringList(specs);
    }

    @Override
    public int compareTo(Listing listing) {
        return 0;
    }
}
