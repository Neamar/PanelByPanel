package fr.neamar.panelbypanel;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import fr.neamar.panelbypanel.comic.Book;
import fr.neamar.panelbypanel.comic.PdfBook;
import fr.neamar.panelbypanel.panel.PanelAnalyzer;

import static fr.neamar.panelbypanel.R.id.page;

public class ViewerActivity extends AppCompatActivity {
    private static final boolean DEBUG = true;

    private static final String TAG = "ViewerActivity";

    private Book currentBook;
    private int currentPageNumber = 0;

    private int currentPanelNumber = 0;
    private ArrayList<Rect> panels;
    private PanelImageView panelImageView;

    private int lastTouchCoordinateX;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        panelImageView = (PanelImageView) findViewById(page);

        Book book = null;
        try {
            book = new PdfBook(this);
            loadBook(book);
        } catch (IOException e) {
            e.printStackTrace();
        }

        panelImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                lastTouchCoordinateX = (int) motionEvent.getX();
                return false;
            }
        });
        panelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int halfWidth = (int) (view.getWidth() / 2 * 0.8);

                if(lastTouchCoordinateX > halfWidth) {
                    moveToNextPanel();
                }
                else {
                    moveToPreviousPanel();
                }
            }
        });
    }

    protected void loadBook(@NonNull Book book) {
        currentBook = book;
        currentPageNumber = 0;
        currentPanelNumber = 0;

        setTitle(book.getTitle());
        moveToPage();
    }

    /**
     * Move to page currentPageNumber
     */
    protected void moveToPage() {
        if(currentPageNumber >= currentBook.getPageCount()) {
            throw new RuntimeException("You've reached the end of the book");
        }

        Log.i(TAG, "Moving to page #" + currentPageNumber);
        Bitmap bitmap = currentBook.getPage(currentPageNumber);
        currentPanelNumber = 0;
        PanelAnalyzer panelAnalyzer = new PanelAnalyzer(bitmap, true);
        panels = panelAnalyzer.getPanels();
        // panelAnalyzer.colorizeBackground();

        panelImageView.setImageBitmap(bitmap);

        // Move to first panel
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveToPanel();
            }
        }, 100);
    }

    /**
     * Move to the next panel, or next page if no panels left to read
     */
    protected void moveToNextPanel() {
        currentPanelNumber += 1;
        if(currentPanelNumber >= panels.size()) {
            currentPageNumber += 1;
            moveToPage();
            return;
        }

        moveToPanel();
    }

    /**
     * Move to the previous panel, or last page if on first panel
     */
    protected void moveToPreviousPanel() {
        currentPanelNumber -= 1;
        if(currentPanelNumber < 0) {
            if(currentPageNumber > 0) {
                currentPageNumber -= 1;
                moveToPage();
            }
            else {
                currentPanelNumber = 0;
                Toast.makeText(this, "Can't go back!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        moveToPanel();
    }

    /**
     * Move to panel currentPanelNumber on currentPageNumber
     */
    protected void moveToPanel() {
        if(currentPanelNumber >= panels.size()) {
            throw new RuntimeException("Trying to move to non existing panel");
        }

        Rect panel = panels.get(currentPanelNumber);
        Log.i(TAG, "Moving to panel " + panel + ", page " + currentPageNumber);
        panelImageView.goToPanel(panel);
    }
}
