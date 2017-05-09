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

public class PanelActivity extends AppCompatActivity {

    private static final String TAG = "PanelActivity";
    private int currentPanelNumber = 0;
    private ArrayList<Rect> panels;
    private PanelImageView panelImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);

        panelImageView = (PanelImageView) findViewById(R.id.page);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample);
        PanelAnalyzer panelAnalyzer = new PanelAnalyzer(bitmap);
        panels = panelAnalyzer.getPanels();

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
