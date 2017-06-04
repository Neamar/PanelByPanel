package fr.neamar.panelbypanel.comic;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.SparseArray;

import java.util.ArrayList;

import fr.neamar.panelbypanel.panel.PanelAnalyzer;

/**
 * Created by neamar on 25/05/17.
 */

public abstract class Book {
    private SparseArray<ArrayList<Rect>> pagesRect = new SparseArray<>();

    public abstract String getTitle();
    public abstract int getPageCount();
    public abstract Bitmap getPageBitmap(int page);
    public abstract void closeBook();

    public Page getPage(final int page) {
        return getPage(page, true);
    }

    private Page getPage(final int page, boolean preloadNextPage) {
        Bitmap bitmap = getPageBitmap(page);
        ArrayList<Rect> panels = pagesRect.get(page);

        // Keep a cache of known panels
        if(panels == null) {
            PanelAnalyzer panelAnalyzer = new PanelAnalyzer(bitmap, true);
            panels = panelAnalyzer.getPanels();

            pagesRect.put(page, panels);
        }

        // Do we know the panels for next page?
        if(preloadNextPage && page + 1 < getPageCount() && pagesRect.get(page + 1) == null) {
            new Thread(new Runnable() {
                public void run() {
                    // Ensure we only buffer one page
                    getPage(page + 1, false);
                }
            }).start();
        }

        return new Page(panels, bitmap);
    }

}
