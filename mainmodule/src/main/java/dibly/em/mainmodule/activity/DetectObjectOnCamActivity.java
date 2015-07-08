package dibly.em.mainmodule.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import dibly.em.mainmodule.R;
import dibly.em.mainmodule.views.CameraView;

/**
 * Created by USER on 7/8/2015.
 */
public class DetectObjectOnCamActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener
{
    private static final String TAG = "OCVSample::Activity";

    private CameraView mOpenCvCameraView;
    private List<Camera.Size> mResolutionList;
    private MenuItem[] mEffectMenuItems;
    private SubMenu mColorEffectsMenu;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;


    Mat img_object;
    MatOfKeyPoint keypoints_object;
    MatOfKeyPoint keypoints_scene;
    FeatureDetector featureDetector;
    DescriptorExtractor extractor;
    Mat descriptors_object;
    Mat descriptors_scene;
    DescriptorMatcher matcher;
    MatOfDMatch matches;


    Mat H;
    MatOfPoint2f obj_corners;
    Point pointObjConners[] = new Point[4];
    MatOfPoint2f scene_corners;
    double min_dist = 0;

    double threeMinDist;

    List<DMatch> listMatches;
    List<DMatch> listGoodMatches;// = new ArrayList<>();

    List<KeyPoint> listKeyPointObject;//= keypoints_object.toList();
    List<KeyPoint> listKeyPointScene;//= keypoints_scene.toList();

    MatOfPoint2f obj;// = new MatOfPoint2f();
    MatOfPoint2f scene;// = new MatOfPoint2f();

    Mat img_scene;

    DetectThread detectThread;
    Thread thread;
    boolean isRun = false;


    InitThread initThread;
    Thread thread2;


    Point p0;
    Point p1;
    Point p2;
    Point p3;


    MatOfDMatch good_matches;// = new MatOfDMatch();

    List<Point> listPointScene;

    List<Point> listResult;

    //ReentrantReadWriteLock reentrantReadWriteLock;

