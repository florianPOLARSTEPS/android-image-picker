package com.esafirm.imagepicker.features;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.esafirm.imagepicker.features.common.ImageLoaderListener;
import com.esafirm.imagepicker.model.FileSystemData;
import com.esafirm.imagepicker.model.Folder;
import com.esafirm.imagepicker.model.Image;
import com.facebook.common.util.UriUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {

    private static final String TAG = ImageLoader.class.getSimpleName();
    private final String[] projection = new String[]{
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED
    };
    private Context context;
    private ExecutorService executorService;

    public ImageLoader(Context context) {
        this.context = context;
    }

    public void loadDeviceImages(final ImagePickerConfig config, final ImageLoaderListener listener) {
        getExecutorService().execute(new ImageLoadRunnable(config, listener));
    }

    public void loadExternalDeviceImages(ImagePickerConfig config, List<Uri> uris, final ImageLoaderListener listener) {
        getExecutorService().execute(new ExternalImageLoadRunnable(config, uris, listener));
    }

    public void abortLoadImages() {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

    private void updateWithExifData(File file, Image image) {

        ExifInterface exifInterface;
        try {
            exifInterface = new ExifInterface(file.toString());
            float[] latLng = new float[2];
            boolean hasLatLng = exifInterface.getLatLong(latLng);
            if (hasLatLng) {
                image.setLatLng(latLng);
            }
        } catch (IOException e) {
            // noop
        }

    }

    private ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        return executorService;
    }

    private class ImageLoadRunnable implements Runnable {

        private final ImagePickerConfig config;
        private ImageLoaderListener listener;

        public ImageLoadRunnable(ImagePickerConfig config, ImageLoaderListener listener) {
            this.listener = listener;
            this.config = config;
        }

        @Override
        public void run() {
            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                    null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC, " + MediaStore.Images.Media.DATE_ADDED + " DESC");

            if (cursor == null) {
                listener.onFailed(new NullPointerException());
                return;
            }

            List<Image> temp = new ArrayList<>(cursor.getCount());
            Map<String, Folder> folderMap = null;
            Folder recentFolder = null;
            if (config.isFolderMode()) {
                folderMap = new HashMap<>();
                if (config.isShowFolderForDateRange()) {
                    String title = config.getDateFolderTitle() != null ? config.getDateFolderTitle() : "Recent";
                    recentFolder = new Folder(title);
                    recentFolder.setType(Folder.Type.RECENT);
                }
            }

            long imageCount = cursor.getCount();
            long geoInformationCount = 0;

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                    String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                    String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                    String bucket = cursor.getString(cursor.getColumnIndex(projection[3]));
                    long dateTaken = cursor.getLong(cursor.getColumnIndex(projection[5])) * 1000;
                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    if (path == null) {
                        continue;
                    }
                    try {
                        File file = new File(path);
                        if (file.exists()) {
                            Image image = new Image(id, name, path, contentUri, false);
                            if (config.isFetchLocationData()) {
                                updateWithExifData(file, image);
                                if (image.hasLatLng()) {
                                    Log.d(TAG, String.format("geocoordinates: %f : %f", image.getLatLng()[0], image.getLatLng()[1]));
                                    geoInformationCount++;
                                }
                            }

                            temp.add(image);

                            if (folderMap != null) {
                                Folder folder = folderMap.get(bucket);
                                if (folder == null) {
                                    folder = new Folder(bucket);
                                    folderMap.put(bucket, folder);
                                }
                                folder.getImages().add(image);

                                if (config.isShowFolderForDateRange() && recentFolder != null) {
                                    if (dateTaken >= config.getFolderStartDate() && dateTaken < config.getFolderEndDate()) {
                                        recentFolder.getImages().add(image);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "error accessing file: ", e);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();

            /* Convert HashMap to ArrayList if not null */
            List<Folder> folders = null;
            if (folderMap != null) {
                folders = new ArrayList<>(folderMap.values());
                Collections.sort(folders, new Comparator<Folder>() {
                    @Override
                    public int compare(Folder folder, Folder t1) {
                        return folder.getFolderName().compareTo(t1.getFolderName());
                    }
                });

                if (recentFolder != null && recentFolder.getImages() != null && recentFolder.getImages().size() > 0) {
                    folders.add(0, recentFolder);
                }
            }


            if (config.isFetchLocationData()) {
                Log.d(TAG, String.format("%d / %d images with geo coordinates", geoInformationCount, imageCount));
                FileSystemData mFileSystemData = new FileSystemData();
                mFileSystemData.setNumImages(imageCount);
                mFileSystemData.setNumImagesWithGeoTags(geoInformationCount);
                listener.onImageLoaded(temp, folders, mFileSystemData);
            } else {
                listener.onImageLoaded(temp, folders, null);
            }

        }
    }

    private class ExternalImageLoadRunnable implements Runnable {

        private final ImageLoaderListener listener;
        private final List<Uri> mImageUris;
        private final ImagePickerConfig config;

        public ExternalImageLoadRunnable(ImagePickerConfig config, List<Uri> mImageUris, ImageLoaderListener listener) {
            this.listener = listener;
            this.config = config;
            this.mImageUris = mImageUris;
        }

        private Image createSafeImage(@NonNull Uri mImageUri) {

            long imageId = mImageUri.hashCode();
            String imageName = mImageUri.getLastPathSegment();
            return new Image(imageId, imageName, null, mImageUri, false);
        }

        private Image tryReadWithMediaStorage(Uri mImageUri) throws CannotReadImageFromMediaStoreException {
            if (UriUtil.isLocalContentUri(mImageUri) || UriUtil.isDataUri(mImageUri)) {
                Cursor cursor = context.getContentResolver().query(mImageUri, projection,
                        null, null, null);

                try {
                    if (cursor == null) {
                        listener.onFailed(new NullPointerException());
                    } else {
                        if (cursor.moveToFirst()) {
                            long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                            String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                            String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                            Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                            File file = new File(path);
                            if (file.exists()) {
                                Image image = new Image(id, name, path, contentUri, false);
                                if (config.isFetchLocationData()) {
                                    updateWithExifData(file, image);
                                }
                                return image;
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new CannotReadImageFromMediaStoreException(e);
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } else if (UriUtil.isLocalFileUri(mImageUri)) {
                try {
                    String name = mImageUri.getLastPathSegment();
                    String path = mImageUri.getPath();

                    File file = new File(path);
                    if (file.exists()) {
                        Image image = new Image(path.hashCode(), name, path, mImageUri, false);
                        if (config.isFetchLocationData()) {
                            updateWithExifData(file, image);
                        }
                        return image;
                    }
                } catch (Exception e) {
                    throw new CannotReadImageFromMediaStoreException(e);
                    // no idea what happened
                }
            }
            return null;

        }

        private class CannotReadImageFromMediaStoreException extends IOException {
            public CannotReadImageFromMediaStoreException(Exception e) {
                super(e);
            }
        }

        @Override
        public void run() {

            List<Image> temp = new ArrayList<>();

            for (Uri mImageUri : mImageUris) {
                try {
                    Image image = tryReadWithMediaStorage(mImageUri);
                    if (image != null) {
                        temp.add(image);
                    } else {
                        temp.add(createSafeImage(mImageUri));
                    }
                } catch (CannotReadImageFromMediaStoreException e) {
                    temp.add(createSafeImage(mImageUri));
                }
            }

            listener.onImageLoaded(temp, null, null);
        }


    }
}
