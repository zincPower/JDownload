package com.zinc.libdownload.internet;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.FileNameMap;
import java.net.URLConnection;

import com.zinc.libdownload.internet.bean.DownloadingInfo;
import com.zinc.libdownload.internet.listener.DownloadInfoListener;
import com.zinc.libdownload.internet.progress.ProgressResponseBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 * @date 创建时间：2017/11/14
 * @author Jiang zinc
 * @description okhttp管理类
 *
 */

public class OkHttpClientManager {
    private static OkHttpClientManager mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mDelivery;
    private Gson mGson;

    private ProgressResponseBody.ProgressListener progressListener;

    private DownloadInfoListener downloadInfoListener;

    private static final String TAG = "OkHttpClientManager";

    private OkHttpClientManager() {

        progressListener = new ProgressResponseBody.ProgressListener() {
            @Override
            public void onProgress(long currentBytes, long contentLength, boolean done) {
                Log.i(TAG, "onProgress: 【currentBytes:" + currentBytes + "】【contentLength:" + contentLength + "】【done:" + done + "】");
                if (getDownloadInfoListener() != null) {
                    getDownloadInfoListener().onProgress(new DownloadingInfo(currentBytes, contentLength, done));
                }

                if (done) {
                    downloadInfoListener = null;
                }
            }
        };

        mOkHttpClient = getmOkHttpClient(new OkHttpClient(), progressListener);

        mDelivery = new Handler(Looper.getMainLooper());

        mGson = new Gson();
    }

    /**
     * 用于当downloadInfoListener为空时，作为获取信息使用
     */
    private DownloadInfoListener jDownloadInfoListener = new DownloadInfoListener() {
        @Override
        public void onProgress(DownloadingInfo downloadingInfo) {

        }

        @Override
        public String getFileNamePrefix() {
            return "JFrame";
        }
    };

    /**
     * @param client           okhttp实体类
     * @param progressListener 进度侦听类
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 构建okhttpclient，主要做一些初始化参数
     * @version
     */
    private OkHttpClient getmOkHttpClient(OkHttpClient client, final ProgressResponseBody.ProgressListener progressListener) {

        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {

                //拦截
                Response originalResponse = chain.proceed(chain.request());

                //包装响应体并返回
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();

            }
        };

