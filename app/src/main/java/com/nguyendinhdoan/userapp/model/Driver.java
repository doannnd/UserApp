package com.nguyendinhdoan.userapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Driver implements Parcelable{

    private String name;
    private String email;
    private String phone;
    private String avatarUrl;
    private String rates;
    private String state;

    public Driver(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public Driver() {
    }

    public String getRates() {
        return rates;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public static Creator<Driver> getCREATOR() {
        return CREATOR;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.email);
        dest.writeString(this.phone);
    }

    protected Driver(Parcel in) {
        this.name = in.readString();
        this.email = in.readString();
        this.phone = in.readString();
    }

    public static final Parcelable.Creator<Driver> CREATOR = new Parcelable.Creator<Driver>() {
        @Override
        public Driver createFromParcel(Parcel source) {
            return new Driver(source);
        }

        @Override
        public Driver[] newArray(int size) {
            return new Driver[size];
        }
    };
}
