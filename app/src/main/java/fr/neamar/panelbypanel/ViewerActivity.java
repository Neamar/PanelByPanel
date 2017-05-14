package fr.neamar.panelbypanel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import fr.neamar.panelbypanel.panel.PanelAnalyzer;

public class ViewerActivity extends AppCompatActivity {
    private static final boolean DEBUG = true;

    private static final String TAG = "ViewerActivity";
    private int currentPanelNumber = 0;
    private ArrayList<Rect> panels;
    private PanelImageView panelImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        panelImageView = (PanelImageView) findViewById(R.id.page);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = DEBUG;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample, options);
        PanelAnalyzer panelAnalyzer = new PanelAnalyzer(bitmap, true);
        panels = panelAnalyzer.getPanels();
        panelAnalyzer.colorizeBackground();

        panelImageView.setImageBitmap(bitmap);
        panelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToNextPanel();
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveToNextPanel();
            }
        }, 100);
    }

    protected void moveToNextPanel() {
        Rect panel = panels.get(currentPanelNumber % panels.size());
        Log.i(TAG, "Moving to " + panel);
        panelImageView.goToPanel(panel);

        currentPanelNumber += 1;
    }
}
