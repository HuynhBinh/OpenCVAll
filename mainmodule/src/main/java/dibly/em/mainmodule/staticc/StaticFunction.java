package dibly.em.mainmodule.staticc;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

/**
 * Created by USER on 7/7/2015.
 */
public class StaticFunction
{

    public static Bitmap matToBitmap(Mat mat)
    {
        // convert mat to bitmap for display
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

}
