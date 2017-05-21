package fr.neamar.panelbypanel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Base64;
import android.util.Log;

import junit.framework.AssertionFailedError;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
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
    private static final String TAG = "PanelAnalyzerTest";
    private static final String ERROR_TAG = "PBPTestError";

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        int newHeight = (int) (scaleWidth * height);
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    /**
     * Write a base64 representation of the bitmap to the logs
     *
     * @param name
     * @param bitmap
     * @throws IOException
     */
    private void saveBitmap(String name, Bitmap bitmap) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        getResizedBitmap(bitmap, 350).compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

        Log.e(ERROR_TAG, "Error:" + name);
        for (int i = 0; i < encoded.length(); i += 900) {
            Log.e(ERROR_TAG, encoded.substring(i, Math.min(encoded.length(), i + 900)));
        }
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        options.inMutable = true;

        Bitmap bitmap = BitmapFactory.decodeResource(appContext.getResources(), drawable, options);
        PanelAnalyzer panelAnalyzer = new PanelAnalyzer(bitmap, true);
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
            saveBitmap(drawableName, bitmap);
            throw e;
        }
    }

    @BeforeClass
    public static void initialize() {
        Log.e(ERROR_TAG, "--START");
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
    public void simpleComicWhiteMargin4() throws Exception {
        testResource("zorn_dirna_2", fr.neamar.panelbypanel.test.R.drawable.zorn_dirna_2, new int[]{2, 2, 3, 3});
    }

    @Test
    public void simpleComicWhiteMargin5() throws Exception {
        testResource("peanuts", fr.neamar.panelbypanel.test.R.drawable.peanuts, new int[]{2, 5, 5});
    }

    @Test
    public void simpleComicWhiteMargin6() throws Exception {
        testResource("herge_tintin", fr.neamar.panelbypanel.test.R.drawable.herge_tintin, new int[]{2, 3, 3, 3});
    }

    @Test
    public void simpleComicWhiteMarginCompleteBlackBorderAroundPage() throws Exception {
        testResource("peyo_schtroumpfs", fr.neamar.panelbypanel.test.R.drawable.peyo_schtroumpfs, new int[]{2, 3, 3, 4});
    }

    @Test
    public void simpleComicOverflowingPanelWhiteMargin() throws Exception {
        testResource("zorn_dirna", fr.neamar.panelbypanel.test.R.drawable.zorn_dirna, new int[]{2, 3, 2});
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
        testResource("waterson_calvin_hobbes_3", fr.neamar.panelbypanel.test.R.drawable.waterson_calvin_hobbes_3, new int[]{5, 4, 4});
    }

    @Test
    public void borderlessPanel() throws Exception {
        testResource("waterson_calvin_hobbes_2", fr.neamar.panelbypanel.test.R.drawable.waterson_calvin_hobbes_2, new int[]{4, 4});
    }

    @Test
    public void scanLineOnRightOverflowingText() throws Exception {
        testResource("don_rosa_the_black_knight_glorps_again", fr.neamar.panelbypanel.test.R.drawable.don_rosa_the_black_knight_glorps_again, new int[]{2, 2, 1, 3});
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
    public void scanWithNoise2() throws Exception {
        testResource("asterix_obelix_legionary", fr.neamar.panelbypanel.test.R.drawable.asterix_obelix_legionary, new int[]{1, 2, 2});
    }

    @Test
    public void blackAndWhiteComicNoTopBottomMarginOverflowingText() throws Exception {
        testResource("eiichiro_oda_one_piece", fr.neamar.panelbypanel.test.R.drawable.eiichiro_oda_one_piece, new int[]{2, 1, 2});
    }

    @Test
    public void darkNonUniformMargin() throws Exception {
        testResource("dorison_bec_sanctuaire_vol_1", fr.neamar.panelbypanel.test.R.drawable.dorison_bec_sanctuaire_vol_1, new int[]{3, 3, 1});
    }

    @Test
    public void complexLayoutWhiteMargin() throws Exception {
        testResource("dorison_bec_sanctuaire_vol_2", fr.neamar.panelbypanel.test.R.drawable.dorison_bec_sanctuaire_vol_2, new int[]{3, 2, 2});
    }

    @Test
    public void universalWarOneTest() throws Exception {
        testResource("bajram_universal_war_one", fr.neamar.panelbypanel.test.R.drawable.bajram_universal_war_one, new int[]{4, 1, 4});
    }
}