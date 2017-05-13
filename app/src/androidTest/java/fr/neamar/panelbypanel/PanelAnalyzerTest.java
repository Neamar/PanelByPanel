package fr.neamar.panelbypanel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import fr.neamar.panelbypanel.panel.PanelAnalyzer;

import static junit.framework.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PanelAnalyzerTest {
    public static final String TAG = "PanelAnalyzerTest";

    /**
     * Test that an image is correctly analyzed
     *
     * @param drawable             the drawable to analyzed
     * @param expectedPanelsByTier the expected panel distribution -- for instance 3,2,1 for an image with a first panel comprising 3 tiers, the second panel being made up of 2 and the last being a single tier.
     */
    private void testResource(String drawableName, @DrawableRes int drawable, int[] expectedPanelsByTier) {
        // getContext() => get a context for the test app, with the test drawables
        // getTargetContext() => get a context for the real app, with the actual drawable that'll be shipped.
        Context appContext = InstrumentationRegistry.getContext();
        Bitmap bitmap = BitmapFactory.decodeResource(appContext.getResources(), drawable);
        PanelAnalyzer panelAnalyzer = new PanelAnalyzer(bitmap);
        ArrayList<Rect> panels = panelAnalyzer.getPanels();

        assertFalse("No panels detected for " + drawableName, panels.isEmpty());

        ArrayList<ArrayList<Rect>> tiers = new ArrayList<>();
        ArrayList<Rect> currentTier = new ArrayList<>();

        int currentY = panels.get(0).top;
        for (Rect panel : panels) {
            if (panel.top == currentY) {
                currentTier.add(panel);
            } else {
                tiers.add(currentTier);
                currentY = panel.top;
                currentTier = new ArrayList<>();
                currentTier.add(panel);
            }
        }
        // Add the last panel
        tiers.add(currentTier);

        Log.e(TAG, "Detected tiers for " + drawableName + ": " + tiers.toString());
        assertEquals("Invalid tier count for " + drawableName, expectedPanelsByTier.length, tiers.size());

        for (int i = 0; i < tiers.size(); i++) {
            ArrayList<Rect> tier = tiers.get(i);

            assertEquals("Invalid panel count in tier " + (i + 1) + " for " + drawableName, expectedPanelsByTier[i], tier.size());
        }

    }

    @Test
    public void computeCorrectPanels() throws Exception {
        testResource("panel_1", fr.neamar.panelbypanel.test.R.drawable.panel_1, new int[]{2, 3, 2, 2});
    }
}
