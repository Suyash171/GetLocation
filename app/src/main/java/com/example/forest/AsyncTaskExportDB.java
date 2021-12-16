package com.example.forest;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.channels.FileChannel;

/**
 * Created by naresh on 18-Jul-2019.
 */
public class AsyncTaskExportDB extends AsyncTask<Void, Float, Boolean> {
    private static final String TAG = "AsyncTaskExportDB";
    private static final String EXPORT_FOLDER_NAME = "SVM Location Record";

    public interface ExportDBEventsListener {
        void onExportSuccess();

        void onExportFailed(String s);

        void exportProgress(float progress);
    }

    private String errorMessage = "";
    private WeakReference<ExportDBEventsListener> listenerWeakReference;
    private File dbFile;

    public AsyncTaskExportDB(Context context) {
        if (context instanceof ExportDBEventsListener) {
            this.listenerWeakReference = new WeakReference<>(((ExportDBEventsListener) context));
        } else {
            throw new IllegalArgumentException("ExportDBEventsListener is not implemented");
        }
        dbFile = context.getDatabasePath("app_database");
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected Boolean doInBackground(Void... voids) {

        if (dbFile == null) {
            errorMessage = "Database file not found";
            return false;
        }
        if (!isExternalStorageWritable()) {
            errorMessage = "External storage not available";
            return false;
        }

        File outputDir = createDirectory();
        if (outputDir == null) {
            errorMessage = "Output directory not created";
            return false;
        }

        File dbParentDir = dbFile.getParentFile();
        File[] dbFilesArray = dbParentDir.listFiles();

        if (dbFilesArray == null) {
            errorMessage = "Database file not found";
            return false;
        }

        for (File file : dbFilesArray) {

            //create input stream
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                errorMessage = "Database file not found";
                return false;
            }

            //create output stream
            String fileName = file.getName();
            File outputFile = new File(outputDir, fileName);
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(outputFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                errorMessage = "Unable to create export folder";
                return false;
            }

            //copy file
            try {
                copyFile(inputStream, outputStream);
            } catch (IOException e) {
                e.printStackTrace();
                errorMessage = "An error occurred while copying the file";
                return false;
            }
        }

        return true;
    }


    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        ExportDBEventsListener listener = listenerWeakReference.get();
        if (listener == null) {
            return;
        }

        if (!aBoolean) {
            listener.onExportFailed(errorMessage);
            return;
        }

        listener.onExportSuccess();
    }

    @Override
    protected void onProgressUpdate(Float... values) {
        super.onProgressUpdate(values);
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }

        return false;
    }

    private File createDirectory() {
        File file = new File(Environment.getExternalStorageDirectory(), EXPORT_FOLDER_NAME);

        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(TAG, "Directory not created");
                return null;
            }
        }
        return file;

    }

    private void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }

}
