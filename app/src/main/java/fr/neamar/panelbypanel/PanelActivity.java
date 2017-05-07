package fr.neamar.panelbypanel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class PanelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);

        findViewById(R.id.page).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }
}
