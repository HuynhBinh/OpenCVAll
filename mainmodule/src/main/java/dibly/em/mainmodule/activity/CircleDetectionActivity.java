package dibly.em.mainmodule.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import dibly.em.mainmodule.R;

/**
 * Created by USER on 7/7/2015.
 */
public class CircleDetectionActivity extends Activity
{

    ImageView imageViewFinalResult;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_detection);
        initView();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, new OpenCVLoaderCallback(this));
    }

    private void initView()
    {

        imageViewFinalResult = (ImageView) findViewById(R.id.imageViewResult);
    }


    private class OpenCVLoaderCallback extends BaseLoaderCallback
    {
        private Context mContext;

        public OpenCVLoaderCallback(Context context)
        {
            super(context);
            mContext = context;
        }

        @Override
        public void onManagerConnected(int status)
        {
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                    detectCircle(R.drawable.circle);
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }

    }


    private void detectCircle(int imageWithCircle)
    {
        try
        {

            // variable
            Mat source;
            Mat source_grey = new Mat();


            // to store circle that the function found
            MatOfPoint3f matOfPoint3f = new MatOfPoint3f();

            // load image
            source = Utils.loadResource(CircleDetectionActivity.this, imageWithCircle);

            // convert image to gray
            Imgproc.cvtColor(source, source_grey, Imgproc.COLOR_BGR2GRAY);


            // blur the image for better result
            Imgproc.GaussianBlur(source_grey, source_grey, new Size(9, 9), 2, 2);


            // find circle
            Imgproc.HoughCircles(source_grey, matOfPoint3f, Imgproc.CV_HOUGH_GRADIENT, 3, source_grey.rows() / 8, 200, 100, 0, 0);


            // convert mat 3 point to list
            List<Point3> point3List = new ArrayList<>();
            point3List = matOfPoint3f.toList();


            // loop and draw circle that are found
            for (int i = 0; i < point3List.size(); i++)
            {

                Point center = new Point(point3List.get(i).x, point3List.get(i).y);

                int radius = (int) point3List.get(i).z;

                Scalar scalar = new Scalar(96, 128, 15);
                Core.circle(source, center, 3, scalar, -1, 8, 0);

                Core.circle(source, center, radius + 7, scalar, 5, 8, 0);

                // This si code C++ sample
               /* Point center (cvRound(circles[i][0]), cvRound(circles[i][1]));
                int radius = cvRound(circles[i][2]);
                // circle center
                circle(src, center, 3, Scalar(0, 255, 0), -1, 8, 0);
                // circle outline
                circle(src, center, radius, Scalar(0, 0, 255), 3, 8, 0);*/

            }


            // convert to bitmap and display
            Bitmap bmpTmp = Bitmap.createBitmap(source.cols(), source.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(source, bmpTmp);

            imageViewFinalResult.setImageBitmap(bmpTmp);

        }
        catch (Exception ex)
        {

        }
    }


}
