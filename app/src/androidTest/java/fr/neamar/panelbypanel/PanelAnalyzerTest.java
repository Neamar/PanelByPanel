package fr.neamar.panelbypanel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import junit.framework.AssertionFailedError;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import fr.neamar.panelbypanel.panel.PanelAnalyzer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PanelAnalyzerTest {
    public static final String TAG = "PanelAnalyzerTest";
    public int errorCount = 0;

    private void saveBitmap(Context context, Bitmap bitmap) throws IOException {
        File outputDir = context.getCacheDir(); // context being the Activity pointer
        File outputFile = File.createTempFile("error-" + errorCount, ".jpg", outputDir);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
    }

    /**
     * Test that an image is correctly analyzed
     *
     * @param drawable             the drawable to analyzed
     * @param expectedPanelsByTier the expected panel distribution -- for instance 3,2,1 for an image with a first panel comprising 3 tiers, the second panel being made up of 2 and the last being a single tier.
     */
    private void testResource(String drawableName, @DrawableRes int drawable, int[] expectedPanelsByTier) throws IOException {
        // getContext() => get a context for the test app, with the test drawables
        // getTargetContext() => get a context for the real app, with the actual drawable that'll be shipped.
        Context appContext = InstrumentationRegistry.getContext();


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        Bitmap bitmap = BitmapFactory.decodeResource(appContext.getResources(), drawable, options);
        PanelAnalyzer panelAnalyzer = new PanelAnalyzer(bitmap, false);
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

        try {
            Log.e(TAG, "Detected tiers for " + drawableName + ": " + tiers.toString());
            assertEquals("Invalid tier count for " + drawableName, expectedPanelsByTier.length, tiers.size());

            for (int i = 0; i < tiers.size(); i++) {
                ArrayList<Rect> tier = tiers.get(i);

                assertEquals("Invalid panel count in tier " + (i + 1) + " for " + drawableName, expectedPanelsByTier[i], tier.size());
            }
        } catch (AssertionFailedError e) {
            saveBitmap(appContext, bitmap);
            // throw e;
        }
    }

    @Test
    public void simpleComicWhiteMargin1() throws Exception {
        testResource("morris_lucky_luke_2", fr.neamar.panelbypanel.test.R.drawable.morris_lucky_luke_2, new int[]{2, 3, 2, 3});
    }

    @Test
    public void simpleComicWhiteMargin2() throws Exception {
        testResource("morris_lucky_luke", fr.neamar.panelbypanel.test.R.drawable.morris_lucky_luke, new int[]{3, 3, 2, 2});
    }

    @Test
    public void simpleComicWhiteMargin3Ambiguous() throws Exception {
        testResource("don_rosa_the_beagle_boys_vs_the_money_bin", fr.neamar.panelbypanel.test.R.drawable.don_rosa_the_beagle_boys_vs_the_money_bin, new int[]{3, 3, 2, 2});
    }

    @Test
    public void simpleComicWhiteMargin() throws Exception {
        testResource("zorn_dirna_2", fr.neamar.panelbypanel.test.R.drawable.zorn_dirna_2, new int[]{2,2,3,3});
    }

    @Test
    public void simpleComicOverflowingPanelWhiteMargin() throws Exception {
        testResource("zorn_dirna", fr.neamar.panelbypanel.test.R.drawable.zorn_dirna, new int[]{2,3,2});
    }

    @Test
    public void simpleComicSmallPanels() throws Exception {
        testResource("de_cape_et_de_crocs", fr.neamar.panelbypanel.test.R.drawable.de_cape_et_de_crocs, new int[]{1, 1, 3, 1, 4});
    }

    @Test
    public void smallMargins() throws Exception {
        testResource("waterson_calvin_hobbes", fr.neamar.panelbypanel.test.R.drawable.waterson_calvin_hobbes, new int[]{2, 4, 4});
    }

    @Test
    public void bicolorBackground() throws Exception {
        testResource("waterson_calvin_hobbes_3", fr.neamar.panelbypanel.test.R.drawable.waterson_calvin_hobbes_3, new int[]{5,4,4});
    }

    @Test
    public void borderlessPanel() throws Exception {
        testResource("waterson_calvin_hobbes_2", fr.neamar.panelbypanel.test.R.drawable.waterson_calvin_hobbes_2, new int[]{4, 4});
    }

    @Test
    public void scanLineOnRightOverflowingText() throws Exception {
        testResource("don_rosa_the_black_knight_glorps_again", fr.neamar.panelbypanel.test.R.drawable.don_rosa_the_black_knight_glorps_again, new int[]{2, 3, 2, 3});
    }

    @Test
    public void scanLineOnLeft() throws Exception {
        testResource("don_rosa_trash_or_treasure", fr.neamar.panelbypanel.test.R.drawable.don_rosa_trash_or_treasure, new int[]{1, 2, 3});
    }

    @Test
    public void scanWithNoise() throws Exception {
        testResource("don_rosa_mythological_menagerie", fr.neamar.panelbypanel.test.R.drawable.don_rosa_mythological_menagerie, new int[]{2, 2, 2, 2});
    }

    @Test
    public void blackAndWhiteComicNoTopBottomMarginOverflowingText() throws Exception {
        testResource("eiichiro_oda_one_piece", fr.neamar.panelbypanel.test.R.drawable.eiichiro_oda_one_piece, new int[]{3, 1, 2});
    }

    @Test
    public void darkNonUniformMargin() throws Exception {
        testResource("dorison_bec_sanctuaire_vol_1", fr.neamar.panelbypanel.test.R.drawable.dorison_bec_sanctuaire_vol_1, new int[]{3, 3, 1});
    }

    @Test
    public void complexLayoutWhiteMargin() throws Exception {
        testResource("dorison_bec_sanctuaire_vol_2", fr.neamar.panelbypanel.test.R.drawable.dorison_bec_sanctuaire_vol_2, new int[]{3, 2, 3});
    }

    @Test
    public void universalWarOneTest() throws Exception {
        testResource("bajram_universal_war_one", fr.neamar.panelbypanel.test.R.drawable.bajram_universal_war_one, new int[]{4, 1, 4});
    }

    @Test
    public void noMarginBackground() throws Exception {
        testResource("nebezial_death_vigil", fr.neamar.panelbypanel.test.R.drawable.nebezial_death_vigil, new int[]{1, 4, 1, 1, 1, 4});
    }

    @Test
    public void complexNonUniformBackground() throws Exception {
        testResource("shiniez_sunstone", fr.neamar.panelbypanel.test.R.drawable.shiniez_sunstone, new int[]{1, 1, 1, 1, 5});
    }
}