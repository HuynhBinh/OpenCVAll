package org.opencv.samples.tutorial3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Tutorial3Activity extends Activity implements CvCameraViewListener2, OnTouchListener
{
    private static final String TAG = "OCVSample::Activity";

    private Tutorial3View mOpenCvCameraView;
    private List<Size> mResolutionList;
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

    Point p0;
    Point p1;
    Point p2;
    Point p3;


    MatOfDMatch good_matches;// = new MatOfDMatch();

    List<Point> listPointScene;

    ReentrantReadWriteLock reentrantReadWriteLock;

    Lock readLock;
    Lock writeLock;


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
                    mOpenCvCameraView.setOnTouchListener(Tutorial3Activity.this);
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

    public Tutorial3Activity()
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

        setContentView(R.layout.tutorial3_surface_view);

        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        reentrantReadWriteLock = new ReentrantReadWriteLock();
        readLock = reentrantReadWriteLock.readLock();
        writeLock = reentrantReadWriteLock.writeLock();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        isRun = false;
        thread.interrupt();
        thread = null;
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();
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
    }

    public void onCameraViewStarted(int width, int height)
    {
        try
        {

            detectThread = null;

            if (detectThread == null)
            {
                detectThread = new DetectThread();
                thread = new Thread(detectThread);
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
            }


            //-- Step 1: Detect the keypoints using SURF Detector
            featureDetector = FeatureDetector.create(FeatureDetector.FAST);

            //-- Step 2: Calculate descriptors (feature vectors)
            extractor = DescriptorExtractor.create(DescriptorExtractor.FREAK);

            //-- Step 3: Matching descriptor vectors using FLANN matcher
            matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_L1);

            img_object = Utils.loadResource(this, R.drawable.cardobj, Highgui.CV_LOAD_IMAGE_GRAYSCALE);

            img_scene = new Mat();
            //img_scene = Utils.loadResource(this, R.drawable.cardscene, Highgui.CV_LOAD_IMAGE_GRAYSCALE);

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
            min_dist = 9999999;

            p0 = new Point(0, 0);
            p1 = new Point(0, 0);
            p2 = new Point(0, 0);
            p3 = new Point(0, 0);

            good_matches = new MatOfDMatch();

            listPointScene = new ArrayList<>();


        }
        catch (Exception ex)
        {

        }
    }

    public void onCameraViewStopped()
    {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame)
    {


        return detectObjectLiteVersion(inputFrame.gray());

    }


    private void detectObject()
    {


        readLock.lock();

        featureDetector.detect(img_scene, keypoints_scene);
        featureDetector.detect(img_object, keypoints_object);

        extractor.compute(img_object, keypoints_object, descriptors_object);
        extractor.compute(img_scene, keypoints_scene, descriptors_scene);

        readLock.unlock();


        if (!descriptors_scene.empty())
        {
            matcher.match(descriptors_object, descriptors_scene, matches);

            //readLock.unlock();

            //
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

            Log.e("Min", min_dist + "");


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

            //good_matches.fromList(listGoodMatches);


            Log.e("Matches", listMatches.size() + "");
            Log.e("Good Matches", listGoodMatches.size() + "");
            //


            if (listGoodMatches.size() > 4)
            {
                Point pointObj[] = new Point[listGoodMatches.size()];
                Point pointScene[] = new Point[listGoodMatches.size()];

                listKeyPointObject = keypoints_object.toList();
                listKeyPointScene = keypoints_scene.toList();


                //listPointScene.removeAll(listPointScene);
                for (int i = 0; i < listGoodMatches.size(); i++)
                {
                    //-- Get the keypoints from the good matches
                    pointObj[i] = listKeyPointObject.get(listGoodMatches.get(i).queryIdx).pt;
                    pointScene[i] = listKeyPointScene.get(listGoodMatches.get(i).trainIdx).pt;

                    //listPointScene.add(listKeyPointScene.get(listGoodMatches.get(i).trainIdx).pt);
                }


                obj.fromArray(pointObj);
                scene.fromArray(pointScene);

                Log.e("Before findHomography", "");

                H = Calib3d.findHomography(obj, scene, Calib3d.RANSAC, 9);

                Log.e("AFTERRR findHomography", "");

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

                Log.e("POINT THREAD", p0.toString() + p1.toString() + p2.toString() + p3.toString());


                Log.e("detect ok", "detect ok");

            }


        }
        else
        {
            Log.e("No descritor", "No descritor");

            //readLock.unlock();
        }
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

                }

            }


        }
    }


    private Mat detectObjectLiteVersion(Mat img)
    {
        try
        {
            //writeLock.lock();

            try
            {
                img_scene = img.clone();
            }
            finally
            {
                //writeLock.unlock();
            }


            isRun = true;


            //detectObject();


           /* if (listPointScene != null && !listPointScene.isEmpty())
            {
                for (int i = 0; i < listPointScene.size(); i++)
                {
                    Core.circle(img, listPointScene.get(i), 5, Scalar.all(1), 2);

                }
            }*/


            Scalar scalar = new Scalar(0, 255, 0);

            //Log.e("POINT CAP", p0.toString() + p1.toString());
            Core.line(img, p0, p1, scalar, 4);
            Core.line(img, p1, p2, scalar, 4);
            Core.line(img, p2, p3, scalar, 4);
            Core.line(img, p3, p0, scalar, 4);


            return img;


        }
        catch (Exception ex)
        {
            Log.e("", "");
            return null;

        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        List<String> effects = mOpenCvCameraView.getEffectList();

        if (effects == null)
        {
            Log.e(TAG, "Color effects are not supported by device!");
            return true;
        }

        mColorEffectsMenu = menu.addSubMenu("Color Effect");
        mEffectMenuItems = new MenuItem[effects.size()];

        int idx = 0;
        ListIterator<String> effectItr = effects.listIterator();
        while (effectItr.hasNext())
        {
            String element = effectItr.next();
            mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE, element);
            idx++;
        }

        mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<Size> resolutionItr = mResolutionList.listIterator();
        idx = 0;
        while (resolutionItr.hasNext())
        {
            Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE, Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
            idx++;
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item.getGroupId() == 1)
        {
            mOpenCvCameraView.setEffect((String) item.getTitle());
            Toast.makeText(this, mOpenCvCameraView.getEffect(), Toast.LENGTH_SHORT).show();
        }
        else if (item.getGroupId() == 2)
        {
            int id = item.getItemId();
            Size resolution = mResolutionList.get(id);
            mOpenCvCameraView.setResolution(resolution);
            resolution = mOpenCvCameraView.getResolution();
            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
        }

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
