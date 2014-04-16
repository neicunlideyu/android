package cn.onboard.android.app.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.DisplayMetrics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/** 
 * 图片操作工具包
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class ImageUtils{

    /** 请求相册 */
    public static final int REQUEST_CODE_GETIMAGE_BYSDCARD = 0;
    /** 请求相机 */
    public static final int REQUEST_CODE_GETIMAGE_BYCAMERA = 1;

    /**
	 * 写图片文件
	 * 在Android系统中，文件保存在 /data/data/PACKAGE_NAME/files 目录下
	 * @throws IOException 
	 */
	public static void saveImage(Context context, String fileName, Bitmap bitmap) throws IOException 
	{ 
		saveImage(context, fileName, bitmap, 100);
	}
	public static void saveImage(Context context, String fileName, Bitmap bitmap, int quality) throws IOException 
	{ 
		if(bitmap==null || fileName==null || context==null)	return;		

		FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, quality, stream);
		byte[] bytes = stream.toByteArray();
		fos.write(bytes); 			
		fos.close();
	}

    /**
	 * 获取bitmap
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static Bitmap getBitmap(Context context,String fileName) {
		FileInputStream fis = null;
		Bitmap bitmap = null;
		try {
			fis = context.openFileInput(fileName);
			bitmap = BitmapFactory.decodeStream(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}finally{
			try {
				fis.close();
			} catch (Exception e) {}
		}
		return bitmap;
	}

    public static Bitmap getBitmapByPath(String filePath, BitmapFactory.Options opts) {
		FileInputStream fis = null;
		Bitmap bitmap =null; 
		try { 
			File file = new File(filePath);
			fis = new FileInputStream(file);
			bitmap = BitmapFactory.decodeStream(fis,null,opts);
		} catch (FileNotFoundException e) {  
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} finally{
			try {
				fis.close();
			} catch (Exception e) {}
		}
		return bitmap;
	}

    /**
     * (缩放)重绘图片 
     * @param context Activity
     * @param bitmap
     * @return
     */
    public static Bitmap reDrawBitMap(Activity context,Bitmap bitmap){ 
    	DisplayMetrics dm = new DisplayMetrics(); 
    	context.getWindowManager().getDefaultDisplay().getMetrics(dm); 
	    int rHeight = dm.heightPixels; 
	    int rWidth = dm.widthPixels; 
		//float rHeight=dm.heightPixels/dm.density+0.5f; 
		//float rWidth=dm.widthPixels/dm.density+0.5f; 
		//int height=bitmap.getScaledHeight(dm); 
		//int width = bitmap.getScaledWidth(dm); 
	    int height=bitmap.getHeight(); 
	    int width = bitmap.getWidth(); 
	    float zoomScale; 
	    /**方式1**/
//	    if(rWidth/rHeight>width/height){//以高为准 
//	    	zoomScale=((float) rHeight) / height; 
//	    }else{ 
//	    	//if(rWidth/rHeight<width/height)//以宽为准 
//	    	zoomScale=((float) rWidth) / width; 
//	    } 
	    /**方式2**/
//	    if(width*1.5 >= height) {//以宽为准
//	    	if(width >= rWidth)
//	    		zoomScale = ((float) rWidth) / width;
//	    	else
//	    		zoomScale = 1.0f;
//	    }else {//以高为准
//	    	if(height >= rHeight)
//	    		zoomScale = ((float) rHeight) / height;
//	    	else
//	    		zoomScale = 1.0f;
//	    }
	    /**方式3**/
	    if(width >= rWidth)
    		zoomScale = ((float) rWidth) / width;
    	else
    		zoomScale = 1.0f;
	    //创建操作图片用的matrix对象  
	    Matrix matrix = new Matrix();  
	    //缩放图片动作  
	    matrix.postScale(zoomScale, zoomScale);  
	    Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);  
	    return resizedBitmap; 
    }

    /**
     * detect bytes's image type
     * @param bytes 2~8 byte at beginning of the image file  
     * @return image mimetype or null if the file is not image
     */
    public static String getImageType(byte[] bytes) {
        if (isJPEG(bytes)) {
            return "image/jpeg";
        }
        if (isGIF(bytes)) {
            return "image/gif";
        }
        if (isPNG(bytes)) {
            return "image/png";
        }
        if (isBMP(bytes)) {
            return "application/x-bmp";
        }
        return null;
    }

    private static boolean isJPEG(byte[] b) {
        if (b.length < 2) {
            return false;
        }
        return (b[0] == (byte)0xFF) && (b[1] == (byte)0xD8);
    }

    private static boolean isGIF(byte[] b) {
        if (b.length < 6) {
            return false;
        }
        return b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8'
                && (b[4] == '7' || b[4] == '9') && b[5] == 'a';
    }

    private static boolean isPNG(byte[] b) {
        if (b.length < 8) {
            return false;
        }
        return (b[0] == (byte) 137 && b[1] == (byte) 80 && b[2] == (byte) 78
                && b[3] == (byte) 71 && b[4] == (byte) 13 && b[5] == (byte) 10
                && b[6] == (byte) 26 && b[7] == (byte) 10);
    }

    private static boolean isBMP(byte[] b) {
        if (b.length < 2) {
            return false;
        }
        return (b[0] == 0x42) && (b[1] == 0x4d);
    }
}
