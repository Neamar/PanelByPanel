package fr.neamar.panelbypanel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import fr.neamar.panelbypanel.panel.PanelAnalyzer;

import static junit.framework.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PanelAnalyzerTest {
    /**
     * Test t hat an image is correctly analyzed
     *
     * @param drawable             the drawable to analyzed
     * @param expectedPanelsByTier the expected panel distribution -- for instance 3,2,1 for an image with a first panel comprising 3 tiers, the second panel being made up of 2 and the last being a single tier.
     */
    private void testResource(@DrawableRes int drawable, int[] expectedPanelsByTier) {
        Context appContext = InstrumentationRegistry.getTargetContext();
        Bitmap bitmap = BitmapFactory.decodeResource(appContext.getResources(), drawable);
        PanelAnalyzer panelAnalyzer = new PanelAnalyzer(bitmap);
        ArrayList<Rect> panels = panelAnalyzer.getPanels();

        ArrayList<ArrayList<Rect>> tiers = new ArrayList<>();
        ArrayList<Rect> currentTier = new ArrayList<>();

        int currentY = panels.get(0).top;
        for(Rect panel: panels) {
            if(panel.top == currentY) {
                currentTier.add(panel);
            }
            else {
                tiers.add(currentTier);
                currentY = panel.top;
                currentTier = new ArrayList<>();
            }
        }
        // Add the last panel
        tiers.add(currentTier);

        assertEquals("Invalid tier number,", expectedPanelsByTier.length, tiers.size());
    }

    @Test
    public void computeCorrectPanels() throws Exception {
        testResource(R.drawable.sample, new int[]{2, 3, 2, 2});
    }
}
