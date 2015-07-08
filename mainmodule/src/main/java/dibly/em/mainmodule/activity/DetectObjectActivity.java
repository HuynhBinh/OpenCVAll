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
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;

import java.util.ArrayList;
import java.util.List;

import dibly.em.mainmodule.R;
import dibly.em.mainmodule.staticc.StaticFunction;

/**
 * Created by USER on 7/7/2015.
 */
public class DetectObjectActivity extends Activity
{

    ImageView imageViewOriginal;
    ImageView imageViewObjectToFind;
    ImageView imageViewFinalResult;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_matching);
        initView();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, new OpenCVLoaderCallback(this));
    }

    private void initView()
    {
        imageViewOriginal = (ImageView) findViewById(R.id.image_original);
        imageViewObjectToFind = (ImageView) findViewById(R.id.image_object);
        imageViewFinalResult = (ImageView) findViewById(R.id.image_final_result);
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
                    detectObjectFull(R.drawable.cardobj1, R.drawable.cardscene);
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

            imageViewOriginal.setImageResource(R.drawable.cardscene);
            imageViewObjectToFind.setImageResource(R.drawable.cardobj1);


            Mat img_object = Utils.loadResource(DetectObjectActivity.this, objectToFind, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
            Mat img_scene = Utils.loadResource(DetectObjectActivity.this, sceneImage, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
            MatOfKeyPoint keypoints_object = new MatOfKeyPoint();
            MatOfKeyPoint keypoints_scene = new MatOfKeyPoint();


            //-- Step 1: Detect the keypoints using SURF Detector
            FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.FAST);


            featureDetector.detect(img_scene, keypoints_scene);
            featureDetector.detect(img_object, keypoints_object);


            //-- Step 2: Calculate descriptors (feature vectors)
            DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.FREAK);

            Mat descriptors_object = new Mat();
            Mat descriptors_scene = new Mat();

            extractor.compute(img_object, keypoints_object, descriptors_object);
            extractor.compute(img_scene, keypoints_scene, descriptors_scene);

            //-- Step 3: Matching descriptor vectors using FLANN matcher
            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_L1);
            MatOfDMatch matches = new MatOfDMatch();
            matcher.match(descriptors_object, descriptors_scene, matches);

            List<DMatch> listMatches = matches.toList();
            //double max_dist = 0;
            double min_dist = 999999999;

            //-- Quick calculation of max and min distances between keypoints
            for (int i = 0; i < descriptors_object.rows(); i++)
            {
                double dist = listMatches.get(i).distance;
                if (dist < min_dist) min_dist = dist;
                //if (dist > max_dist) max_dist = dist;
            }

            Log.e("Min", min_dist + "");
            //Log.e("Max" , max_dist+"");


            //-- Draw only "good" matches (i.e. whose distance is less than 3*min_dist )
            MatOfDMatch good_matches = new MatOfDMatch();

            List<DMatch> listGoodMatches = new ArrayList<>();

            if (min_dist == 0)
            {
                listGoodMatches = listMatches;
            }
            else
            {
                int size = descriptors_object.rows();


                for (int i = 0; i < size; i++)
                {
                    float distance = listMatches.get(i).distance;

                    float threeMinDist = (float) (2.5 * min_dist);

                    if (distance < threeMinDist)
                    {
                        //good_matches.push_back(matches.col(i));
                        listGoodMatches.add(listMatches.get(i));
                    }
                }
            }


            good_matches.fromList(listGoodMatches);


            Log.e("Matches", listMatches.size() + "");
            Log.e("Good Matches", listGoodMatches.size() + "");

            Mat img_matches = new Mat();
            MatOfByte matOfByte = new MatOfByte();
            Features2d.drawMatches(img_object, keypoints_object, img_scene, keypoints_scene, good_matches, img_matches, Scalar.all(-1), Scalar.all(-1), matOfByte, Features2d.NOT_DRAW_SINGLE_POINTS);


            //List<Point> listPointObj = new ArrayList<>();
            //List<Point> listPointScene = new ArrayList<>();

            Point pointObj[] = new Point[listGoodMatches.size()];
            Point pointScene[] = new Point[listGoodMatches.size()];

            List<KeyPoint> listKeyPointObject = keypoints_object.toList();
            List<KeyPoint> listKeyPointScene = keypoints_scene.toList();

            for (int i = 0; i < listGoodMatches.size(); i++)
            {
                //-- Get the keypoints from the good matches
                pointObj[i] = listKeyPointObject.get(listGoodMatches.get(i).queryIdx).pt;
                pointScene[i] = listKeyPointScene.get(listGoodMatches.get(i).trainIdx).pt;

            }

            MatOfPoint2f obj = new MatOfPoint2f(pointObj);
            MatOfPoint2f scene = new MatOfPoint2f(pointScene);


            Mat H = Calib3d.findHomography(obj, scene, Calib3d.RANSAC, 9);


            MatOfPoint2f obj_corners = new MatOfPoint2f();
            Point pointObjConners[] = new Point[4];
            pointObjConners[0] = new Point(0, 0);
            pointObjConners[1] = new Point(img_object.cols(), 0);
            pointObjConners[2] = new Point(img_object.cols(), img_object.rows());
            pointObjConners[3] = new Point(0, img_object.rows());

            obj_corners.fromArray(pointObjConners);


            MatOfPoint2f scene_corners = new MatOfPoint2f();
            Core.perspectiveTransform(obj_corners, scene_corners, H);


            //-- Draw lines between the corners (the mapped object in the scene - image_2 )
            Point p0 = new Point(scene_corners.toList().get(0).x + img_object.cols(), scene_corners.toList().get(0).y + 0);
            Point p1 = new Point(scene_corners.toList().get(1).x + img_object.cols(), scene_corners.toList().get(1).y + 0);
            Point p2 = new Point(scene_corners.toList().get(2).x + img_object.cols(), scene_corners.toList().get(2).y + 0);
            Point p3 = new Point(scene_corners.toList().get(3).x + img_object.cols(), scene_corners.toList().get(3).y + 0);


            Scalar scalar = new Scalar(0, 255, 0);

            Core.line(img_matches, p0, p1, scalar, 4);
            Core.line(img_matches, p1, p2, scalar, 4);
            Core.line(img_matches, p2, p3, scalar, 4);
            Core.line(img_matches, p3, p0, scalar, 4);


            Bitmap bitmap = StaticFunction.matToBitmap(img_matches);

            imageViewFinalResult.setImageBitmap(bitmap);

        }
        catch (Exception ex)
        {
            Log.e("", "");

        }


    }


}
