package com.example.schoolproject.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

public class Seller implements Parcelable, Comparable<Seller>{

    private Boolean mSelected = false;
    private String item;

    private String profilePhoto,college,email,phone,sellerKey,name,time,userID,hostel,room,mantra;
    private String patrons,rating,listingSource,trade,description;


    // Business
    public Seller(String profilePhoto, String rating,String college, String mantra, String email, String phone,
                 String name, String userID, String time,String sellerKey,String hostel,String room,
                 String listingSource,String trade,String description) {
        this.profilePhoto = profilePhoto;
        this.rating = rating;
        this.college = college;
        this.mantra = mantra;
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.userID = userID;
        this.time = time;
        this.sellerKey = sellerKey;
        this.hostel = hostel;
        this.room = room;
        this.listingSource = listingSource;
        this.trade = trade;
        this.description = description;

    }

    public Seller() {
    }

    public Seller(String item, boolean selected) {
        this.item = item;
        this.mSelected = Boolean.valueOf(selected);
    }


    protected Seller(Parcel in) {
        byte tmpMSelected = in.readByte();
        mSelected = tmpMSelected == 0 ? null : tmpMSelected == 1;
        item = in.readString();
        profilePhoto = in.readString();
        college = in.readString();
        email = in.readString();
        phone = in.readString();
        sellerKey = in.readString();
        name = in.readString();
        time = in.readString();
        userID = in.readString();
        hostel = in.readString();
        room = in.readString();
        mantra = in.readString();
        patrons = in.readString();
        rating = in.readString();
        listingSource = in.readString();
        trade = in.readString();
        description = in.readString();
    }

    public static final Creator<Seller> CREATOR = new Creator<Seller>() {
        @Override
        public Seller createFromParcel(Parcel in) {
            return new Seller(in);
        }

        @Override
        public Seller[] newArray(int size) {
            return new Seller[size];
        }
    };

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSellerKey() {
        return sellerKey;
    }

    public void setSellerKey(String sellerKey) {
        this.sellerKey = sellerKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
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

    public String getMantra() {
        return mantra;
    }

    public void setMantra(String mantra) {
        this.mantra = mantra;
    }

    public String getPatrons() {
        return patrons;
    }

    public void setPatrons(String patrons) {
        this.patrons = patrons;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
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
        parcel.writeByte((byte) (mSelected == null ? 0 : mSelected ? 1 : 2));
        parcel.writeString(item);
        parcel.writeString(profilePhoto);
        parcel.writeString(college);
        parcel.writeString(email);
        parcel.writeString(phone);
        parcel.writeString(sellerKey);
        parcel.writeString(name);
        parcel.writeString(time);
        parcel.writeString(userID);
        parcel.writeString(hostel);
        parcel.writeString(room);
        parcel.writeString(mantra);
        parcel.writeString(patrons);
        parcel.writeString(rating);
        parcel.writeString(listingSource);
        parcel.writeString(trade);
        parcel.writeString(description);
    }

    @Override
    public int compareTo(Seller seller) {
        return 0;
    }


//    Opening a business
//
//Name of store
//Bio
//Profile Photo(Optional)
//Email
//Whatsapp Number
//University
//Hostel(Optional)
//Room No.(Optional)

//    Posting as an Individual
//
//-Name(Use the regsitered name)
//-Profile Photo(Optional)
//-Whatsapp Number
//-University
//-Hostel(Optional)
//-Room No.(Optional)

}
