package co.zinc.jdownload;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;

import com.zinc.libdownload.config.TagConfig;
import com.zinc.libdownload.fragment.DownloadRoundProgressFragment;
import com.zinc.libdownload.internet.OkHttpClientManager;
import com.zinc.libdownload.internet.bean.DownloadingInfo;
import com.zinc.libdownload.internet.listener.DownloadInfoListener;
import com.zinc.libdownload.internet.progress.ProgressListener;
import com.zinc.libdownload.testTools.ThreadTimer;
import com.zinc.libdownload.utils.Arith;
import com.zinc.libdownload.utils.SystemUtils;

import okhttp3.Request;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DownloadInfoListener {

    public static final String DOWNLOAD_MAIN_PATH = "JFrameTest";
    public static final int DOWNLOAD_PROGRESS_CHANGE = 0x002;

    private TextView btn_download;
    private TextView btn_show_download_dialog;
    private TextView btn_show_upload_dialog;

    private DownloadRoundProgressFragment downloadRoundProgressFragment;

    private JWeakHandler jWeakHandler = new JWeakHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_download = findViewById(R.id.btn_download);
        btn_show_download_dialog = findViewById(R.id.btn_show_download_dialog);
        btn_show_upload_dialog = findViewById(R.id.btn_show_upload_dialog);

        btn_download.setOnClickListener(this);
        btn_show_download_dialog.setOnClickListener(this);
        btn_show_upload_dialog.setOnClickListener(this);

        downloadRoundProgressFragment = DownloadRoundProgressFragment.newInstance();
        downloadRoundProgressFragment.setCancelable(false);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_download:
                OkHttpClientManager.getInstance().setDownloadInfoListener(this);

                downloadRoundProgressFragment.show(getSupportFragmentManager());

                ThreadTimer.progess = 0;
                new Thread(new ThreadTimer(jWeakHandler)).start();

                OkHttpClientManager.downloadAsyn("http://woyunbao-1253685439.file.myqcloud.com/app/aoyun/201709/aoyun_128_v2.5.128.20170926_debug.apk",
                        getDownloadMainPath(),
                        new OkHttpClientManager.ResultCallback<String>() {
                            @Override
                            public void onError(Request request, Exception e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onResponse(String absolutePath) {
                                Log.i("文件下载", "onResponse: " + absolutePath);
                                Toast.makeText(MainActivity.this, "下载成功，路径：" + absolutePath, Toast.LENGTH_SHORT).show();

                                downloadRoundProgressFragment.dismiss();
                                SystemUtils.installAuto(MainActivity.this, new File(absolutePath));
                            }
                        });
                break;
            case R.id.btn_show_download_dialog:

                downloadRoundProgressFragment.show(getSupportFragmentManager());
                ThreadTimer.progess = 0;
                new Thread(new ThreadTimer(jWeakHandler)).start();
                break;

            case R.id.btn_show_upload_dialog:

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onProgress(DownloadingInfo downloadingInfo) {

        double percent = 0;

        try {
            percent = Arith.div((double) downloadingInfo.getCurrentBytes(), (double) downloadingInfo.getContentLength(), 5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final int perInt = (int) (percent * 100);

        Log.i(TagConfig.TAG, "方法【onProgress】: downloadingInfo" + downloadingInfo.toString() + "；percent：" + percent + "；perInt：" + perInt);

//        if (downloadingInfo.isDone()) {
//            downloadRoundProgressFragment.dismiss();
//        } else {
//            Message message = jWeakHandler.obtainMessage(DOWNLOAD_PROGRESS_CHANGE);
//            message.obj = perInt;
//            jWeakHandler.sendMessage(message);

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloadRoundProgressFragment.setProgress(perInt, 100);
            }
        });

//        }

    }

    @Override
    public String getFileNamePrefix() {
        return "JFrameTest";
    }

    public static String getDownloadMainPath() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + DOWNLOAD_MAIN_PATH;
        return path;
    }

    static class JWeakHandler extends Handler {
        WeakReference<MainActivity> mActivity;

        JWeakHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity theActivity = mActivity.get();
            switch (msg.what) {
                case MainActivity.DOWNLOAD_PROGRESS_CHANGE:

                    Log.i(TagConfig.TAG, "handleMessage: " + msg.obj);

                    theActivity.downloadRoundProgressFragment.setProgress((int) msg.obj, 100);
                    break;
            }
        }
    }

}
