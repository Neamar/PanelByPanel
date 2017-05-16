package fr.neamar.panelbypanel.panel;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class PanelAnalyzer {
    private static final String TAG = "PanelAnalyzer";
    private static final int[] DEBUG_COLORS = new int[]{
            Color.rgb(255, 0, 0),
            Color.rgb(0, 255, 0),
            Color.rgb(0, 0, 255),
    };

    // Orange-ish
    private static final int DEBUG_BACKGROUND_HORIZONTAL = Color.rgb(255, 128, 0);
    // Purple-ish
    private static final int DEBUG_BACKGROUND_VERTICAL = Color.rgb(255, 0, 128);

    // How close on each RGB component a color has to be to be considered "background color"
    private static final int SIMILARITY_THRESHOLD = 15 * 15;

    // Minimum height (%) for a tier
    private static final float MIN_TIER_HEIGHT = 0.1f;

    // Minimum width (%) for a panel
    private static final float MIN_PANEL_WIDTH = 0.1f;

    // Percentage of pixels that can be different from background color before we assume the line is not "background color".
    // Useful for bad scans (stains, ...), footnotes or art effects between tiers / panels
    private static final float TIER_NOT_EMPTY_TOLERANCE = 0.05f;

    // Percentage of pixels that can be different from background color before we assume the line is not "background color".
    // Useful for bad scans (stains, ...), footnotes or art effects between tiers / panels
    private static final float PANEL_NOT_EMPTY_TOLERANCE = 0.1f;

    // Bitmap to use for computations
    private Bitmap bitmap;

    private int width;
    private int height;

    private boolean debug;

    public PanelAnalyzer(Bitmap bitmap, boolean debug) {
        this.bitmap = bitmap;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        this.debug = debug;
    }

    private void addColorsAround(int x, int y, int spread, ArrayList<Integer> colors) {
        for (int i = x - spread; i < x + spread; i++) {
            for (int j = y - spread; j < y + spread; j++) {
                if (i >= 0 && i < width && j >= 0 && j < height) {
                    colors.add(bitmap.getPixel(i, j));
                }
            }
        }
    }

    private int getBaseColor() {
        ArrayList<Integer> samples = new ArrayList<>();

        // Sample colors in the four corners
        addColorsAround(0, 0, 5, samples);
        addColorsAround(width, 0, 5, samples);
        addColorsAround(0, height, 5, samples);
        addColorsAround(width, height, 5, samples);

        // Find the most common colors
        Map<Integer, Integer> counter = new HashMap<>();
        for (Integer i : samples) {
            if (!counter.containsKey(i)) {
                counter.put(i, 1);
            } else {
                counter.put(i, counter.get(i) + 1);
            }
        }

        Integer maxValue = 0;
        Integer bestMatch = 0;
        for (Integer i : counter.keySet()) {
            if (counter.get(i) > maxValue) {
                maxValue = counter.get(i);
                bestMatch = i;
            }
        }

        return bestMatch;
    }

    public void colorizeBackground() {
        int baseColor = getBaseColor();
        int br = (baseColor >> 16) & 0xff;
        int bg = (baseColor >> 8) & 0xff;
        int bb = (baseColor) & 0xff;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int currentColor = bitmap.getPixel(i, j);
                int r = (currentColor >> 16) & 0xff;
                int g = (currentColor >> 8) & 0xff;
                int b = (currentColor) & 0xff;
                int gray = (r + g + b) / 3;
                gray = gray - (gray % 64);
                gray = Color.argb(1, gray, gray, gray);
                bitmap.setPixel(i, j, gray);
            }
        }
    }

    // Horizontal gutter detection
    public ArrayList<Rect> getTiers() {
        ArrayList<Rect> rowPanels = new ArrayList<>();

        int baseColor = getBaseColor();
        int br = (baseColor >> 16) & 0xff;
        int bg = (baseColor >> 8) & 0xff;
        int bb = (baseColor) & 0xff;
        Log.v(TAG, "Base color:" + baseColor + " (r:" + br + ", g:" + bg + ", b:" + bb + ")");

        int minTierHeight = (int) (height * MIN_TIER_HEIGHT);
        int baseTolerance = (int) (width * TIER_NOT_EMPTY_TOLERANCE);

        Point tierStart = null;
        for (int y = 0; y <= height; y++) {
            // Number of non-background color pixel we'll allow
            int baseToleranceCount = baseTolerance;

            // For-loop extends beyond bitmap boundary, to add an artificial whiteline at the end.
            if (y < height) {
                int x = 0;
                while (x < width) {
                    int currentColor = bitmap.getPixel(x, y);
                    int r = (currentColor >> 16) & 0xff;
                    int g = (currentColor >> 8) & 0xff;
                    int b = (currentColor) & 0xff;

                    // Square delta for fast absolute value
                    int dr = br - r;
                    int dg = bg - g;
                    int db = bb - b;
                    if (dr * dr > SIMILARITY_THRESHOLD || dg * dg > SIMILARITY_THRESHOLD || db * db > SIMILARITY_THRESHOLD) {
                        baseToleranceCount--;
                        if (baseToleranceCount <= 0) {
                            break;
                        }
                    }
                    x++;
                }
            }

            boolean fullyWhite = baseToleranceCount > 0;

            if (fullyWhite && tierStart != null) {
                if (y - tierStart.y > minTierHeight) {
                    // We have a white line, stop the panel here
                    rowPanels.add(new Rect(tierStart.x, tierStart.y, width, y));
                    Log.i(TAG, "Adding tier from " + tierStart.y + " to " + y);
                    tierStart = null;
                }
            } else if (!fullyWhite && tierStart == null) {
                // We have the start of a new panel
                tierStart = new Point(0, y);
            }
        }

        Log.e(TAG, rowPanels.toString());

        return rowPanels;
    }

    // Vertical gutter detection
    public ArrayList<Rect> getPanels() {
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

        int baseColor = getBaseColor();
        int br = (baseColor >> 16) & 0xff;
        int bg = (baseColor >> 8) & 0xff;
        int bb = (baseColor) & 0xff;

        for (Rect rowPanel : tiers) {
            Point panelStart = null;
            int height = rowPanel.height();
            int minPanelWidth = (int) (rowPanel.width() * MIN_PANEL_WIDTH);
            int baseTolerance = (int) (height * PANEL_NOT_EMPTY_TOLERANCE);
            for (int x = rowPanel.left; x <= rowPanel.right; x++) {
                // Number of non-background color pixel we'll allow
                int baseToleranceCount = baseTolerance;

                // For-loop extends beyond bitmap boundary, to add an artificial whiteline at the end.
                if (x < rowPanel.right) {
                    int y = rowPanel.top;
                    while (y < rowPanel.bottom) {
                        int currentColor = bitmap.getPixel(x, y);
                        int r = (currentColor >> 16) & 0xff;
                        int g = (currentColor >> 8) & 0xff;
                        int b = (currentColor) & 0xff;

                        // Square delta for fast absolute value
                        int dr = br - r;
                        int dg = bg - g;
                        int db = bb - b;
                        if (dr * dr > SIMILARITY_THRESHOLD || dg * dg > SIMILARITY_THRESHOLD || db * db > SIMILARITY_THRESHOLD) {
                            baseToleranceCount--;
                            if (baseToleranceCount <= 0) {
                                break;
                            }

                        }
                        y++;
                    }
                }

                boolean fullyWhite = baseToleranceCount > 0;

                if (fullyWhite && panelStart != null) {
                    if (x - panelStart.x > minPanelWidth) {
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