        return client.newBuilder()
                .addInterceptor(interceptor)
                .build();

    }

    /**
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 获取单例
     * @version
     */
    public static OkHttpClientManager getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpClientManager.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpClientManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 获取下载侦听类
     * @version
     */
    public DownloadInfoListener getDownloadInfoListener() {
        return downloadInfoListener == null ? jDownloadInfoListener : downloadInfoListener;
    }

    /**
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 设置下载侦听类（如果不设置，不会有问题，但就无法获取到对应和设置对应的信息）
     * @version
     */
    public void setDownloadInfoListener(DownloadInfoListener downloadInfoListener) {
        this.downloadInfoListener = downloadInfoListener;
    }

    /**
     * @param url      上传路径
     * @param files    上传文件数组
     * @param fileKeys 上传文件键组
     * @param params   参数
     * @return
     * @throws IOException
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 同步基于post的文件上传【多文件，带参数】
     * @version
     */
    private Response _post(String url, File[] files, String[] fileKeys, Param... params) throws IOException {
        Request request = buildMultipartFormRequest(url, files, fileKeys, params);
        return mOkHttpClient.newCall(request).execute();
    }

    /**
     * @param url     上传路径
     * @param file    上传文件
     * @param fileKey 上传文件键组
     * @return
     * @throws IOException
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 同步基于post的文件上传【单文件，不带参】
     * @version
     */
    private Response _post(String url, File file, String fileKey) throws IOException {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, null);
        return mOkHttpClient.newCall(request).execute();
    }

    /**
     * @param url     上传路径
     * @param file    上传文件
     * @param fileKey 上传文件键组
     * @param params  参数
     * @return
     * @throws IOException
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 同步基于post的文件上传【单文件，带参数】
     * @version
     */
    private Response _post(String url, File file, String fileKey, Param... params) throws IOException {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, params);
        return mOkHttpClient.newCall(request).execute();
    }

    /**
     * @param url      上传路径
     * @param callback 回调
     * @param files    上传文件数组
     * @param fileKeys 上传文件键组
     * @throws IOException
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 异步基于post的文件上传
     * @version
     */
    private void _postAsyn(String url, ResultCallback callback, File[] files, String[] fileKeys, Param... params) throws IOException {
        Request request = buildMultipartFormRequest(url, files, fileKeys, params);
        deliveryResult(callback, request);
    }

    /**
     * @param url      上传路径
     * @param callback 回调
     * @param file     上传文件
     * @param fileKey  上传文件
     * @throws IOException
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 异步基于post的文件上传【单文件，不带参数】
     * @version
     */
    private void _postAsyn(String url, ResultCallback callback, File file, String fileKey) throws IOException {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, null);
        deliveryResult(callback, request);
    }

    /**
     * @param url      上传路径
     * @param callback 回调
     * @param file     上传文件
     * @param fileKey  上传文件
     * @param params   参数
     * @throws IOException
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 异步基于post的文件上传【单文件，带参数】
     * @version
     */
    private void _postAsyn(String url, ResultCallback callback, File file, String fileKey, Param... params) throws IOException {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, params);
        deliveryResult(callback, request);
    }

    /**
     * @param url         下载路径
     * @param destFileDir 本地文件存储的文件夹
     * @param callback    回调
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 异步下载文件
     * @version
     */
    private void _downloadAsyn(final String url, final String destFileDir, final ResultCallback callback) {
        final Request request = new Request.Builder().url(url).build();

        final Call call = mOkHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                sendFailedStringCallback(request, e, callback);

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                File dirFile = new File(destFileDir);
                if(!dirFile.exists()){
                    Log.i(TAG, destFileDir+" 文件不存在,进行创建");
                    boolean result = dirFile.mkdirs();

                    if(!result){
                        Log.i(TAG, destFileDir+" 创建失败");
                    }
                }

                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;

                try {

                    is = response.body().byteStream();

                    File file = new File(destFileDir,getFileName(url));

//                    if(!file.exists()){
//                        file.createNewFile();
//                    }

                    fos = new FileOutputStream(file);

                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();

                    //如果下载文件成功，第一个参数为文件的绝对路径
                    sendSuccessResultCallback(file.getAbsolutePath(), callback);

                } catch (IOException e) {

                    sendFailedStringCallback(response.request(), e, callback);

                } finally {

                    try {
                        if (is != null) is.close();
                    } catch (IOException e) {
                        Log.e(TAG, "onResponse: " + e.getMessage());
                    }
                    try {
                        if (fos != null) fos.close();
                    } catch (IOException e) {
                        Log.e(TAG, "onResponse: " + e.getMessage());
                    }

                }

            }
        });
    }

    /**
     * @param path 下载的路径
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 获取下载的文件名
     * @version
     */
    private String getFileName(String path) {

        String timeStamp = "" + System.currentTimeMillis();

        int sepIndex = path.lastIndexOf("/");
        String lastPart = (sepIndex < 0) ? path : path.substring(sepIndex + 1, path.length());

        int separatorIndex = lastPart.lastIndexOf(".");
        String type = (separatorIndex < 0) ? "png" : lastPart.substring(separatorIndex + 1, lastPart.length());

        if (TextUtils.isEmpty(getDownloadInfoListener().getFileNamePrefix())) {
            return timeStamp + "." + type;
        } else {
            return getDownloadInfoListener().getFileNamePrefix() + "_" + timeStamp + "." + type;
        }
    }

    //=========================对外公布的方法 start===============================

    /**
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 多文件，带参数上传
     * @version
     */
    public static Response post(String url, File[] files, String[] fileKeys, Param... params) throws IOException {
        return getInstance()._post(url, files, fileKeys, params);
    }

    /**
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 单文件，不带参数上传
     * @version
     */
    public static Response post(String url, File file, String fileKey) throws IOException {
        return getInstance()._post(url, file, fileKey);
    }

    /**
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 单文件，带参数上传
     * @version
     */
    public static Response post(String url, File file, String fileKey, Param... params) throws IOException {
        return getInstance()._post(url, file, fileKey, params);
    }

    /**
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 异步，多文件，带参数上传
     * @version
     */
    public static void postAsyn(String url, ResultCallback callback, File[] files, String[] fileKeys, Param... params) throws IOException {
        getInstance()._postAsyn(url, callback, files, fileKeys, params);
    }

    /**
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 异步，单文件，不带参数上传
     * @version
     */
    public static void postAsyn(String url, ResultCallback callback, File file, String fileKey) throws IOException {
        getInstance()._postAsyn(url, callback, file, fileKey);
    }

    /**
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 异步，单文件，带参数上传
     * @version
     */
    public static void postAsyn(String url, ResultCallback callback, File file, String fileKey, Param... params) throws IOException {
        getInstance()._postAsyn(url, callback, file, fileKey, params);
    }

    /**
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 异步，下载文件
     * @version
     */
    public static void downloadAsyn(String url, String destDir, ResultCallback callback) {
        getInstance()._downloadAsyn(url, destDir, callback);
    }

    //=========================对外公布的方法 end=========================

    /**
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 创建form表单请求
     * @version
     */
    private Request buildMultipartFormRequest(String url, File[] files, String[] fileKeys, Param[] params) {
        params = validateParam(params);

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (Param param : params) {

            //只是字符串时，requestbody的create第一个参数可以不填；当为文件时，需要用他的文件类型。
            builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + param.key + "\""), RequestBody.create(null, param.value));

        }

        if (files != null) {

            RequestBody fileBody = null;

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String fileName = file.getName();
                fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
                // 根据文件名设置contentType
                builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + fileKeys[i] + "\"; filename=\"" + fileName + "\""), fileBody);
            }

        }

        RequestBody requestBody = builder.build();
        return new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
    }

    /**
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 获取MimeType
     * @version
     */
    private String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    /**
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 验证参数，防止报空异常
     * @version
     */
    private Param[] validateParam(Param[] params) {
        if (params == null) {
            return new Param[0];
        } else {
            return params;
        }
    }

    /**
     *
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 发送请求
     * @version
     *
     */
    private void deliveryResult(final ResultCallback callback, Request request) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailedStringCallback(call.request(), e, callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final String string = response.body().string();
                    if (callback.mType == String.class) {
                        sendSuccessResultCallback(string, callback);
                    } else {
                        Object o = mGson.fromJson(string, callback.mType);
                        sendSuccessResultCallback(o, callback);
                    }


                } catch (IOException e) {
                    sendFailedStringCallback(response.request(), e, callback);
                } catch (com.google.gson.JsonParseException e)//Json解析的错误
                {
                    sendFailedStringCallback(response.request(), e, callback);
                }

            }
        });
    }

    /**
     * @date 创建时间 2017/11/14
     * @author Jiang zinc
     * @Description 失败回调
     * @version
     */
    private void sendFailedStringCallback(final Request request, final Exception e, final ResultCallback callback) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null)
                    callback.onError(request, e);
            }
        });
    }

    /**
     * @date 创建时间：2017/11/14
     * @author Jiang zinc
     * @description 成功回调
     */

    private void sendSuccessResultCallback(final Object object, final ResultCallback callback) {
        mDelivery.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onResponse(object);
                }
            }
        }, 1000);
    }

    /**
     * @author Jiang zinc
     * @date 创建时间 2017/11/14
     * @Description 回调抽象类
     */
    public static abstract class ResultCallback<T> {
        Type mType;

        public ResultCallback() {
            mType = getSuperclassTypeParameter(getClass());
        }

        static Type getSuperclassTypeParameter(Class<?> subclass) {
            Type superclass = subclass.getGenericSuperclass();
            Log.i(TAG, "getSuperclassTypeParameter: " + superclass.toString());
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }
            ParameterizedType parameterized = (ParameterizedType) superclass;
            return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
        }

        public abstract void onError(Request request, Exception e);

        public abstract void onResponse(T response);
    }

    /**
     * @author Jiang zinc
     * @date 创建时间：2017/11/14
     * @description 参数实体类
     */
    public static class Param {
        public Param() {
        }

        public Param(String key, String value) {
            this.key = key;
            this.value = value;
        }

        String key;
        String value;
    }

}
