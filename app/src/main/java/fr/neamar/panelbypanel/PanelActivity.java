package fr.neamar.panelbypanel;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class PanelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);

        final Rect panel = new Rect(417, 441, 688, 841);
        final Rect panel2 = new Rect(577, 853, 1185, 1258);
        final int[] current = {0};
        final PanelImageView panelImageView = (PanelImageView) findViewById(R.id.page);

        panelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("WTF", "Going to panel");
                current[0] += 1;
                if(current[0] % 2 == 0) {
                    panelImageView.goToPanel(panel2);
                }
                else {
                    panelImageView.goToPanel(panel);
                }
            }
        });
    }
}
