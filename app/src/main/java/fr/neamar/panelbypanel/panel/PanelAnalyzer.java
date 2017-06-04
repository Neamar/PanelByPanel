package fr.neamar.panelbypanel.panel;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.util.Log;

import java.util.ArrayList;


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

    // Minimum height (%) for a tier
    private static final float MIN_TIER_HEIGHT = 0.05f;

    // Minimum width (%) for a panel
    private static final float MIN_PANEL_WIDTH = 0.1f;

    // Minimum gradient, over the three channels, to consider a pixel as an edge
    private static final int MAX_GRADIENT = 50;

    // Pixels to skip at the top and the left, to avoid potential black border or scan lines
    private static final int DEFAULT_PAGE_MARGIN = 5;

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

    /**
     * Returns true if there is a high gradient difference (as defined by threshold) between color1 and color2
     *
     * @param color1
     * @param color2
     * @param threshold min threshold (over any RGB channel) to count as high gradient
     * @return true if high gradient
     */
    private boolean isHighGradient(@ColorInt int color1, @ColorInt int color2, int threshold) {
        int r1 = (color1 >> 16) & 0xff;
        int g1 = (color1 >> 8) & 0xff;
        int b1 = (color1) & 0xff;

        int r2 = (color2 >> 16) & 0xff;
        int g2 = (color2 >> 8) & 0xff;
        int b2 = (color2) & 0xff;

        return Math.abs(r1 - r2) > threshold || Math.abs(g1 - g2) > threshold || Math.abs(b1 - b2) > threshold;
    }

    public void colorizeBackground() {
        for (int y = DEFAULT_PAGE_MARGIN; y < height - DEFAULT_PAGE_MARGIN; y++) {
            int x;
            for (x = DEFAULT_PAGE_MARGIN; x < width - DEFAULT_PAGE_MARGIN; x++) {
                @ColorInt int color1 = bitmap.getPixel(x > DEFAULT_PAGE_MARGIN + 5 ? x - 5 : DEFAULT_PAGE_MARGIN, y);
                @ColorInt int color2 = bitmap.getPixel(x, y);
                if (isHighGradient(color1, color2, MAX_GRADIENT)) {
                    break;
                }
            }

            for (int i = 0; i < x; i++) {
                bitmap.setPixel(i, y, DEBUG_BACKGROUND_HORIZONTAL);
            }
        }
    }

    // Horizontal gutter detection
    public ArrayList<Rect> getTiers() {
        ArrayList<Rect> rowPanels = new ArrayList<>();

        int minTierHeight = (int) (height * MIN_TIER_HEIGHT);

        Point tierStart = null;
        for (int y = DEFAULT_PAGE_MARGIN; y <= height; y++) {
            // For-loop extends beyond bitmap boundary, to add an artificial whiteline at the end.
            // (for comics with no margins)
            boolean fullyWhite = true;
            if (y < height) {
                int x = DEFAULT_PAGE_MARGIN;
                while (x < width - DEFAULT_PAGE_MARGIN) {
                    @ColorInt int color1 = bitmap.getPixel(x > DEFAULT_PAGE_MARGIN + 5 ? x - 5 : DEFAULT_PAGE_MARGIN, y);
                    @ColorInt int color2 = bitmap.getPixel(x, y);
                    if (isHighGradient(color1, color2, MAX_GRADIENT)) {
                        fullyWhite = false;
                        break;
                    }

                    x++;
                }
            }

            if (fullyWhite && tierStart != null) {
                if (y - tierStart.y > minTierHeight) {
                    // We have a white line, stop the panel here
                    rowPanels.add(new Rect(tierStart.x, tierStart.y, width, y));
                    Log.d(TAG, "Adding tier from " + tierStart.y + " to " + y);
                    tierStart = null;
                }
            } else if (!fullyWhite && tierStart == null) {
                // We have the start of a new panel
                tierStart = new Point(DEFAULT_PAGE_MARGIN, y);
            }
        }

        return rowPanels;
    }

    // Vertical gutter detection
    public ArrayList<Rect> getPanels() {
        ArrayList<Rect> tiers = getTiers();
        ArrayList<Rect> panels = new ArrayList<>();

        for (Rect rowPanel : tiers) {
            Point panelStart = null;
            int minPanelWidth = (int) (rowPanel.width() * MIN_PANEL_WIDTH);
            for (int x = rowPanel.left; x <= rowPanel.right; x++) {
                boolean fullyWhite = true;
                // For-loop extends beyond bitmap boundary, to add an artificial whiteline at the end.
                // (for comics without margin)
                if (x < rowPanel.right) {
                    int y = rowPanel.top;
                    int yThreshold = Math.min(rowPanel.bottom, height - DEFAULT_PAGE_MARGIN);
                    while (y < yThreshold) {
                        @ColorInt int color1 = bitmap.getPixel(x, y > rowPanel.top + 5 ? y - 5 : rowPanel.top);
                        @ColorInt int color2 = bitmap.getPixel(x, y);
                        if (isHighGradient(color1, color2, MAX_GRADIENT)) {
                            fullyWhite = false;
                            break;
                        }
                        y++;
                    }
                }

                if (fullyWhite && panelStart != null) {
                    if (x - panelStart.x > minPanelWidth) {
                        // We have a white line, stop the panel here
                        Rect rect = new Rect(panelStart.x, panelStart.y, x, rowPanel.bottom);
                        panels.add(rect);
                        Log.d(TAG, "Adding panel at " + rect.toString());
                        panelStart = null;
                    }
                } else if (!fullyWhite && panelStart == null) {
                    // We have the start of a new panel
                    panelStart = new Point(x == DEFAULT_PAGE_MARGIN ? 0 : x, rowPanel.top);
                }
            }
        }

        if (debug) {
            Canvas debugCanvas = new Canvas(bitmap);
            Paint debugPaint = new Paint();
            debugPaint.setStrokeWidth(4);
            debugPaint.setStyle(Paint.Style.STROKE);

            for (int i = 0; i < panels.size(); i++) {
                debugPaint.setColor(DEBUG_COLORS[i % DEBUG_COLORS.length]);
                debugCanvas.drawRect(panels.get(i), debugPaint);
            }

        }

        return panels;
    }
}
