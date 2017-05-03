package com.esafirm.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ImagePickerActivity;
import com.esafirm.imagepicker.features.camera.CameraModule;
import com.esafirm.imagepicker.features.camera.ImmediateCameraModule;
import com.esafirm.imagepicker.features.camera.OnImageReadyListener;
import com.esafirm.imagepicker.model.FileSystemData;
import com.esafirm.imagepicker.model.Image;
import com.esafirm.rximagepicker.RxImagePicker;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    private static final int RC_CAMERA = 3000;

    private static final int RC_CODE_PICKER = 2000;

    private CameraModule cameraModule;


    private TextView textView;

    Action1<List<Image>> action = new Action1<List<Image>>() {
        @Override
        public void call(List<Image> images) {
            printImages(images);
        }
    };

    //region ------- ANDROID -------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fresco.initialize(this);

        textView = (TextView) findViewById(R.id.text_view);

        findViewById(R.id.button_pick_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });

        findViewById(R.id.button_pick_image_rx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImagePickerObservable().forEach(action);
            }
        });

        findViewById(R.id.button_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Activity activity = MainActivity.this;
                final String[] permissions = new String[]{Manifest.permission.CAMERA};
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, permissions, RC_CAMERA);
                } else {
                    captureImage();
                }
            }
        });

        findViewById(R.id.button_pick_image_intent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWithIntent();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {

        if (requestCode == RC_CODE_PICKER && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = (ArrayList<Image>) ImagePicker.getImages(data);
            FileSystemData fileSystemData = ImagePicker.getFileSystemData(data);
            if (fileSystemData != null) {
                Log.d("FSD", fileSystemData.toString());
            }
            printImages(images);
            loadImages(images);
        } else if (requestCode == RC_CAMERA && resultCode == RESULT_OK) {
            getCameraModule().getImage(this, data, new OnImageReadyListener() {
                @Override
                public void onImageReady(List<Image> resultImages) {
                    ArrayList<Image> images = (ArrayList<Image>) resultImages;
                    printImages(images);
                    loadImages(images);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_CAMERA) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    //endregion ------- ANDROID -------

    // Recommended builder
    public void start() {
        boolean returnAfterCapture = ((Switch) findViewById(R.id.ef_switch_return_after_capture)).isChecked();
        boolean isSingleMode = ((Switch) findViewById(R.id.ef_switch_single)).isChecked();

        ImagePicker imagePicker = ImagePicker.create(this)
                .returnAfterFirst(returnAfterCapture) // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
                .folderMode(true) // set folder mode (false by default)
                .folderTitle("Folder") // folder selection title
                .imageTitle("Tap to select"); // image selection title

        if (isSingleMode) {
            imagePicker.single();
        } else {
            imagePicker.multi(); // multi mode (default mode)
        }

        Calendar instance = Calendar.getInstance();
        long now = instance.getTimeInMillis();
        instance.add(Calendar.DATE, -30);
        long daysAgo = instance.getTimeInMillis();

        imagePicker.limit(10) // max images can be selected (99 by default)
                .useExternalPickers(true)
                .highlightFolder(daysAgo, now, "Recent")
                .showCamera(true) // show camera or not (true by default)
                .imageDirectory("Camera")   // captured image directory name ("Camera" folder by default)
                .start(RC_CODE_PICKER); // start image picker activity with request code
    }

    // Traditional intent
    public void startWithIntent() {
        Intent intent = new Intent(this, ImagePickerActivity.class);
        intent.putExtra(ImagePicker.EXTRA_FOLDER_MODE, true);
        intent.putExtra(ImagePicker.EXTRA_MODE, ImagePicker.MODE_MULTIPLE);
        intent.putExtra(ImagePicker.EXTRA_LIMIT, 10);
        intent.putExtra(ImagePicker.EXTRA_SHOW_CAMERA, true);
        intent.putExtra(ImagePicker.EXTRA_FOLDER_TITLE, "Album");
        intent.putExtra(ImagePicker.EXTRA_IMAGE_TITLE, "Tap to select images");
        intent.putExtra(ImagePicker.EXTRA_IMAGE_DIRECTORY, "Camera");

        /* Will force ImagePicker to single pick */
        intent.putExtra(ImagePicker.EXTRA_RETURN_AFTER_FIRST, true);

        startActivityForResult(intent, RC_CODE_PICKER);
    }

    private void captureImage() {
        startActivityForResult(getCameraModule().getCameraIntent(MainActivity.this), RC_CAMERA);
    }

    private void printImages(List<Image> images) {
        if (images == null) return;

        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0, l = images.size(); i < l; i++) {
            stringBuffer.append(images.get(i)
                    .getUri())
                    .append("\n");
        }
        textView.setText(stringBuffer.toString());
    }

    @SuppressLint({"StaticFieldLeak", "HandlerLeak"})
    private void loadImages(final List<Image> images) {
        if (images == null) return;

        new Handler() {
            @Override
            public void handleMessage(Message msg) {
                new AsyncTask<Void, Void, Boolean>() {

                    @Override
                    protected Boolean doInBackground(Void... voids) {

                        for (Image image : images) {
                            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(image.getUri())
                                    .build();
                            ImagePipeline imagePipeline = Fresco.getImagePipeline();
                            DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(request, null);

                            dataSource.subscribe(new BaseBitmapDataSubscriber() {
                                @Override
                                public void onFailureImpl(DataSource dataSource) {
                                    // No cleanup required here.
                                    Log.e("RESULT", "received error", dataSource.getFailureCause());
                                }

                                @Override
                                public void onNewResultImpl(@Nullable Bitmap bitmap) {
                                    Log.d("RESULT", "received image");
                                    // You can use the bitmap here, but in limited ways.
                                    // No need to do any cleanup.
                                }
                            }, CallerThreadExecutor.getInstance());
                        }

                        return null;
                    }
                }.execute();
            }
        }.sendEmptyMessageDelayed(0, 10000);


    }

    private ImmediateCameraModule getCameraModule() {
        if (cameraModule == null) {
            cameraModule = new ImmediateCameraModule();
        }
        return (ImmediateCameraModule) cameraModule;
    }

    private Observable<List<Image>> getImagePickerObservable() {
        return RxImagePicker.getInstance()
                .start(this, ImagePicker.create(this));
    }
}
