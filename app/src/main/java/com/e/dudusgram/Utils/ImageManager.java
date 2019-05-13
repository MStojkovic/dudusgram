package com.e.dudusgram.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

class ImageManager {

    private static final String TAG = "ImageManager";

    static Bitmap getBitmap(String imgUrl){

        File imageFile = new File(imgUrl);
        FileInputStream fis = null;
        Bitmap bitmap = null;
        
        try {
            
            fis = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(fis);
            
        } catch (FileNotFoundException e){
            Log.e(TAG, "getBitmap: FileNotFoundException" + e.getMessage());
        } finally {
            try {
                fis.close();
            } catch (IOException e){
                Log.e(TAG, "getBitmap: IOException" + e.getMessage() );
            }
        }
        return bitmap;
    }

    /**
     * Returns byte array from a bitmap
     * @param bm
     * @param quality greater than 0 but less than 100
     * @return
     */

    static byte[] getBytesFromBitmap(Bitmap bm, int quality){

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, quality, stream);

        return stream.toByteArray();
    }
}
