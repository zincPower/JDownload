package co.zinc.jdownload;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import com.zinc.libdownload.testTools.ThreadTimer;
import com.zinc.libdownload.utils.Arith;
import com.zinc.libdownload.utils.SystemUtils;

import okhttp3.Request;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DownloadInfoListener {

    public static final int INSTALL_PACKAGES_REQUESTCODE = 0x001;
    public static final int GET_UNKNOWN_APP_SOURCES = 0x002;

    public static final String DOWNLOAD_MAIN_PATH = "JFrameTest";
    public static final int DOWNLOAD_PROGRESS_CHANGE = 0x002;

    private TextView btn_download;
    private TextView btn_show_download_dialog;
    private TextView btn_show_upload_dialog;

    private DownloadRoundProgressFragment downloadRoundProgressFragment;

    private JWeakHandler jWeakHandler = new JWeakHandler(this);

    private String absolutePath;

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
                                MainActivity.this.absolutePath = absolutePath;
                                checkIsAndroidO();
                            }
                        });
                break;
            case R.id.btn_show_download_dialog:

                downloadRoundProgressFragment.show(getSupportFragmentManager());
                ThreadTimer.progess = 0;
                new Thread(new ThreadTimer(jWeakHandler)).start();
                break;

            case R.id.btn_show_upload_dialog:

                this.absolutePath = "/storage/emulated/0/JFrameTest/JFrameTest_1513592345040.apk";
                checkIsAndroidO();

                break;
        }
    }

    /**
     * 判断是否是8.0,8.0需要处理未知应用来源权限问题,否则直接安装
     */
    private void checkIsAndroidO() {
        if (Build.VERSION.SDK_INT >= 26) {
            boolean b = getPackageManager().canRequestPackageInstalls();
            if (b) {
                installApk();
            } else {
                //请求安装未知应用来源的权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, INSTALL_PACKAGES_REQUESTCODE);
            }
        } else {
            installApk();
        }

    }

    private void installApk() {
        SystemUtils.installAuto(MainActivity.this, new File(absolutePath));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case INSTALL_PACKAGES_REQUESTCODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    installApk();
                } else {
                    Toast.makeText(this, "请开启安装权限", Toast.LENGTH_SHORT).show();
                    Uri packageURI = Uri.parse("package:" + this.getPackageName());
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                    startActivityForResult(intent, GET_UNKNOWN_APP_SOURCES);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GET_UNKNOWN_APP_SOURCES:
                checkIsAndroidO();
                break;

            default:
                break;
        }
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
