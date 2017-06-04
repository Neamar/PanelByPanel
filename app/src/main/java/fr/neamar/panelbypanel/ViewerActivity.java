package fr.neamar.panelbypanel;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import fr.neamar.panelbypanel.comic.Book;
import fr.neamar.panelbypanel.comic.Page;
import fr.neamar.panelbypanel.comic.PdfBook;

public class ViewerActivity extends AppCompatActivity {
    private static final boolean DEBUG = true;

    private static final String TAG = "ViewerActivity";

    private Book currentBook;
    private int currentPageNumber = 0;

    private int currentPanelNumber = 0;
    private ArrayList<Rect> panels;

    private SeekBar seekBar;
    private PanelImageView panelImageView;

    private int lastTouchCoordinateX;

    private Handler scrollHandler = new Handler();
    private Runnable scrollRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        panelImageView = (PanelImageView) findViewById(R.id.page);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        Book book;
        try {
            book = new PdfBook(this);
            loadBook(book);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // We need to store the coordinates of touch everts to know if user is trying to move forward or backward.
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

                if (lastTouchCoordinateX > halfWidth) {
                    moveToNextPanel();
                } else {
                    moveToPreviousPanel();
                }
            }
        });

        // Movement on the seekbar hould move to another page,
        // But in a smooth way to avoid overwhelming the CPU
        // so we implement a message queue, and wait for 300ms before doing anything.
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }

                if (scrollRunnable != null) {
                    scrollHandler.removeCallbacks(scrollRunnable);
                }

                scrollRunnable = new Runnable() {
                    @Override
                    public void run() {
                        currentPageNumber = progress;
                        moveToPage();
                    }
                };

                scrollHandler.postDelayed(scrollRunnable, 300);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToNextPanel();
            }
        });

        findViewById(R.id.prevButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToPreviousPanel();
            }
        });
    }

    protected void loadBook(@NonNull Book book) {
        currentBook = book;
        currentPageNumber = 0;
        currentPanelNumber = 0;

        seekBar.setMax(book.getPageCount() - 1);

        setTitle(book.getTitle());
        moveToPage();
    }

    /**
     * Move to page currentPageNumber
     */
    protected void moveToPage() {
        if (currentPageNumber >= currentBook.getPageCount()) {
            Toast.makeText(this, "You've reached the end of the book.", Toast.LENGTH_SHORT).show();
            return;
        }

        seekBar.setProgress(currentPageNumber);

        Log.i(TAG, "Moving to page #" + currentPageNumber);
        Page page = currentBook.getPage(currentPageNumber);
        currentPanelNumber = 0;
        panels = page.panels;

        panelImageView.setImageBitmap(page.bitmap);

        // display the full page
        panelImageView.goToPanel(new Rect(0, 0, page.bitmap.getWidth(), page.bitmap.getHeight()));

        // And move to first panel after a small delay
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
        if (currentPanelNumber >= panels.size()) {
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
        if (currentPanelNumber < 0) {
            if (currentPageNumber > 0) {
                currentPageNumber -= 1;
                moveToPage();
                currentPanelNumber = panels.size() - 1;
                moveToPanel();
            } else {
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
        if (currentPanelNumber >= panels.size()) {
            throw new RuntimeException("Trying to move to non existing panel");
        }

        Rect panel = panels.get(currentPanelNumber);
        Log.i(TAG, "Moving to panel " + panel + ", page " + currentPageNumber);
        panelImageView.goToPanel(panel);
    }
}
