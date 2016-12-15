package com.esafirm.imagepicker.helper;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/**
 * Created by florian on 15/12/2016.
 * (c) Polarsteps
 */

public class FrescoHelper {


    public static void setImageUriResizeToImage(@NonNull DraweeView view, @NonNull Uri uri, int width, int height) {
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(width, height))
                .setLocalThumbnailPreviewsEnabled(false)
                .setCacheChoice(ImageRequest.CacheChoice.DEFAULT)
                .setRotationOptions(RotationOptions.autoRotate())
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(view.getController())
                .setImageRequest(request)
                .build();
        view.setController(controller);
    }

}
