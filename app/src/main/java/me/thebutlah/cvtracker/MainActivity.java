package me.thebutlah.cvtracker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }
        String inputFileName="test";
        String inputExtension = "png";
        String inputDir = getCacheDir().getAbsolutePath();  // use the cache directory for i/o
        String outputDir = getCacheDir().getAbsolutePath();
        String outputExtension = "png";
        String inputFilePath = inputDir + File.separator + inputFileName + "." + inputExtension;

        File cacheFile = new File(inputFilePath);
        if (!cacheFile.exists()) {
            Bitmap bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.test);
            saveBitmapToFile(cacheFile, bm, Bitmap.CompressFormat.PNG, 100);

        }


        Log.d (this.getClass().getSimpleName(), "loading " + inputFilePath + "...");
        Mat image = Imgcodecs.imread(inputFilePath);
        Log.d (this.getClass().getSimpleName(), "width of " + inputFileName + ": " + image.width());
// if width is 0 then it did not read your image.


// for the canny edge detection algorithm, play with these to see different results
        int threshold1 = 70;
        int threshold2 = 100;

        Mat im_canny = new Mat();  // you have to initialize output image before giving it to the Canny method
        Imgproc.Canny(image, im_canny, threshold1, threshold2);
        String cannyFilename = outputDir + File.separator + inputFileName + "_canny-" + threshold1 + "-" + threshold2 + "." + outputExtension;
        Log.d (this.getClass().getSimpleName(), "Writing " + cannyFilename);
        Imgcodecs.imwrite(cannyFilename, im_canny);
    }


    /**
     * Bitmap.CompressFormat can be PNG,JPEG or WEBP.
     *
     * quality goes from 1 to 100. (Percentage).
     *
     * dir you can get from many places like Environment.getExternalStorageDirectory() or mContext.getFilesDir()
     * depending on where you want to save the image.
     */
    public static boolean saveBitmapToFile(File imageFile, Bitmap bm,
                                           Bitmap.CompressFormat format, int quality) {

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);

            bm.compress(format,quality,fos);

            fos.close();

            return true;
        }
        catch (IOException e) {
            Log.e("app",e.getMessage());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }
}
