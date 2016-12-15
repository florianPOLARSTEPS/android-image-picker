package com.esafirm.imagepicker.model;

import android.net.Uri;

import com.esafirm.imagepicker.helper.ImagePickerUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageFactory {

    public static List<Image> singleListFromPath(String path) {
        List<Image> images = new ArrayList<>();
        images.add(new Image(0, ImagePickerUtils.getNameFromFilePath(path), path, Uri.fromFile(new File(path)), true));
        return images;
    }
}
