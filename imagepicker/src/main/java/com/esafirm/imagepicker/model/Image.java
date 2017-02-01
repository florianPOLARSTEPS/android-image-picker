package com.esafirm.imagepicker.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.io.File;

public class Image implements Parcelable {


    public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };
    private long id;
    private String name;
    private String path;
    private Uri uri;
    private boolean isSelected;
    private double[] latLng;


    public Image(long id, String name, String path, Uri uri, boolean isSelected) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.isSelected = isSelected;
        this.uri = uri;
    }

    protected Image(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.path = in.readString();
        this.uri = in.readParcelable(Uri.class.getClassLoader());
        this.isSelected = in.readByte() != 0;
        this.latLng = in.createDoubleArray();
    }

    public boolean hasLatLng() {
        return this.latLng != null;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Nullable
    public Uri getUri() {
        if (uri != null) {
            return uri;
        } else if (path != null) {
            return Uri.fromFile(new File(path));
        }
        return null;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public double[] getLatLng() {
        return latLng;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void setLatLng(float[] latLng) {
        this.latLng = new double[]{latLng[0], latLng[1]};
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.path);
        dest.writeParcelable(this.uri, flags);
        dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
        dest.writeDoubleArray(this.latLng);
    }
}
