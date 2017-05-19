package me.thebutlah.cvtracker;

import android.os.AsyncTask;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * Created by ryan on 5/19/17.
 */

public class ObjectTracker extends AsyncTask<Mat, Object, List<Rect>>{

    public final String TAG;

    private MainActivity context;

    public ObjectTracker(MainActivity context) {
        this.context = context;
        this.TAG = context.getResources().getString(R.string.app_name) + "::Tracker";

    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected synchronized List<Rect> doInBackground(Mat... params) {
        if (params == null || params.length != 4) return null;
        Mat img = params[0];
        MatOfRect locations = (MatOfRect) params[1];
        MatOfDouble weights = (MatOfDouble) params[2];
        Mat down = params[3];
        Size originalSize = img.size();
        Size downSize = new Size();
        Imgproc.resize(img, down, downSize, 0.15, 0.15, Imgproc.INTER_NEAREST);
        downSize = down.size();
        //Log.d(TAG, Integer.toString(img.type()));
        context.hog.detectMultiScale(
            down, locations, weights,
            0.0, new Size(4, 4), new Size(16, 16), 1.25, 2.0, false
        );
        List<Rect> result = locations.toList();
        for (int i=0; i<result.size(); i++) {
            Rect r = result.get(i);
            //Log.v(TAG, String.format("(%f, %f)", r.br().x, r.tl().x));
            /*Point br = r.br();
            Point tl = r.tl();*/
            Point br = scalePoint(r.br(), originalSize, downSize);
            Point tl = scalePoint(r.tl(), originalSize, downSize);
            Rect scaled = new Rect(br, tl);
            result.set(i, scaled);
        }
        long currentTime = System.currentTimeMillis();
        context.time1 = context.time2;
        context.time2 = currentTime;
        return result;
    }


    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}.</p>
     *
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @param result The result of the operation computed by {@link #doInBackground}.
     *
     * @see #onPreExecute
     * @see #doInBackground
     * @see #onCancelled(Object)
     */
    @Override
    protected void onPostExecute(List<Rect> result) {
        synchronized (context.boxLock) {
            context.boxes = result;
        }
    }

    private static Point scalePoint(Point p, Size original, Size current){
        double x_factor = original.width/current.width;
        double y_factor = original.height/current.height;
        Point result = new Point();
        result.x = p.x * x_factor;
        result.y = p.y * y_factor;
        return result;
    }
}
