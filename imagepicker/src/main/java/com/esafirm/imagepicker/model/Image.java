package com.esafirm.imagepicker.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.io.File;

public class Image implements Parcelable {

    public static final Creator<Image> CREATOR = new Creator<Image>() {
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
    }
}
