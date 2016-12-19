package com.esafirm.imagepicker.features;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.esafirm.imagepicker.features.common.ImageLoaderListener;
import com.esafirm.imagepicker.model.Folder;
import com.esafirm.imagepicker.model.Image;

import java.io.File;
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
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    };
    private Context context;
    private ExecutorService executorService;

    public ImageLoader(Context context) {
        this.context = context;
    }

    public void loadDeviceImages(final boolean isFolderMode, final ImageLoaderListener listener) {
        getExecutorService().execute(new ImageLoadRunnable(isFolderMode, listener));
    }

    public void loadExternalDeviceImages(List<Uri> uris, final ImageLoaderListener listener) {
        getExecutorService().execute(new ExternalImageLoadRunnable(uris, listener));
    }

    public void abortLoadImages() {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

    private ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        return executorService;
    }

    private class ImageLoadRunnable implements Runnable {

        private boolean isFolderMode;
        private ImageLoaderListener listener;

        public ImageLoadRunnable(boolean isFolderMode, ImageLoaderListener listener) {
            this.isFolderMode = isFolderMode;
            this.listener = listener;
        }

        @Override
        public void run() {
            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                    null, null, MediaStore.Images.Media.DATE_ADDED);

            if (cursor == null) {
                listener.onFailed(new NullPointerException());
                return;
            }

            List<Image> temp = new ArrayList<>(cursor.getCount());
            Map<String, Folder> folderMap = null;
            if (isFolderMode) {
                folderMap = new HashMap<>();
            }

            if (cursor.moveToLast()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                    String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                    String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                    String bucket = cursor.getString(cursor.getColumnIndex(projection[3]));
                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    File file = new File(path);
                    if (file.exists()) {
                        Image image = new Image(id, name, path, contentUri, false);
                        temp.add(image);

                        if (folderMap != null) {
                            Folder folder = folderMap.get(bucket);
                            if (folder == null) {
                                folder = new Folder(bucket);
                                folderMap.put(bucket, folder);
                            }
                            folder.getImages().add(image);
                        }
                    }

                } while (cursor.moveToPrevious());
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
            }

            listener.onImageLoaded(temp, folders);
        }
    }

    private class ExternalImageLoadRunnable implements Runnable {

        private final ImageLoaderListener listener;
        private final List<Uri> mImageUris;

        public ExternalImageLoadRunnable(List<Uri> mImageUris, ImageLoaderListener listener) {
            this.listener = listener;
            this.mImageUris = mImageUris;
        }

        @Override
        public void run() {

            List<Image> temp = new ArrayList<>();

            for (Uri mImageUri : mImageUris) {

                Cursor cursor = context.getContentResolver().query(mImageUri, projection,
                        null, null, null);

                try {
                    if (cursor == null) {
                        listener.onFailed(new NullPointerException());
                        return;
                    }

                    if (cursor.moveToFirst()) {
                        long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                        String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                        String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                        Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                        File file = new File(path);
                        if (file.exists()) {
                            Image image = new Image(id, name, path, contentUri, false);
                            temp.add(image);
                        }
                    }
                    cursor.close();
                } catch (Exception e) {
                    Log.e(TAG, "error loading image for uri: " + mImageUri);
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }


            listener.onImageLoaded(temp, null);
        }
    }
}
