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

        final Rect panel = new Rect(417, 441, 688, 841);
        final Rect panel2 = new Rect(577, 853, 1185, 1258);
        final int[] current = {0};
        final PanelImageView panelImageView = (PanelImageView) findViewById(R.id.page);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sample);
        PanelAnalyzer panelAnalyzer = new PanelAnalyzer(bitmap);
        final ArrayList<Rect> panels = panelAnalyzer.getPanelByRows();

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
