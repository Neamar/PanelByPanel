package fr.neamar.panelbypanel.comic;

import android.graphics.Bitmap;

/**
 * Created by neamar on 25/05/17.
 */

public interface Book {
    public int getPageCount();
    public Bitmap getPage(int page);
    public void closeBook();
}
