package fr.neamar.panelbypanel.panel;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;


public class PanelAnalyzer {
    private static final String TAG = "PanelAnalyzer";
    private static final int[] DEBUG_COLORS = new int[]{
            Color.rgb(255, 0, 0),
            Color.rgb(0, 255, 0),
            Color.rgb(0, 0, 255),
    };

    private Bitmap bitmap;
    // How close on each RGB component a color has to be to be considered "background color"
    private static final int SIMILARITY_THRESHOLD = 20;

    // Minimum height (px) for a tier
    private static final int MIN_TIER_HEIGHT = 30;

    // Minimum width (px) for a panel
    private static final int MIN_PANEL_WIDTH = 30;

    // Percentage of pixels that can be different from background color before we assume the line is not "background color".
    // Useful for bad scans (stains, ...), footnotes or art effects between tiers / panels
    private static final float TIER_NOT_EMPTY_TOLERANCE = 0.05f;

    // Percentage of pixels that can be different from background color before we assume the line is not "background color".
    // Useful for bad scans (stains, ...), footnotes or art effects between tiers / panels
    private static final float PANEL_NOT_EMPTY_TOLERANCE = 0.1f;

    public PanelAnalyzer(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    // Horizontal gutter detection
    public ArrayList<Rect> getTiers() {
        ArrayList<Rect> rowPanels = new ArrayList<>();

        int baseColor = bitmap.getPixel(0, 0);
        int br = (baseColor >> 16) & 0xff;
        int bg = (baseColor >> 8) & 0xff;
        int bb = (baseColor) & 0xff;

        Point tierStart = null;
        int width = bitmap.getWidth();
        for (int y = 0; y < bitmap.getHeight(); y++) {
            // Number of non-background color pixel we'll allow
            int baseTolerance = Math.round(width * TIER_NOT_EMPTY_TOLERANCE);
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
                    if (baseTolerance <= 0) {
                        break;
                    }
                }
                x++;
            }
            boolean fullyWhite = baseTolerance > 0;
            if (fullyWhite && tierStart != null) {
                if (y - tierStart.y > MIN_TIER_HEIGHT) {
                    // We have a white line, stop the panel here
                    rowPanels.add(new Rect(tierStart.x, tierStart.y, width, y + 1));
                    Log.i(TAG, "Adding row panel from " + tierStart.y + " to " + y);
                    tierStart = null;
                }
            } else if (!fullyWhite && tierStart == null) {
                // We have the start of a new panel
                tierStart = new Point(0, y - 1);
            }
        }

        return rowPanels;
    }

    // Vertical gutter detection
    public ArrayList<Rect> getPanels() {
        return getPanels(false);
    }

    public ArrayList<Rect> getPanels(boolean debug) {
        Canvas debugCanvas = null;
        Paint debugPaint = null;
        int debugCount = 0;
        if (debug) {
            debugCanvas = new Canvas(bitmap);
            debugPaint = new Paint();
            debugPaint.setStrokeWidth(4);
            debugPaint.setStyle(Paint.Style.STROKE);
        }

        ArrayList<Rect> tiers = getTiers();
        ArrayList<Rect> panels = new ArrayList<>();

        int baseColor = bitmap.getPixel(0, 0);
        int br = (baseColor >> 16) & 0xff;
        int bg = (baseColor >> 8) & 0xff;
        int bb = (baseColor) & 0xff;

        for (Rect rowPanel : tiers) {
            Point panelStart = null;
            int height = rowPanel.height();
            for (int x = rowPanel.left; x < rowPanel.right - 1; x++) {
                // Number of non-background color pixel we'll allow
                int baseTolerance = Math.round(height * PANEL_NOT_EMPTY_TOLERANCE);
                int y = rowPanel.top;
                while (y < rowPanel.bottom) {
                    int currentColor = bitmap.getPixel(x, y);
                    int r = (currentColor >> 16) & 0xff;
                    int g = (currentColor >> 8) & 0xff;
                    int b = (currentColor) & 0xff;

                    int dr = br - r;
                    int dg = bg - g;
                    int db = bb - b;
                    if (dr * dr > SIMILARITY_THRESHOLD && dg * dg > SIMILARITY_THRESHOLD && db * db > SIMILARITY_THRESHOLD) {
                        baseTolerance--;
                        if (baseTolerance <= 0) {
                            break;
                        }
                    }
                    y++;
                }
                boolean fullyWhite = baseTolerance > 0;
                if (fullyWhite && panelStart != null) {
                    if (x - panelStart.x > MIN_PANEL_WIDTH) {
                        // We have a white line, stop the panel here
                        Rect rect = new Rect(panelStart.x, panelStart.y, x, rowPanel.bottom);
                        panels.add(rect);
                        Log.i(TAG, "Adding panel at " + rect.toString());
                        panelStart = null;

                        if (debug) {
                            debugPaint.setColor(DEBUG_COLORS[debugCount++ % DEBUG_COLORS.length]);
                            debugCanvas.drawRect(rect, debugPaint);
                        }
                    }
                } else if (!fullyWhite && panelStart == null) {
                    // We have the start of a new panel
                    panelStart = new Point(x, rowPanel.top);
                }
            }
        }


        return panels;
    }
}
