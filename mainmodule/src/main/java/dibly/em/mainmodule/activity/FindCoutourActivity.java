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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import dibly.em.mainmodule.R;

/**
 * Created by USER on 7/7/2015.
 */
public class FindCoutourActivity extends Activity
{

    ImageView imageViewFinalResult;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_coutour);
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
                    findContours(R.drawable.kkc, 50); // try with different value to see the different. The bigger the value, the less details the image
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }

    }


    private void findContours(int inputImage, int threshole)
    {
        try
        {
            int thresh = threshole;
            //int max_thresh = 255;
            // variable
            Mat source;
            Mat source_grey = new Mat();


            // load image
            source = Utils.loadResource(FindCoutourActivity.this, inputImage);

            // convert image to gray
            Imgproc.cvtColor(source, source_grey, Imgproc.COLOR_BGR2GRAY);


            // blur the image for better result
            Imgproc.GaussianBlur(source_grey, source_grey, new Size(3, 3), 2, 2);


            Mat canny_output = new Mat();
            List<MatOfPoint> matOfPointList = new ArrayList<>();
            Mat hierarchy = new Mat();

            Imgproc.Canny(source_grey, canny_output, thresh, thresh * 2, 3, false);
            Imgproc.findContours(canny_output, matOfPointList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));


            Mat draw_mat = Mat.zeros(canny_output.size(), CvType.CV_8UC3);


            for (int i = 0; i < matOfPointList.size(); i++)
            {
                Scalar color = new Scalar(123, 0, 70);
                Imgproc.drawContours(draw_mat, matOfPointList, i, color, 2, 8, hierarchy, 0, new Point());

            }


            // convert mat to bitmap for display
            Bitmap bitmap = Bitmap.createBitmap(draw_mat.cols(), draw_mat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(draw_mat, bitmap);

            imageViewFinalResult.setImageBitmap(bitmap);

        }
        catch (Exception ex)
        {

        }


    }


}
