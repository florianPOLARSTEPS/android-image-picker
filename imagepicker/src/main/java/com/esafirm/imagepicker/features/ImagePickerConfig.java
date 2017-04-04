package com.esafirm.imagepicker.features;

import android.os.Parcel;
import android.os.Parcelable;

import com.esafirm.imagepicker.model.Image;

import java.util.ArrayList;

public class ImagePickerConfig implements Parcelable {


    public static final Creator<ImagePickerConfig> CREATOR = new Creator<ImagePickerConfig>() {
        @Override
        public ImagePickerConfig createFromParcel(Parcel in) {
            return new ImagePickerConfig(in);
        }

        @Override
        public ImagePickerConfig[] newArray(int size) {
            return new ImagePickerConfig[size];
        }
    };
    private ArrayList<Image> selectedImages;
    private String folderTitle;
    private String imageTitle;
    private String imageDirectory;
    private String dateFolderTitle;
    private int mode;
    private int limit;
    private long folderStartDate;
    private long folderEndDate;
    private boolean showFolderForDateRange;
    private boolean folderMode;
    private boolean showCamera;
    private boolean returnAfterFirst;
    private boolean useExternalPickers;
    private boolean fetchLocationData;


    public ImagePickerConfig() {
    }

    protected ImagePickerConfig(Parcel in) {
        selectedImages = in.createTypedArrayList(Image.CREATOR);
        folderTitle = in.readString();
        imageTitle = in.readString();
        imageDirectory = in.readString();
        dateFolderTitle = in.readString();
        mode = in.readInt();
        limit = in.readInt();
        folderStartDate = in.readLong();
        folderEndDate = in.readLong();
        showFolderForDateRange = in.readByte() != 0;
        folderMode = in.readByte() != 0;
        showCamera = in.readByte() != 0;
        returnAfterFirst = in.readByte() != 0;
        useExternalPickers = in.readByte() != 0;
        fetchLocationData = in.readByte() != 0;
    }

    public boolean isReturnAfterFirst() {
        return returnAfterFirst;
    }

    public int getMode() {
        return mode;
    }

    public int getLimit() {
        return limit;
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    public String getFolderTitle() {
        return folderTitle;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public ArrayList<Image> getSelectedImages() {
        return selectedImages;
    }

    public boolean isFolderMode() {
        return folderMode;
    }

    public String getImageDirectory() {
        return imageDirectory;
    }

    public boolean isUseExternalPickers() {
        return useExternalPickers;
    }

    public boolean isFetchLocationData() {
        return fetchLocationData;
    }

    public boolean isShowFolderForDateRange() {
        return showFolderForDateRange;
    }

    public long getFolderEndDate() {
        return folderEndDate;
    }

    public long getFolderStartDate() {
        return folderStartDate;
    }

    public String getDateFolderTitle() {
        return dateFolderTitle;
    }

    public void setReturnAfterFirst(boolean returnAfterFirst) {
        this.returnAfterFirst = returnAfterFirst;
    }

    public void setFolderStartDate(long folderStartDate) {
        this.folderStartDate = folderStartDate;
    }

    public void setFolderEndDate(long folderEndDate) {
        this.folderEndDate = folderEndDate;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
    }

    public void setFolderTitle(String folderTitle) {
        this.folderTitle = folderTitle;
    }

    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }

    public void setSelectedImages(ArrayList<Image> selectedImages) {
        this.selectedImages = selectedImages;
    }

    public void setShowFolderForDateRange(boolean showFolderForDateRange) {
        this.showFolderForDateRange = showFolderForDateRange;
    }

    public void setFolderMode(boolean folderMode) {
        this.folderMode = folderMode;
    }

    public void setImageDirectory(String imageDirectory) {
        this.imageDirectory = imageDirectory;
    }

    public void setUseExternalPickers(boolean useExternalPickers) {
        this.useExternalPickers = useExternalPickers;
    }

    public void setFetchLocationData(boolean fetchLocationData) {
        this.fetchLocationData = fetchLocationData;
    }

    public void setDateFolderTitle(String dateFolderTitle) {
        this.dateFolderTitle = dateFolderTitle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(selectedImages);
        dest.writeString(folderTitle);
        dest.writeString(imageTitle);
        dest.writeString(imageDirectory);
        dest.writeString(dateFolderTitle);
        dest.writeInt(mode);
        dest.writeInt(limit);
        dest.writeLong(folderStartDate);
        dest.writeLong(folderEndDate);
        dest.writeByte((byte) (showFolderForDateRange ? 1 : 0));
        dest.writeByte((byte) (folderMode ? 1 : 0));
        dest.writeByte((byte) (showCamera ? 1 : 0));
        dest.writeByte((byte) (returnAfterFirst ? 1 : 0));
        dest.writeByte((byte) (useExternalPickers ? 1 : 0));
        dest.writeByte((byte) (fetchLocationData ? 1 : 0));
    }
}
