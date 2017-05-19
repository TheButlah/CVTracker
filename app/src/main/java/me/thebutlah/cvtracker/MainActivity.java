package me.thebutlah.cvtracker;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.engine.OpenCVEngineInterface;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements
        CameraBridgeViewBase.CvCameraViewListener2,
        View.OnTouchListener {

    public String TAG;

    private CameraView cameraView;
    private int width, height;

    private TextView fpsView;
    long time1 = System.currentTimeMillis(); //Older
    long time2 = System.currentTimeMillis(); //Newer

    Communicator comm;
    private ObjectTracker tracker;
    HOGDescriptor hog;

    /**
     * Called when OpenCV loads
     */
    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    cameraView.enableView();
                    cameraView.setOnTouchListener(MainActivity.this);
                    hog = new HOGDescriptor();
                    hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        this.height = metrics.heightPixels;
        this.width = metrics.widthPixels;
        this.TAG = getResources().getString(R.string.app_name) + "::Main";
        ;
        cameraView = (CameraView) findViewById(R.id.camera_view);
        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.setCvCameraViewListener(this);
        fpsView = (TextView) findViewById(R.id.fps);
        comm = new Communicator(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cameraView != null) cameraView.disableView();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, loaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraView != null) cameraView.disableView();
        Log.v(TAG, "onDestroy!");
    }


    /**
     * Bitmap.CompressFormat can be PNG,JPEG or WEBP.
     * <p>
     * quality goes from 1 to 100. (Percentage).
     * <p>
     * dir you can get from many places like Environment.getExternalStorageDirectory() or mContext.getFilesDir()
     * depending on where you want to save the image.
     */
    public static boolean saveBitmapToFile(File imageFile, Bitmap bm,
                                           Bitmap.CompressFormat format, int quality) {

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);

            bm.compress(format, quality, fos);

            fos.close();

            return true;
        } catch (IOException e) {
            Log.e("app", e.getMessage());
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

    /**
     * This method is invoked when camera preview has started. After this method is invoked
     * the frames will start to be delivered to client via the onCameraFrame() callback.
     *
     * @param width  -  the width of the frames that will be delivered
     * @param height - the height of the frames that will be delivered
     */
    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    /**
     * This method is invoked when camera preview has been stopped for some reason.
     * No frames will be delivered via onCameraFrame() callback after this method is called.
     */
    @Override
    public void onCameraViewStopped() {

    }

    private Mat img;
    private MatOfRect locations;
    private MatOfDouble weights;
    private Mat down;
    List<Rect> boxes;
    Object boxLock = new Object();
    /**
     * This method is invoked when delivery of the frame needs to be done.
     * The returned values - is a modified frame which needs to be displayed on the screen.
     * TODO: pass the parameters specifying the format of the frame (BPP, YUV or RGB and etc)
     *
     * @param inputFrame
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        img = inputFrame.rgba();
        Core.flip(img, img, -1);
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGBA2RGB);
        if (locations == null) locations = new MatOfRect();
        if (weights == null) weights = new MatOfDouble();
        if (down == null) down = new Mat();
        if (tracker == null) {
            tracker = new ObjectTracker(this);
            tracker.execute(img, locations, weights, down);
        } else if (tracker.getStatus() == AsyncTask.Status.FINISHED) {
            synchronized (boxLock) {
                for (Rect r : boxes) {
                    Imgproc.rectangle(img, r.br(), r.tl(), new Scalar(255, 0, 0), 4);
                    double x = (r.br().x + r.tl().x) / 2.0;
                    byte center = (byte) (x / img.size().width * 127);
                    Log.d(TAG, String.format("Sending: %d", center & 0xFF));
                    comm.send(center);
                }
            }
            tracker = new ObjectTracker(this);
            tracker.execute(img, locations, weights, down);
        }

        //Imgproc.resize(down, result, originalSize);

        //if (centers.size() < 0) comm.send(centers.get(0).x)
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long delta = time2 - time1;
                double fps = 1000.0 / delta;
                fpsView.setText(String.format("FPS: %4f", fps));
            }
        });
        return img;
        //return inputFrame.rgba(); //Do nothing for now
    }

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        /*byte x = (byte) (event.getX()/width * 255);
        byte y = (byte) (event.getY()/height * 255);
        String str = String.format("(%d,%d)", x, y);
        comm.send(x);
        Log.d(TAG, str);
        return true;*/
        return false;
    }
}
