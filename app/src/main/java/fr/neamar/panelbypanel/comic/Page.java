package fr.neamar.panelbypanel.comic;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.ArrayList;

/**
 * Created by neamar on 04/06/17.
 */

public class Page {
    public ArrayList<Rect> panels;
    public Bitmap bitmap;

    public Page(ArrayList<Rect> panels, Bitmap bitmap) {
        this.panels = panels;
        this.bitmap = bitmap;
    }
}
