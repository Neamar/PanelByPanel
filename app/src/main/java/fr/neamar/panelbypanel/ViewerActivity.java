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
import java.io.InputStream;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        panelImageView = (PanelImageView) findViewById(R.id.page);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        Book book;
        try {
            InputStream inputStream = getContentResolver().openInputStream(getIntent().getData());
            book = new PdfBook(this, inputStream);
            if (savedInstanceState != null) {
                loadBook(book, savedInstanceState.getInt("currentPageNumber", 0), savedInstanceState.getInt("currentPanelNumber", 0));
            } else {
                loadBook(book, 0, 0);
            }
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
            private boolean isTouching = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                if (!fromUser || isTouching) {
                    return;
                }

                currentPageNumber = progress;
                moveToPage();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTouching = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTouching = false;
                onProgressChanged(seekBar, seekBar.getProgress(), true);
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

    protected void loadBook(@NonNull Book book, int pageNumber, int panelNumber) {
        currentBook = book;
        currentPageNumber = pageNumber;

        seekBar.setMax(book.getPageCount() - 1);

        setTitle(book.getTitle());
        moveToPage();

        currentPanelNumber = panelNumber;
        moveToPanel();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (currentBook != null) {
            outState.putInt("currentPageNumber", currentPageNumber);
            outState.putInt("currentPanelNumber", currentPanelNumber);
        }

        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onDestroy() {
        if(currentBook != null) {
            currentBook.closeBook();
        }

        super.onDestroy();
    }
}
