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
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import dibly.em.mainmodule.R;
import dibly.em.mainmodule.staticc.StaticFunction;

/**
 * Created by USER on 7/7/2015.
 */
public class MultipleChoiceActivity extends Activity
{

    ImageView imageView1;
    ImageView imageView2;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_choice);
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
                    detectObjectFull();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }

    }

    private Point computeIntersect(double[] a, double[] b)
    {
        double x1 = a[0], y1 = a[1], x2 = a[2], y2 = a[3];
        double x3 = b[0], y3 = b[1], x4 = b[2], y4 = b[3];

        double d = ((double) (x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4));


        Point pt = new Point();
        pt.x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
        pt.y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
        return pt;


    }

    public boolean comparator(Point a, Point b)
    {
        return a.x < b.x;
    }

    List<Point> list4Corner = new ArrayList<>();

    void sortCorners(List<Point> corners, Point center)
    {

        List<Point> top, bot;
        top = new ArrayList<>();
        bot = new ArrayList<>();

        for (int i = 0; i < corners.size(); i++)
        {
            if (corners.get(i).y < center.y)
                top.add(corners.get(i));

            else
                bot.add(corners.get(i));
        }


        double minX = 9999999;
        double maxX = 0;
        int currentMin = 0;
        int currentMax = 0;
        for (int i = 0; i < top.size(); i++)
        {
            if (top.get(i).x < minX)
            {
                minX = top.get(i).x;
                currentMin = i;
            }

            if (top.get(i).x > maxX)
            {
                maxX = top.get(i).x;
                currentMax = i;
            }

        }
        Point tl = top.get(currentMin);
        Point tr = top.get(currentMax);


        minX = 9999999;
        maxX = 0;
        currentMin = 0;
        currentMax = 0;
        for (int i = 0; i < bot.size(); i++)
        {
            if (bot.get(i).x < minX)
            {
                minX = bot.get(i).x;
                currentMin = i;
            }

            if (bot.get(i).x > maxX)
            {
                maxX = bot.get(i).x;
                currentMax = i;
            }

        }
        Point bl = bot.get(currentMin);
        Point br = bot.get(currentMax);

        list4Corner.add(tl);
        list4Corner.add(tr);
        list4Corner.add(br);
        list4Corner.add(bl);


    }


    private void detectObjectFull()
    {
        try
        {
            //blur the image
            Mat image = Utils.loadResource(MultipleChoiceActivity.this, R.drawable.test6, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
            Mat colorImage = new Mat();
            Imgproc.cvtColor(image, colorImage, Imgproc.COLOR_GRAY2BGR);

            Mat image3 = new Mat();
            Imgproc.cvtColor(image, image3, Imgproc.COLOR_GRAY2BGR);

            Size size = new Size(3, 3);
            Imgproc.GaussianBlur(image, image, size, 0);

            // black and white
            Imgproc.adaptiveThreshold(image, image, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 75, 10);
            Core.bitwise_not(image, image);

            // detect line
            Mat lines = new Mat();
            Imgproc.HoughLinesP(image, lines, 1, Math.PI / 180, 80, 400, 10);

            for (int x = 0; x < lines.cols(); x++)
            {
                double[] vec = lines.get(0, x);
                double x1 = vec[0],
                        y1 = vec[1],
                        x2 = vec[2],
                        y2 = vec[3];
                Point start = new Point(x1, y1);
                Point end = new Point(x2, y2);

                Core.line(colorImage, start, end, new Scalar(255, 0, 0), 3);


            }


            // find all the corners of the square
            List<Point> corners = new ArrayList<>();
            for (int i = 0; i < lines.cols(); i++)
            {
                for (int j = i + 1; j < lines.cols(); j++)
                {
                    double[] a = lines.get(0, i);
                    double[] b = lines.get(0, j);


                    Point pt = computeIntersect(a, b);
                    if (pt.x >= 0 && pt.y >= 0 && pt.x < image.cols() && pt.y < image.rows())
                        corners.add(pt);
                }
            }


            //sort corners to find only 4 corners
            Point center = new Point(0, 0);
            for (int i = 0; i < corners.size(); i++)
            {
                center.x += corners.get(i).x;
                center.y += corners.get(i).y;
            }

            center.x /= corners.size();
            center.y /= corners.size();

            sortCorners(corners, center);


            // draw the 4 corners
            for (int i = 0; i < list4Corner.size(); i++)
            {
                Scalar scalar = new Scalar(0, 0, 0);
                if (i == 0)
                {
                    scalar = new Scalar(0, 0, 255); //blue
                }
                if (i == 1)
                {
                    scalar = new Scalar(0, 255, 0); //green
                }
                if (i == 2)
                {
                    scalar = new Scalar(255, 255, 0); //red
                }
                if (i == 3)
                {
                    scalar = new Scalar(0, 255, 255); //cayan
                }

                Core.circle(colorImage, list4Corner.get(i), 9, scalar, 2);
            }


            MatOfPoint matRect = new MatOfPoint();
            matRect.fromList(list4Corner);
            Rect rect = Imgproc.boundingRect(matRect);

            Core.rectangle(colorImage, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 255, 0), 2);


            // transform the rect into normal rect

            // create new MAT to store the rect
            Mat quad = Mat.zeros(rect.height, rect.width, CvType.CV_8UC3);

            // take 4 corners of the rect
            List<Point> quad_pts = new ArrayList<>();
            quad_pts.add(new Point(0, 0));
            quad_pts.add(new Point(quad.cols(), 0));
            quad_pts.add(new Point(quad.cols(), quad.rows()));
            quad_pts.add(new Point(0, quad.rows()));

            // prepare 2 mat for the transform
            MatOfPoint2f mat1 = new MatOfPoint2f();
            mat1.fromList(list4Corner);
            MatOfPoint2f mat2 = new MatOfPoint2f();
            mat2.fromList(quad_pts);
            // Get transformation matrix
            Mat transmtx = Imgproc.getPerspectiveTransform(mat1, mat2);
            // Apply perspective transformation
            Imgproc.warpPerspective(image3, quad, transmtx, quad.size());


            //Bitmap bitmap = StaticFunction.matToBitmap(quad);
            //imageView1.setImageBitmap(bitmap);

            Bitmap bitmap1 = StaticFunction.matToBitmap(quad);
            imageView2.setImageBitmap(bitmap1);


        }
        catch (Exception ex)
        {
            Log.e("EXcept", ex.getMessage());

        }


    }


}
