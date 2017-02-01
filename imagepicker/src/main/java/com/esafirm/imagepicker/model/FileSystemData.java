package com.esafirm.imagepicker.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by florian on 01/02/2017.
 * (c) Polarsteps
 */

public class FileSystemData implements Parcelable {

    public static final Parcelable.Creator<FileSystemData> CREATOR = new Parcelable.Creator<FileSystemData>() {
        @Override
        public FileSystemData createFromParcel(Parcel source) {
            return new FileSystemData(source);
        }

        @Override
        public FileSystemData[] newArray(int size) {
            return new FileSystemData[size];
        }
    };
    private long numImages;
    private long numImagesWithGeoTags;

    public FileSystemData() {
    }

    protected FileSystemData(Parcel in) {
        this.numImages = in.readLong();
        this.numImagesWithGeoTags = in.readLong();
    }

    public long getNumImages() {
        return numImages;
    }

    public long getNumImagesWithGeoTags() {
        return numImagesWithGeoTags;
    }

    public void setNumImages(long numImages) {
        this.numImages = numImages;
    }

    public void setNumImagesWithGeoTags(long numImagesWithGeoTags) {
        this.numImagesWithGeoTags = numImagesWithGeoTags;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.numImages);
        dest.writeLong(this.numImagesWithGeoTags);
    }

    @Override
    public String toString() {
        return "FileSystemData{" +
                "numImages=" + numImages +
                ", numImagesWithGeoTags=" + numImagesWithGeoTags +
                '}';
    }
}
