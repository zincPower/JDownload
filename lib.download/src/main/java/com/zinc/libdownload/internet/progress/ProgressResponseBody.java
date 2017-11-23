package com.zinc.libdownload.internet.progress;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 包装的响体，处理进度
 * User:lizhangqu(513163535@qq.com)
 * Date:2015-09-02
 * Time: 17:18
 */
public class ProgressResponseBody extends ResponseBody {
    //实际的待包装响应体
    private final ResponseBody responseBody;
    //进度回调接口
    private final ProgressListener progressListener;
    //包装完成的BufferedSource
    private BufferedSource bufferedSource;

    private Handler mMainHandler;

    /**
     * 构造函数，赋值
     *
     * @param responseBody     待包装的响应体
     * @param progressListener 回调接口
     */
    public ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
        this.responseBody = responseBody;
        this.progressListener = progressListener;
        this.mMainHandler = new Handler(Looper.getMainLooper());
    }


    /**
     * 重写调用实际的响应体的contentType
     *
     * @return MediaType
     */
    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    /**
     * 重写调用实际的响应体的contentLength
     *
     * @return contentLength
     * @throws IOException 异常
     */
    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    /**
     * 重写进行包装source
     *
     * @return BufferedSource
     * @throws IOException 异常
     */
    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            //包装
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    /**
     * 读取，回调进度接口
     *
     * @param source Source
     * @return Source
     */
    private Source source(Source source) {

        return new ForwardingSource(source) {
            //当前读取字节数
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {

                long bytesRead = super.read(sink, byteCount);

                //增加当前读取的字节数，如果读取完成了bytesRead会返回-1
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;

                //回调，如果contentLength()不知道长度，会返回-1
                if (progressListener != null && responseBody.contentLength() > 0) {
                    int temp = (int) (totalBytesRead * 100 / responseBody.contentLength());
                    if (bytesRead == -1) {
                        mMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressListener.onProgress(totalBytesRead, responseBody.contentLength(), true);
                            }
                        });

                    } else if ((temp / 10) != (progress / 10)) {
                        progress = temp;
                        mMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressListener.onProgress(totalBytesRead, responseBody.contentLength(), false);
                            }
                        });
                    }
                }

                return bytesRead;

            }
        };
    }

    int progress = -10;

    /**
     * 进度回调接口，比如用于文件上传与下载
     * User:lizhangqu(513163535@qq.com)
     * Date:2015-09-02
     * Time: 17:16
     */
    public interface ProgressListener {
        void onProgress(long currentBytes, long contentLength, boolean done);
    }
}