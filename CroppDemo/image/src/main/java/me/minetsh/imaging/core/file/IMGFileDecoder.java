package me.minetsh.imaging.core.file;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import me.minetsh.imaging.IMGCropUtil;

/**
 * Created by felix on 2017/12/26 下午3:07.
 */

public class IMGFileDecoder extends IMGDecoder {

    private Context mContext;

    public IMGFileDecoder(Context context,Uri uri) {
        super(uri);
        mContext = context;
    }

    @Override
    public Bitmap decode(BitmapFactory.Options options) {
        Uri uri = getUri();
        if (uri == null) {
            return null;
        }

        String path = IMGCropUtil.getInstance().getPath(mContext, uri);
        if (TextUtils.isEmpty(path)) {
            return null;
        }

        //当targetSDK>=29时，安卓10以下和安卓11以上机型，File，BitmapFactory等相关API可兼容使用
        //而安卓10特殊，当targetSDK>=29时，File，BitmapFactory等部分API无法使用，需要改用方式
        if (Build.VERSION.SDK_INT == 29){
            //构造输入流
            try {
                InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
                //解析Bitmap
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        File file = new File(path);
        if (file.exists()) {
            Bitmap srcBitmap = BitmapFactory.decodeFile(path, options);
            if (options.inJustDecodeBounds){
                return srcBitmap;
            }
            int angle = getBitmapDegree(file);
            if (angle != 0) {
                Bitmap rotateBitmap = null;
                // 下面的方法主要作用是把图片转一个角度，也可以放大缩小等
                Matrix m = new Matrix();
                int width = srcBitmap.getWidth();
                int height = srcBitmap.getHeight();
                m.setRotate(angle); // 旋转angle度
                try {
                    rotateBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, width, height,m, true);// 新生成图片
                } catch(Exception e) {
                }

                if(rotateBitmap == null) {
                    rotateBitmap = srcBitmap;
                }

                if(srcBitmap != rotateBitmap) {
                    srcBitmap.recycle();
                }
                return rotateBitmap;
            }
            else {
                return srcBitmap;
            }
        }
        return null;
    }

    public static int getBitmapDegree(File file) {
        int degree = 0;
        if (Build.VERSION.SDK_INT <= 29) return degree;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(String.valueOf(file));
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }
}
