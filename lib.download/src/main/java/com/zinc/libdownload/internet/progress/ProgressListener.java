package com.zinc.libdownload.internet.progress;

public interface ProgressListener {
    void onProgress(long currentBytes, long contentLength, boolean done);
}