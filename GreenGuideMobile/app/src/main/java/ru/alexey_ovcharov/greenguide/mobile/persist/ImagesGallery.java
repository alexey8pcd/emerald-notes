package ru.alexey_ovcharov.greenguide.mobile.persist;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.alexey_ovcharov.greenguide.mobile.Commons;

/**
 * Created by Admin on 18.06.2017.
 */

public abstract class ImagesGallery {

    protected List<Image> imagesInfo = new ArrayList<>(1);

    @Nullable
    public Image getFirstImage() {
        return imagesInfo.size() > 0 ? imagesInfo.get(0) : null;
    }

    public List<Image> getImagesInfo() {
        return imagesInfo;
    }

    public void addImage(Image image) {
        this.imagesInfo.add(image);
    }

    @NonNull
    public List<Image.ImageDataWrapper<Bitmap>> getImagesBitmaps(ContentResolver contentResolver) {
        try {
            List<Image.ImageDataWrapper<Bitmap>> imageDataWrappers = new ArrayList<>(imagesInfo.size());
            for (Image image : imagesInfo) {
                try {
                    if (image.getUrl() != null) {
                        Uri imageUrl = Uri.parse(image.getUrl());
                        InputStream in = contentResolver.openInputStream(imageUrl);
                        Bitmap bitmap = BitmapFactory.decodeStream(in, null, null);
                        imageDataWrappers.add(new Image.ImageDataWrapper(image.getIdImage(), bitmap));
                    }
                } catch (Exception e) {
                    Log.e(Commons.APP_NAME, e.toString(), e);
                }
            }
            return imageDataWrappers;
        } catch (Exception e) {
            Log.e(Commons.APP_NAME, e.toString(), e);
        }
        return Collections.EMPTY_LIST;
    }

    public void addImages(Collection<Image> images) {
        imagesInfo.addAll(images);
    }


}
