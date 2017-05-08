package fr.neamar.panelbypanel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class PanelActivity extends AppCompatActivity {

    private static final String TAG = "PanelActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);

        final int[] current = {0};
        final PanelImageView panelImageView = (PanelImageView) findViewById(R.id.page);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample);
        PanelAnalyzer panelAnalyzer = new PanelAnalyzer(bitmap);
        final ArrayList<Rect> panels = panelAnalyzer.getPanels();

        panelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current[0] += 1;
                Rect panel = panels.get(current[0] % panels.size());
                Log.i(TAG, "Moving to " + panel);
                panelImageView.goToPanel(panel);
            }
        });
    }
}
