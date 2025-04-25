package com.example.schoolproject.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class UserProfile implements Parcelable, Comparable<UserProfile>{

    String profilePhoto,userId,time,firstName,displayName,phoneNumber,email,userName,followStatus;

    public UserProfile() {
    }

    public UserProfile(String profilePhoto, String userId,String displayName,String firstName,
                       String time, String email, String userName) {
          this.profilePhoto = profilePhoto;
          this.userId = userId;
          this.displayName = displayName;
          this.firstName = firstName;
          this.time = time;
          this.email = email;
          this.userName = userName;
    }

    protected UserProfile(Parcel in) {
        profilePhoto = in.readString();
        userId = in.readString();
        time = in.readString();
        firstName = in.readString();
        displayName = in.readString();
        phoneNumber = in.readString();
        email = in.readString();
        userName = in.readString();
        followStatus = in.readString();
    }

    public static final Creator<UserProfile> CREATOR = new Creator<UserProfile>() {
        @Override
        public UserProfile createFromParcel(Parcel in) {
            return new UserProfile(in);
        }

        @Override
        public UserProfile[] newArray(int size) {
            return new UserProfile[size];
        }
    };

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFollowStatus() {
        return followStatus;
    }

    public void setFollowStatus(String followStatus) {
        this.followStatus = followStatus;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(profilePhoto);
        parcel.writeString(userId);
        parcel.writeString(time);
        parcel.writeString(firstName);
        parcel.writeString(displayName);
        parcel.writeString(phoneNumber);
        parcel.writeString(email);
        parcel.writeString(userName);
        parcel.writeString(followStatus);
    }

    @Override
    public int compareTo(UserProfile userProfile) {
        return 0;
    }
}
