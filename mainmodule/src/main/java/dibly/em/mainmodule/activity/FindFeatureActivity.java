package dibly.em.mainmodule.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;

import dibly.em.mainmodule.R;
import dibly.em.mainmodule.staticc.StaticFunction;

/**
 * Created by USER on 7/7/2015.
 */
public class FindFeatureActivity extends Activity
{

    ImageView imageView1;
    ImageView imageView2;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_feature);
        initView();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, new OpenCVLoaderCallback(this));
    }

    private void initView()
    {
        imageView1 = (ImageView) findViewById(R.id.image_1);
        imageView2 = (ImageView) findViewById(R.id.image_2);

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
                    detectObjectFull(R.drawable.girl1, R.drawable.car);
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }

    }


    private void detectObjectFull(int objectToFind, int sceneImage)
    {

        try
        {


            Mat img_object = Utils.loadResource(FindFeatureActivity.this, objectToFind, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
            Mat img_scene = Utils.loadResource(FindFeatureActivity.this, sceneImage, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
            MatOfKeyPoint keypoints_object = new MatOfKeyPoint();
            MatOfKeyPoint keypoints_scene = new MatOfKeyPoint();


            //-- Step 1: Detect the keypoints using SURF Detector
            FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.FAST);


            featureDetector.detect(img_scene, keypoints_scene);
            featureDetector.detect(img_object, keypoints_object);


            Mat outObject = new Mat();
            Mat outScene = new Mat();

            Features2d.drawKeypoints(img_object, keypoints_object, outObject);
            Features2d.drawKeypoints(img_scene, keypoints_scene, outScene);


            Bitmap bmp1 = StaticFunction.matToBitmap(outObject);
            Bitmap bmp2 = StaticFunction.matToBitmap(outScene);


            imageView1.setImageBitmap(bmp1);
            imageView2.setImageBitmap(bmp2);


        }
        catch (Exception ex)
        {
            Log.e("", "");

        }


    }


}
