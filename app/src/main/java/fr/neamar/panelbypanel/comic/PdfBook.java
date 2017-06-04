package fr.neamar.panelbypanel.comic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class PdfBook implements Book {
    /**
     * File descriptor of the PDF.
     */
    private ParcelFileDescriptor mFileDescriptor;

    /**
     * {@link android.graphics.pdf.PdfRenderer} to render the PDF.
     */
    private PdfRenderer mPdfRenderer;

    /**
     * Page that is currently shown
     */
    private PdfRenderer.Page mCurrentPage;

    public PdfBook(Context context) throws IOException {
        // In this sample, we read a PDF from the assets directory.
        File file = new File(context.getCacheDir(), "sample.pdf");
        if (!file.exists()) {
            // Since PdfRenderer cannot handle the compressed asset file directly, we copy it into
            // the cache directory.
            InputStream asset = context.getAssets().open("sample.pdf");
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }
        mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        // This is the PdfRenderer we use to render the PDF.
        if (mFileDescriptor != null) {
            mPdfRenderer = new PdfRenderer(mFileDescriptor);
        }
    }

    @Override
    public String getTitle() {
        return "Sample PDF";
    }

    @Override
    public int getPageCount() {
        return mPdfRenderer.getPageCount();
    }

    @Override
    public Bitmap getPage(int page) {
        // Make sure to close the current page before opening another one.
        if (null != mCurrentPage) {
            mCurrentPage.close();
        }
        // Use `openPage` to open a specific page in PDF.
        mCurrentPage = mPdfRenderer.openPage(page);
        // Important: the destination bitmap must be ARGB (not RGB).
        Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(),
                Bitmap.Config.ARGB_8888);
        // Here, we render the page onto the Bitmap.
        // To render a portion of the page, use the second and third parameter. Pass nulls to get
        // the default result.
        // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
        mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        return bitmap;
    }

    @Override
    public void closeBook() {
        try {
            if (null != mCurrentPage) {
                mCurrentPage.close();
            }
            mPdfRenderer.close();
            mFileDescriptor.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}
