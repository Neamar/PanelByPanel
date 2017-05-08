package fr.neamar.panelbypanel;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;

public class PanelAnalyzer {
    private Bitmap bitmap;
    private static final int SIMILARITY_THRESHOLD = 10;
    private static final int MIN_PANEL_HEIGHT = 30;

    public PanelAnalyzer(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public ArrayList<Rect> getPanelByRows() {
        ArrayList<Rect> rowPanels = new ArrayList<>();

        int baseColor = bitmap.getPixel(0, 0);
        int br = (baseColor >> 16) & 0xff;
        int bg = (baseColor >> 8) & 0xff;
        int bb = (baseColor) & 0xff;

        Point panelStart = null;
        int width = bitmap.getWidth();
        for (int y = 0; y < bitmap.getHeight(); y++) {
            // Number of non-background color pixel we'll allow
            int baseTolerance = Math.round(width * 0.05f);
            int x = 0;
            while (x < width) {
                int currentColor = bitmap.getPixel(x, y);
                int r = (currentColor >> 16) & 0xff;
                int g = (currentColor >> 8) & 0xff;
                int b = (currentColor) & 0xff;

                int dr = br - r;
                int dg = bg - g;
                int db = bb - b;
                if (dr * dr > SIMILARITY_THRESHOLD && dg * dg > SIMILARITY_THRESHOLD && db * db > SIMILARITY_THRESHOLD) {
                    baseTolerance--;
                    if(baseTolerance <= 0) {
                        break;
                    }
                }
                x++;
            }
            boolean fullyWhite = baseTolerance > 0;
            if (fullyWhite && panelStart != null) {
                if(y - panelStart.y > MIN_PANEL_HEIGHT) {
                    // We have a white line, stop the panel here
                    rowPanels.add(new Rect(panelStart.x, panelStart.y, width, y + 1));
                    Log.e("WTF", "Adding panel from " + panelStart.y + " to " + y);
                    panelStart = null;
                }
            } else if (!fullyWhite && panelStart == null) {
                // We have the start of a new panel
                panelStart = new Point(0, y - 1);
            }
        }

        return rowPanels;
    }
}
