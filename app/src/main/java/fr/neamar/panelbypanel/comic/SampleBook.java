package fr.neamar.panelbypanel.comic;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import fr.neamar.panelbypanel.R;

public class SampleBook implements Book {
    private boolean debug = false;
    private Resources resources;

    public SampleBook(Resources resources, boolean debug) {
        this.debug = debug;
        this.resources = resources;
    }
    @Override
    public int getPageCount() {
        return 1;
    }

    @Override
    public Bitmap getPage(int i) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = debug;

        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.sample, options);
        return bitmap;
    }

    @Override
    public void closeBook() {

    }
}
