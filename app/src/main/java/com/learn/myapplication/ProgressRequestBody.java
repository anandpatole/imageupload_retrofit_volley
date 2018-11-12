package com.learn.myapplication;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {

    private File mFile;
    private UploadCallbacks mListener;
    private String type;
    private int lastProgress = 0;

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage);
        /*void onError();*/
        void onFinish();
    }

    ProgressRequestBody(final File file, final UploadCallbacks listener, String type) {
        mFile = file;
        mListener = listener;
        this.type = type;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return MediaType.parse(type);
    }

    @Override
    public long contentLength() {
        return mFile.length();
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        long fileLength = mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long uploaded = 0;

        try (FileInputStream in = new FileInputStream(mFile)) {
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1) {

                // update progress on UI thread
                if (uploaded == fileLength) {
                    mListener.onFinish();
                } else {
                    handler.post(new ProgressUpdater(uploaded, fileLength));
                }

                uploaded += read;
                sink.write(buffer, 0, read);
            }
        }
    }

    private class ProgressUpdater implements Runnable {
        private long mUploaded;
        private long mTotal;
        ProgressUpdater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            Log.e("Progress::", ""+(100 * mUploaded / mTotal));

            int currentProgress = (int)(100 * mUploaded / mTotal);
            if(lastProgress != currentProgress) {
                mListener.onProgressUpdate(currentProgress);
                lastProgress = currentProgress;
            }
        }
    }
}

