package com.esafirm.imagepicker.features;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.features.camera.CameraModule;
import com.esafirm.imagepicker.features.camera.DefaultCameraModule;
import com.esafirm.imagepicker.features.camera.OnImageReadyListener;
import com.esafirm.imagepicker.features.common.BasePresenter;
import com.esafirm.imagepicker.features.common.ImageLoaderListener;
import com.esafirm.imagepicker.model.FileSystemData;
import com.esafirm.imagepicker.model.Folder;
import com.esafirm.imagepicker.model.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImagePickerPresenter extends BasePresenter<ImagePickerView> {

    public FileSystemData fileSystemData;
    private ImageLoader imageLoader;
    private CameraModule cameraModule = new DefaultCameraModule();
    private Handler handler = new Handler(Looper.getMainLooper());

    public ImagePickerPresenter(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public void abortLoad() {
        imageLoader.abortLoadImages();
    }

    public void loadImages(boolean isFolderMode) {
        if (!isViewAttached()) return;

        getView().showLoading(true);
        imageLoader.loadDeviceImages(isFolderMode, new ImageLoaderListener() {


            @Override
            public void onImageLoaded(final List<Image> images, final List<Folder> folders, FileSystemData mFileSystemData) {
                if (mFileSystemData != null) {
                    fileSystemData = mFileSystemData;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isViewAttached()) {
                            getView().showFetchCompleted(images, folders);

                            if (folders != null) {
                                if (folders.isEmpty()) {
                                    getView().showEmpty();
                                } else {
                                    getView().showLoading(false);
                                }
                            } else {
                                if (images.isEmpty()) {
                                    getView().showEmpty();
                                } else {
                                    getView().showLoading(false);
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onFailed(final Throwable throwable) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isViewAttached()) {
                            getView().showError(throwable);
                        }
                    }
                });
            }
        });
    }

    public void onDoneSelectImages(List<Image> selectedImages) {
        if (selectedImages != null && selectedImages.size() > 0) {

            /** Scan selected images which not existed */
            for (int i = 0; i < selectedImages.size(); i++) {
                Image image = selectedImages.get(i);
                File file = new File(image.getPath());
                if (!file.exists()) {
                    selectedImages.remove(i);
                    i--;
                }
            }
            getView().finishPickImages(selectedImages, fileSystemData);
        }
    }

    public void captureImage(Activity activity, ImagePickerConfig config, int requestCode) {
        Context context = activity.getApplicationContext();
        Intent intent = cameraModule.getCameraIntent(activity, config);
        if (intent == null) {
            Toast.makeText(context, context.getString(R.string.ef_error_create_image_file), Toast.LENGTH_LONG).show();
            return;
        }
        activity.startActivityForResult(intent, requestCode);
    }

    public void finishPickExternalApplication(Context context, Intent data, final ImagePickerConfig config) {

        List<Uri> images = new ArrayList<>();
        if (data.getData() != null) {
            Uri mImageUri = data.getData();
            images.add(mImageUri);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (data.getClipData() != null) {
                ClipData mClipData = data.getClipData();
                for (int i = 0; i < mClipData.getItemCount(); i++) {
                    Uri uri = mClipData.getItemAt(i).getUri();
                    if (!images.contains(uri)) {
                        images.add(uri);
                    }
                }
            }
        }

        if (images.size() > 0) {
            imageLoader.loadExternalDeviceImages(images, new ImageLoaderListener() {
                @Override
                public void onImageLoaded(List<Image> images, List<Folder> folders, FileSystemData mFileSystemData) {
                    getView().finishPickImages(images, fileSystemData);
                }

                @Override
                public void onFailed(Throwable throwable) {
                    //TODO: handle error
                }
            });
        }


    }

    public void finishCaptureImage(Context context, Intent data, final ImagePickerConfig config) {
        cameraModule.getImage(context, data, new OnImageReadyListener() {
            @Override
            public void onImageReady(List<Image> images) {
                if (config.isReturnAfterFirst()) {
                    getView().finishPickImages(images, fileSystemData);
                } else {
                    getView().showCapturedImage();
                }
            }
        });
    }
}
