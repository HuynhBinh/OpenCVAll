package dibly.em.mainmodule.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import dibly.em.mainmodule.R;

/**
 * Created by USER on 7/7/2015.
 */
public class HomeActivity extends Activity
{

    Button btnCamera;
    Button btnTemplateMatching;

    Button btnCircleDetection;
    Button btnFindCoutour;
    Button btnDetectObject;

    Button btnFindFeature;

    Button btnDetectObjectOnCam;
    Button btnMultipleChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initView();

        btnCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });

        btnTemplateMatching.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(HomeActivity.this, TemplateMatchingActivity.class);
                startActivity(intent);
            }
        });

        btnCircleDetection.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(HomeActivity.this, CircleDetectionActivity.class);
                startActivity(intent);
            }
        });


        btnFindCoutour.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(HomeActivity.this, FindCoutourActivity.class);
                startActivity(intent);
            }
        });

        btnDetectObject.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(HomeActivity.this, DetectObjectActivity.class);
                startActivity(intent);
            }
        });

        btnFindFeature.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(HomeActivity.this, FindFeatureActivity.class);
                startActivity(intent);
            }
        });

        btnDetectObjectOnCam.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(HomeActivity.this, DetectObjectOnCamActivity.class);
                startActivity(intent);
            }
        });

        btnMultipleChoice.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(HomeActivity.this, MultipleChoiceActivity.class);
                startActivity(intent);
            }
        });

    }

    private void initView()
    {
        btnCamera = (Button) findViewById(R.id.btnCamera);
        btnTemplateMatching = (Button) findViewById(R.id.btnTemplateMatching);

        btnCircleDetection = (Button) findViewById(R.id.btnCircleDetection);
        btnFindCoutour = (Button) findViewById(R.id.btnFindCoutour);

        btnDetectObject = (Button) findViewById(R.id.btnDetectObject);

        btnFindFeature = (Button) findViewById(R.id.btnFindFeature);

        btnDetectObjectOnCam = (Button) findViewById(R.id.btnDetectObjectOnCam);
        btnMultipleChoice = (Button) findViewById(R.id.btnMultipleChoice);
    }
}