    //Lock readLock;
    //Lock writeLock;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)
    {
        @Override
        public void onManagerConnected(int status)
        {
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(DetectObjectOnCamActivity.this);
                }
                break;
                default:
                {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public DetectObjectOnCamActivity()
    {
        //Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);

        mOpenCvCameraView = (CameraView) findViewById(R.id.java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        //reentrantReadWriteLock = new ReentrantReadWriteLock();
        //readLock = reentrantReadWriteLock.readLock();
        //writeLock = reentrantReadWriteLock.writeLock();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        //thread.interrupt();
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();

        isRun = false;
        //thread.interrupt();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy()
    {
        super.onDestroy();
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();
        isRun = false;

    }

    public void onCameraViewStarted(int width, int height)
    {
        try
        {
            //-- Step 1: Detect the keypoints using SURF Detector
            featureDetector = FeatureDetector.create(FeatureDetector.FAST);

            //-- Step 2: Calculate descriptors (feature vectors)
            extractor = DescriptorExtractor.create(DescriptorExtractor.FREAK);

            //-- Step 3: Matching descriptor vectors using FLANN matcher
            matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_L1);

            img_object = Utils.loadResource(this, R.drawable.cardobj1, Highgui.CV_LOAD_IMAGE_GRAYSCALE);

            img_scene = new Mat();

            matches = new MatOfDMatch();

            listGoodMatches = new ArrayList<>();

            keypoints_object = new MatOfKeyPoint();
            keypoints_scene = new MatOfKeyPoint();
            descriptors_object = new Mat();
            descriptors_scene = new Mat();

            obj = new MatOfPoint2f();
            scene = new MatOfPoint2f();

            H = new Mat();

            obj_corners = new MatOfPoint2f();
            scene_corners = new MatOfPoint2f();

            min_dist = Double.MAX_VALUE;


            listPointScene = new ArrayList<>();
            listResult = new ArrayList<>();


            initThread = null;

            if (initThread == null)
            {
                initThread = new InitThread();
                thread2 = new Thread(initThread);
                thread2.setPriority(Thread.MAX_PRIORITY);
                thread2.start();
            }


            detectThread = null;

            if (detectThread == null)
            {
                detectThread = new DetectThread();
                thread = new Thread(detectThread);
                thread.setPriority(Thread.MAX_PRIORITY);
                thread.start();
            }

        }
        catch (Exception ex)
        {

        }
    }

    private void initObject()
    {
        featureDetector.detect(img_object, keypoints_object);
        extractor.compute(img_object, keypoints_object, descriptors_object);
    }


    public void onCameraViewStopped()
    {
        isRun = false;

    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {

        return detectObjectLiteVersion(inputFrame.gray());

    }

    public synchronized void detectObjectWithPoint()
    {
        featureDetector.detect(img_scene, keypoints_scene);
        extractor.compute(img_scene, keypoints_scene, descriptors_scene);

        if (!descriptors_scene.empty())
        {
            matcher.match(descriptors_object, descriptors_scene, matches);

            listMatches = matches.toList();

            int size = descriptors_object.rows();


            //-- Quick calculation of max and min distances between keypoints
            for (int i = 0; i < size; i++)
            {
                double dist = listMatches.get(i).distance;
                if (dist < min_dist)
                {
                    min_dist = dist;
                }
            }

            threeMinDist = 3 * min_dist;

            listGoodMatches.removeAll(listGoodMatches);

            for (int i = 0; i < size; i++)
            {
                DMatch dMatch = listMatches.get(i);

                float distance = dMatch.distance;

                if (distance < threeMinDist)
                {
                    listGoodMatches.add(dMatch);
                }
            }


            if (listGoodMatches.size() > 4)
            {
                Point pointObj[] = new Point[listGoodMatches.size()];
                Point pointScene[] = new Point[listGoodMatches.size()];

                listKeyPointObject = keypoints_object.toList();
                listKeyPointScene = keypoints_scene.toList();

                for (int i = 0; i < listGoodMatches.size(); i++)
                {
                    //-- Get the keypoints from the good matches
                    pointObj[i] = listKeyPointObject.get(listGoodMatches.get(i).queryIdx).pt;
                    pointScene[i] = listKeyPointScene.get(listGoodMatches.get(i).trainIdx).pt;
                }


                obj.fromArray(pointObj);
                scene.fromArray(pointScene);

                H = Calib3d.findHomography(obj, scene, Calib3d.RANSAC, 3);

                pointObjConners[0] = new Point(0, 0);
                pointObjConners[1] = new Point(img_object.cols(), 0);
                pointObjConners[2] = new Point(img_object.cols(), img_object.rows());
                pointObjConners[3] = new Point(0, img_object.rows());

                obj_corners.fromArray(pointObjConners);


                Core.perspectiveTransform(obj_corners, scene_corners, H);


                p0 = new Point(scene_corners.toList().get(0).x, scene_corners.toList().get(0).y + 0);
                p1 = new Point(scene_corners.toList().get(1).x, scene_corners.toList().get(1).y + 0);
                p2 = new Point(scene_corners.toList().get(2).x, scene_corners.toList().get(2).y + 0);
                p3 = new Point(scene_corners.toList().get(3).x, scene_corners.toList().get(3).y + 0);


            }


        }
    }

    public synchronized void detectObject()
    {
        featureDetector.detect(img_scene, keypoints_scene);
        extractor.compute(img_scene, keypoints_scene, descriptors_scene);


        if (!descriptors_scene.empty())
        {
            matcher.match(descriptors_object, descriptors_scene, matches);

            listMatches = matches.toList();


            int size = descriptors_object.rows();
            //-- Quick calculation of max and min distances between keypoints
            for (int i = 0; i < size; i++)
            {
                double dist = listMatches.get(i).distance;
                if (dist < min_dist)
                {
                    min_dist = dist;
                }
            }

            threeMinDist = 5 * min_dist;

            listGoodMatches.removeAll(listGoodMatches);

            for (int i = 0; i < size; i++)
            {
                DMatch dMatch = listMatches.get(i);

                float distance = dMatch.distance;

                if (distance < threeMinDist)
                {
                    listGoodMatches.add(dMatch);
                }
            }


            listKeyPointScene = keypoints_scene.toList();


            listPointScene.removeAll(listPointScene);
            for (int i = 0; i < listGoodMatches.size(); i++)
            {

                listPointScene.add(listKeyPointScene.get(listGoodMatches.get(i).trainIdx).pt);
            }

            Point centerPoint = findCenterPointFromPointList(listPointScene);
            listResult = findGroupOFCenterPoints(centerPoint, listPointScene, 50);


        }

    }

    private List<Point> findGroupOFCenterPoints(Point centerPoint, List<Point> listPoint, double distance)
    {
        List<Point> listCenterPoints = new ArrayList<>();
        for (int i = 0; i < listPoint.size(); i++)
        {
            Point subPoint = listPoint.get(i);
            double dis = getDistance(centerPoint, subPoint);
            if (dis < distance)
            {
                listCenterPoints.add(subPoint);
            }
        }

        return listCenterPoints;
    }

    private Point findCenterPointFromPointList(List<Point> listPoint)
    {
        Point centerPoint = null;
        double minDistance = 999999999;

        for (int i = 0; i < listPoint.size(); i++)
        {
            double totalDistance = 0;
            Point mainPoint = listPoint.get(i);
            for (int j = 0; j < listPoint.size(); j++)
            {
                Point subPoint = listPoint.get(j);
                totalDistance = totalDistance + getDistance(mainPoint, subPoint);
            }

            if (totalDistance < minDistance)
            {
                minDistance = totalDistance;
                centerPoint = listPoint.get(i);
            }
        }

        return centerPoint;

    }

    private double getDistance(Point p1, Point p2)
    {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    class DetectThread implements Runnable
    {
        @Override
        public void run()
        {

            while (true)
            {
                if (isRun == true)
                {
                    detectObject();
                    //detectObjectWithPoint();
                }

            }


        }
    }

    class InitThread implements Runnable
    {
        @Override
        public void run()
        {
            initObject();
        }
    }


    private Mat detectObjectLiteVersion(Mat img)
    {
        img_scene = img.clone();

        isRun = true;


        if (listResult != null && !listResult.isEmpty())
        {
            for (int i = 0; i < listResult.size(); i++)
            {
                Core.circle(img, listResult.get(i), 5, Scalar.all(1), 2);
            }

        }

//        Scalar scalar = new Scalar(0, 255, 0);
//
//        if(p0 != null && p1 != null && p2 != null && p3 != null)
//        {
//
//            Core.line(img, p0, p1, scalar, 4);
//            Core.line(img, p1, p2, scalar, 4);
//            Core.line(img, p2, p3, scalar, 4);
//            Core.line(img, p3, p0, scalar, 4);
//
//        }


        return img;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {

        return true;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        Log.i(TAG, "onTouch event");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = Environment.getExternalStorageDirectory().getPath() +
                "/sample_picture_" + currentDateandTime + ".jpg";
        mOpenCvCameraView.takePicture(fileName);
        Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
        return false;
    }


}
