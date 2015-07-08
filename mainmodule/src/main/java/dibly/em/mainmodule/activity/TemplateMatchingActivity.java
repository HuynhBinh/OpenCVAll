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
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import dibly.em.mainmodule.R;

/**
 * Created by USER on 7/7/2015.
 */
public class TemplateMatchingActivity extends Activity
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
                    templateMatching(R.drawable.girl1, R.drawable.girltemp1);
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }

    }


    public void templateMatching(int originalImage, int objectToFindImage)
    {

        try
        {
            // set original image for view purpose
            imageViewOriginal.setImageResource(originalImage);

            // create new MAT. Mat is same as Bitmap in android
            Mat displayImg = new Mat();
            //Mat displayImgBoder = new Mat();
            Mat result = new Mat();

            // load image from drawable
            Mat source = Utils.loadResource(TemplateMatchingActivity.this, originalImage, Highgui.CV_LOAD_IMAGE_COLOR);
            Imgproc.cvtColor(source, displayImg, Imgproc.COLOR_RGB2BGRA);

            /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String currentDateandTime = sdf.format(new Date());
            String fileName = Environment.getExternalStorageDirectory().getPath() + "/sample_picture_" + currentDateandTime + ".jpg";
            // save image to sd card
            Highgui.imwrite(fileName, source);*/


            // load object image for comparing
            Mat objectToFind = Utils.loadResource(this, objectToFindImage, Highgui.CV_LOAD_IMAGE_COLOR);
            // convert to get the right color of image
            //Mat templateDisplay = new Mat();
            //Mat templateDisplayBorder = new Mat();
            //Imgproc.cvtColor(objectToFind, templateDisplay, Imgproc.COLOR_RGB2BGRA);
            // convert to get the right color of image


            // convert Mat to bitmap for display to se
            //Bitmap bmpTmp = Bitmap.createBitmap(templateDisplay.cols(), templateDisplay.rows(), Bitmap.Config.ARGB_8888);
            //Utils.matToBitmap(templateDisplay, bmpTmp);
            imageViewObjectToFind.setImageResource(objectToFindImage);


            // create emplty Mat result for store result
            int result_cols = source.cols() - objectToFind.cols() + 1;
            int result_rows = source.rows() - objectToFind.rows() + 1;
            result.create(result_rows, result_cols, CvType.CV_32FC1);


            // compare source and template image
            // "Method: \n 0: SQDIFF \n 1: SQDIFF NORMED \n 2: TM CCORR \n 3: TM CCORR NORMED \n 4: TM COEFF \n 5: TM COEFF NORMED"
            Imgproc.matchTemplate(source, objectToFind, result, 0 /*SQDIFF*/);
            Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());


            Point matchLoc;

            // matched position
            Core.MinMaxLocResult mmResult = Core.minMaxLoc(result);


            // min loc is the matched point, it depend on the Method that used
            matchLoc = mmResult.minLoc;


            // scalar is color
            Scalar scalar = new Scalar(0, 0, 255);


            Core.rectangle(displayImg, matchLoc, new Point(matchLoc.x + objectToFind.cols(), matchLoc.y + objectToFind.rows()), scalar, 3, 8, 0);


            // draw boder for an image
            /*int top = (int) (0.05 * displayImg.rows());

            Scalar scalar1 = new Scalar(96, 128, 15);
            Imgproc.copyMakeBorder(displayImg, displayImgBoder, top, top, top, top, Imgproc.BORDER_CONSTANT, scalar1);*/


            Bitmap bmp = Bitmap.createBitmap(displayImg.cols(), displayImg.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(displayImg, bmp);

            imageViewFinalResult.setImageBitmap(bmp);

        }
        catch (Exception ex)
        {
            Log.e("", "");
        }


    }
}
